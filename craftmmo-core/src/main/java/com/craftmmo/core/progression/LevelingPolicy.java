package com.craftmmo.core.progression;

import com.craftmmo.api.progression.PlayerSkillProgress;

public interface LevelingPolicy {
    PlayerSkillProgress applyXp(PlayerSkillProgress current, long xpToAdd);
}
