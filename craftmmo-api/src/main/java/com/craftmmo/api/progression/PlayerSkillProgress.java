package com.craftmmo.api.progression;

import com.craftmmo.api.skill.SkillId;
import java.util.Objects;

public record PlayerSkillProgress(
        SkillId skillId,
        int level,
        long currentXp,
        long totalXp,
        long version
) {
    public PlayerSkillProgress {
        Objects.requireNonNull(skillId, "skillId");
        if (level < 0) {
            throw new IllegalArgumentException("Level must not be negative");
        }
        if (currentXp < 0 || totalXp < 0) {
            throw new IllegalArgumentException("XP values must not be negative");
        }
        if (totalXp < currentXp) {
            throw new IllegalArgumentException("Total XP must be greater than or equal to current XP");
        }
        if (version < 0) {
            throw new IllegalArgumentException("Version must not be negative");
        }
    }

    public static PlayerSkillProgress initial(SkillId skillId) {
        return new PlayerSkillProgress(skillId, 0, 0L, 0L, 0L);
    }

    public PlayerSkillProgress withXp(long newCurrentXp, long newTotalXp, int newLevel) {
        return new PlayerSkillProgress(skillId, newLevel, newCurrentXp, newTotalXp, version);
    }

    public PlayerSkillProgress nextVersion() {
        return new PlayerSkillProgress(skillId, level, currentXp, totalXp, Math.addExact(version, 1L));
    }
}
