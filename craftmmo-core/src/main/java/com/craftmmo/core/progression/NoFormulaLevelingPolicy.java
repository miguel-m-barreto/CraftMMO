package com.craftmmo.core.progression;

import com.craftmmo.api.progression.PlayerSkillProgress;

public final class NoFormulaLevelingPolicy implements LevelingPolicy {
    @Override
    public PlayerSkillProgress applyXp(PlayerSkillProgress current, long xpToAdd) {
        if (xpToAdd < 0) {
            throw new IllegalArgumentException("XP to add must not be negative");
        }
        return current.withXp(Math.addExact(current.currentXp(), xpToAdd), Math.addExact(current.totalXp(), xpToAdd), current.level());
    }
}
