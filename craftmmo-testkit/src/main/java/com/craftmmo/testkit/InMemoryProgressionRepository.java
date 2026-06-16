package com.craftmmo.testkit;

import com.craftmmo.api.identity.OperationId;
import com.craftmmo.api.progression.PlayerSkillProgress;
import com.craftmmo.api.skill.SkillId;
import com.craftmmo.core.progression.OperationRecordResult;
import com.craftmmo.core.progression.ProgressionRepository;
import com.craftmmo.core.progression.SkillProgressionRequest;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class InMemoryProgressionRepository implements ProgressionRepository {
    private final Map<String, PlayerSkillProgress> progress = new HashMap<>();
    private final Map<OperationId, SkillProgressionRequest> operations = new HashMap<>();
    private final Map<OperationId, PlayerSkillProgress> results = new HashMap<>();

    @Override
    public OperationRecordResult recordOperationStart(SkillProgressionRequest request, Instant now) {
        Objects.requireNonNull(request, "request");
        SkillProgressionRequest existing = operations.get(request.operationId());
        if (existing == null) {
            operations.put(request.operationId(), request);
            return OperationRecordResult.started();
        }
        boolean same = existing.playerId().equals(request.playerId())
                && existing.skillId() == request.skillId()
                && existing.operationType() == request.operationType()
                && existing.source().equals(request.source())
                && existing.xp() == request.xp()
                && existing.payloadHash().equals(request.payloadHash());
        return same
                ? OperationRecordResult.duplicateReplay(Optional.ofNullable(results.get(request.operationId())))
                : OperationRecordResult.identityConflict();
    }

    @Override
    public Optional<PlayerSkillProgress> findForUpdate(com.craftmmo.api.identity.PlayerId playerId, SkillId skillId) {
        return Optional.ofNullable(progress.get(key(playerId, skillId)));
    }

    @Override
    public boolean save(com.craftmmo.api.identity.PlayerId playerId, PlayerSkillProgress previous, PlayerSkillProgress next, Instant now) {
        String key = key(playerId, next.skillId());
        PlayerSkillProgress current = progress.get(key);
        if (current == null && previous.version() == 0) {
            progress.put(key, next);
            return true;
        }
        if (current != null && current.version() == previous.version()) {
            progress.put(key, next);
            return true;
        }
        return false;
    }

    @Override
    public void markOperationApplied(OperationId operationId, PlayerSkillProgress result, Instant now) {
        results.put(operationId, result);
    }

    private static String key(com.craftmmo.api.identity.PlayerId playerId, SkillId skillId) {
        return playerId.value() + ":" + skillId.stableId();
    }
}
