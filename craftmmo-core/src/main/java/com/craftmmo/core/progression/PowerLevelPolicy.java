package com.craftmmo.core.progression;

import com.craftmmo.api.progression.PlayerSkillProgress;
import com.craftmmo.api.skill.SkillId;
import java.util.Map;

public interface PowerLevelPolicy {
    int calculate(Map<SkillId, PlayerSkillProgress> progress);
}
