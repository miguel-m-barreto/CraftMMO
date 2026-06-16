package com.craftmmo.core.progression;

import com.craftmmo.api.progression.PlayerSkillProgress;
import java.util.Optional;

public record OperationRecordResult(OperationRecordStatus status, Optional<PlayerSkillProgress> previousResult) {
    public OperationRecordResult {
        previousResult = previousResult == null ? Optional.empty() : previousResult;
    }

    public static OperationRecordResult started() {
        return new OperationRecordResult(OperationRecordStatus.STARTED, Optional.empty());
    }

    public static OperationRecordResult duplicateReplay(Optional<PlayerSkillProgress> previousResult) {
        return new OperationRecordResult(OperationRecordStatus.DUPLICATE_REPLAY, previousResult);
    }

    public static OperationRecordResult identityConflict() {
        return new OperationRecordResult(OperationRecordStatus.IDENTITY_CONFLICT, Optional.empty());
    }
}
