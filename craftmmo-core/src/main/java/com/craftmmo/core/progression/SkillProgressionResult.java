package com.craftmmo.core.progression;

import com.craftmmo.api.progression.PlayerSkillProgress;
import java.util.Objects;

public record SkillProgressionResult(ProgressionApplyStatus status, PlayerSkillProgress progress) {
    public SkillProgressionResult {
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(progress, "progress");
    }
}
