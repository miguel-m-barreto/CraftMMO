package com.craftmmo.core.progression;

import com.craftmmo.api.progression.PlayerSkillProgress;
import com.craftmmo.api.skill.SkillId;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class ConfigurablePowerLevelPolicy implements PowerLevelPolicy {
    private final Set<SkillId> includedSkills;
    private final boolean includeChildSkills;
    private final ChildSkillPolicy childSkillPolicy;

    public ConfigurablePowerLevelPolicy(Set<SkillId> includedSkills, boolean includeChildSkills) {
        this(includedSkills, includeChildSkills, ConfiguredChildSkillPolicy.defaultPolicy());
    }

    public ConfigurablePowerLevelPolicy(Set<SkillId> includedSkills, boolean includeChildSkills, ChildSkillPolicy childSkillPolicy) {
        this.includedSkills = includedSkills.isEmpty() ? Set.of() : EnumSet.copyOf(includedSkills);
        this.includeChildSkills = includeChildSkills;
        this.childSkillPolicy = childSkillPolicy;
    }

    public static ConfigurablePowerLevelPolicy defaultPolicy() {
        return new ConfigurablePowerLevelPolicy(EnumSet.allOf(SkillId.class), true);
    }

    @Override
    public int calculate(Map<SkillId, PlayerSkillProgress> progress) {
        long total = 0L;
        for (SkillId skillId : includedSkills) {
            if (!includeChildSkills && (skillId == SkillId.SALVAGE || skillId == SkillId.SMELTING)) {
                continue;
            }
            int level = isChildSkill(skillId)
                    ? childSkillPolicy.deriveLevel(skillId, progress)
                    : progress.getOrDefault(skillId, PlayerSkillProgress.initial(skillId)).level();
            total = Math.addExact(total, level);
        }
        return Math.toIntExact(total);
    }

    private static boolean isChildSkill(SkillId skillId) {
        return skillId == SkillId.SALVAGE || skillId == SkillId.SMELTING;
    }
}
