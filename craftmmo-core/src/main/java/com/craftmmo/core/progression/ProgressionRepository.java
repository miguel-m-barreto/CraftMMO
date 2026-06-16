package com.craftmmo.core.progression;

import com.craftmmo.api.identity.OperationId;
import com.craftmmo.api.identity.PlayerId;
import com.craftmmo.api.progression.PlayerSkillProgress;
import com.craftmmo.api.skill.SkillId;
import java.time.Instant;
import java.util.Optional;

public interface ProgressionRepository {
    OperationRecordResult recordOperationStart(SkillProgressionRequest request, Instant now);

    Optional<PlayerSkillProgress> findForUpdate(PlayerId playerId, SkillId skillId);

    boolean save(PlayerId playerId, PlayerSkillProgress previous, PlayerSkillProgress next, Instant now);

    void markOperationApplied(OperationId operationId, PlayerSkillProgress result, Instant now);
}
