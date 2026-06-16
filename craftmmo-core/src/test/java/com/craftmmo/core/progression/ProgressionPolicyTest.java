package com.craftmmo.core.progression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.craftmmo.api.identity.OperationId;
import com.craftmmo.api.identity.PayloadHash;
import com.craftmmo.api.identity.PlayerId;
import com.craftmmo.api.progression.PlayerSkillProgress;
import com.craftmmo.api.skill.SkillId;
import com.craftmmo.api.time.ClockService;
import com.craftmmo.core.transaction.TransactionCallback;
import com.craftmmo.core.transaction.TransactionRunner;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

final class ProgressionPolicyTest {
    @Test
    void derivesSalvageAndSmeltingFromConfiguredParents() {
        Map<SkillId, PlayerSkillProgress> progress = new EnumMap<>(SkillId.class);
        progress.put(SkillId.REPAIR, new PlayerSkillProgress(SkillId.REPAIR, 10, 0, 0, 0));
        progress.put(SkillId.FISHING, new PlayerSkillProgress(SkillId.FISHING, 6, 0, 0, 0));
        progress.put(SkillId.MINING, new PlayerSkillProgress(SkillId.MINING, 20, 0, 0, 0));

        ConfiguredChildSkillPolicy policy = ConfiguredChildSkillPolicy.defaultPolicy();

        assertEquals(8, policy.deriveLevel(SkillId.SALVAGE, progress));
        assertEquals(15, policy.deriveLevel(SkillId.SMELTING, progress));
    }

    @Test
    void powerLevelCanExcludeChildSkills() {
        Map<SkillId, PlayerSkillProgress> progress = new EnumMap<>(SkillId.class);
        progress.put(SkillId.MINING, new PlayerSkillProgress(SkillId.MINING, 4, 0, 0, 0));
        progress.put(SkillId.SMELTING, new PlayerSkillProgress(SkillId.SMELTING, 100, 0, 0, 0));

        ConfigurablePowerLevelPolicy policy = new ConfigurablePowerLevelPolicy(java.util.EnumSet.allOf(SkillId.class), false);

        assertEquals(4, policy.calculate(progress));
    }

    @Test
    void powerLevelDerivesChildSkillsAndIgnoresPersistedChildProgress() {
        Map<SkillId, PlayerSkillProgress> progress = new EnumMap<>(SkillId.class);
        progress.put(SkillId.MINING, new PlayerSkillProgress(SkillId.MINING, 4, 0, 0, 0));
        progress.put(SkillId.REPAIR, new PlayerSkillProgress(SkillId.REPAIR, 10, 0, 0, 0));
        progress.put(SkillId.FISHING, new PlayerSkillProgress(SkillId.FISHING, 6, 0, 0, 0));
        progress.put(SkillId.SALVAGE, new PlayerSkillProgress(SkillId.SALVAGE, 100, 0, 0, 0));
        progress.put(SkillId.SMELTING, new PlayerSkillProgress(SkillId.SMELTING, 100, 0, 0, 0));

        ConfigurablePowerLevelPolicy policy = ConfigurablePowerLevelPolicy.defaultPolicy();

        assertEquals(35, policy.calculate(progress));
    }

    @Test
    void rejectsDirectXpForDerivedChildSkills() {
        PlayerId playerId = PlayerId.of(UUID.randomUUID());

        assertThrows(IllegalArgumentException.class, () ->
                SkillProgressionRequest.xpGrant(new OperationId("salvage-direct"), playerId, SkillId.SALVAGE, "unit", 1));
        assertThrows(IllegalArgumentException.class, () ->
                SkillProgressionRequest.xpGrant(new OperationId("smelting-direct"), playerId, SkillId.SMELTING, "unit", 1));
    }

    @Test
    void requestPayloadHashIsDerivedFromNormalizedSemanticFields() {
        PlayerId playerId = PlayerId.of(UUID.randomUUID());
        SkillProgressionRequest first = SkillProgressionRequest.xpGrant(new OperationId("hash-1"), playerId, SkillId.MINING, " unit ", 10);
        SkillProgressionRequest second = SkillProgressionRequest.xpGrant(new OperationId("hash-2"), playerId, SkillId.MINING, "unit", 10);
        SkillProgressionRequest differentXp = SkillProgressionRequest.xpGrant(new OperationId("hash-3"), playerId, SkillId.MINING, "unit", 11);

        assertEquals(first.payloadHash(), second.payloadHash());
        org.junit.jupiter.api.Assertions.assertNotEquals(first.payloadHash(), differentXp.payloadHash());
        org.junit.jupiter.api.Assertions.assertTrue(Arrays.stream(SkillProgressionRequest.class.getConstructors())
                .noneMatch(constructor -> Arrays.asList(constructor.getParameterTypes()).contains(PayloadHash.class)));
    }

    @Test
    void detectsDuplicateReplayAndPayloadConflict() {
        SkillProgressionService service = new SkillProgressionService(
                new InMemoryProgressionRepository(),
                new InMemoryTransactionRunner(),
                new NoFormulaLevelingPolicy(),
                () -> Instant.parse("2026-06-16T00:00:00Z")
        );
        PlayerId playerId = PlayerId.of(UUID.randomUUID());
        OperationId operationId = new OperationId("same-operation");

        assertEquals(ProgressionApplyStatus.APPLIED, service.applyXp(SkillProgressionRequest.xpGrant(operationId, playerId, SkillId.MINING, "unit", 10)).status());
        assertEquals(ProgressionApplyStatus.DUPLICATE_REPLAY, service.applyXp(SkillProgressionRequest.xpGrant(operationId, playerId, SkillId.MINING, "unit", 10)).status());
        assertEquals(ProgressionApplyStatus.IDENTITY_CONFLICT, service.applyXp(SkillProgressionRequest.xpGrant(operationId, playerId, SkillId.MINING, "unit", 11)).status());
    }

    private static final class InMemoryTransactionRunner implements TransactionRunner {
        @Override
        public <T> T inTransaction(TransactionCallback<T> callback) {
            return callback.execute();
        }
    }

    private static final class InMemoryProgressionRepository implements ProgressionRepository {
        private final Map<String, PlayerSkillProgress> progress = new HashMap<>();
        private final Map<OperationId, SkillProgressionRequest> operations = new HashMap<>();
        private final Map<OperationId, PlayerSkillProgress> results = new HashMap<>();

        @Override
        public OperationRecordResult recordOperationStart(SkillProgressionRequest request, Instant now) {
            SkillProgressionRequest existing = operations.get(request.operationId());
            if (existing == null) {
                operations.put(request.operationId(), request);
                return OperationRecordResult.started();
            }
            boolean same = existing.payloadHash().equals(request.payloadHash())
                    && existing.playerId().equals(request.playerId())
                    && existing.skillId() == request.skillId()
                    && existing.operationType() == request.operationType()
                    && existing.source().equals(request.source());
            return same
                    ? OperationRecordResult.duplicateReplay(Optional.ofNullable(results.get(request.operationId())))
                    : OperationRecordResult.identityConflict();
        }

        @Override
        public Optional<PlayerSkillProgress> findForUpdate(PlayerId playerId, SkillId skillId) {
            return Optional.ofNullable(progress.get(key(playerId, skillId)));
        }

        @Override
        public boolean save(PlayerId playerId, PlayerSkillProgress previous, PlayerSkillProgress next, Instant now) {
            progress.put(key(playerId, next.skillId()), next);
            return true;
        }

        @Override
        public void markOperationApplied(OperationId operationId, PlayerSkillProgress result, Instant now) {
            results.put(operationId, result);
        }

        private static String key(PlayerId playerId, SkillId skillId) {
            return playerId.value() + ":" + skillId.stableId();
        }
    }
}
