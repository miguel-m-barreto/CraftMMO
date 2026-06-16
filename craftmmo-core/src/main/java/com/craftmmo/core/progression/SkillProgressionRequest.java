package com.craftmmo.core.progression;

import com.craftmmo.api.identity.OperationId;
import com.craftmmo.api.identity.PayloadHash;
import com.craftmmo.api.identity.PlayerId;
import com.craftmmo.api.skill.SkillId;
import java.util.Map;
import java.util.Objects;

public final class SkillProgressionRequest {
    private final OperationId operationId;
    private final PlayerId playerId;
    private final SkillId skillId;
    private final ProgressionOperationType operationType;
    private final String source;
    private final long xp;
    private final PayloadHash payloadHash;

    private SkillProgressionRequest(
            OperationId operationId,
            PlayerId playerId,
            SkillId skillId,
            ProgressionOperationType operationType,
            String source,
            long xp
    ) {
        this.operationId = Objects.requireNonNull(operationId, "operationId");
        this.playerId = Objects.requireNonNull(playerId, "playerId");
        this.skillId = Objects.requireNonNull(skillId, "skillId");
        this.operationType = Objects.requireNonNull(operationType, "operationType");
        Objects.requireNonNull(source, "source");
        this.source = source.trim();
        if (this.source.isBlank()) {
            throw new IllegalArgumentException("Progression source must not be blank");
        }
        if (xp < 0) {
            throw new IllegalArgumentException("XP must not be negative");
        }
        if (operationType == ProgressionOperationType.XP_GRANT && isDerivedChildSkill(skillId)) {
            throw new IllegalArgumentException("Direct XP grants are not allowed for derived child skills");
        }
        this.xp = xp;
        this.payloadHash = derivePayloadHash(this.playerId, this.skillId, this.operationType, this.source, this.xp);
    }

    public static SkillProgressionRequest xpGrant(OperationId operationId, PlayerId playerId, SkillId skillId, String source, long xp) {
        return new SkillProgressionRequest(operationId, playerId, skillId, ProgressionOperationType.XP_GRANT, source, xp);
    }

    public OperationId operationId() {
        return operationId;
    }

    public PlayerId playerId() {
        return playerId;
    }

    public SkillId skillId() {
        return skillId;
    }

    public ProgressionOperationType operationType() {
        return operationType;
    }

    public String source() {
        return source;
    }

    public long xp() {
        return xp;
    }

    public PayloadHash payloadHash() {
        return payloadHash;
    }

    private static PayloadHash derivePayloadHash(
            PlayerId playerId,
            SkillId skillId,
            ProgressionOperationType operationType,
            String source,
            long xp
    ) {
        return PayloadHasher.sha256(Map.of(
                "player", playerId.value().toString(),
                "skill", skillId.stableId(),
                "type", operationType.name(),
                "source", source.trim(),
                "xp", Long.toString(xp)
        ));
    }

    private static boolean isDerivedChildSkill(SkillId skillId) {
        return skillId == SkillId.SALVAGE || skillId == SkillId.SMELTING;
    }
}
