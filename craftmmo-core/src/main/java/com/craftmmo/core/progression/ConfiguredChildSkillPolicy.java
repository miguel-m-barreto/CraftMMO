package com.craftmmo.core.progression;

import com.craftmmo.api.progression.PlayerSkillProgress;
import com.craftmmo.api.skill.SkillId;
import java.util.List;
import java.util.Map;

public final class ConfiguredChildSkillPolicy implements ChildSkillPolicy {
    private final Map<SkillId, List<SkillId>> parentSkills;

    public ConfiguredChildSkillPolicy(Map<SkillId, List<SkillId>> parentSkills) {
        this.parentSkills = Map.copyOf(parentSkills);
    }

    public static ConfiguredChildSkillPolicy defaultPolicy() {
        return new ConfiguredChildSkillPolicy(Map.of(
                SkillId.SALVAGE, List.of(SkillId.REPAIR, SkillId.FISHING),
                SkillId.SMELTING, List.of(SkillId.MINING, SkillId.REPAIR)
        ));
    }

    @Override
    public int deriveLevel(SkillId childSkill, Map<SkillId, PlayerSkillProgress> progress) {
        List<SkillId> parents = parentSkills.get(childSkill);
        if (parents == null || parents.isEmpty()) {
            throw new IllegalArgumentException("No child skill policy configured for " + childSkill.stableId());
        }
        long total = 0L;
        for (SkillId parent : parents) {
            total = Math.addExact(total, progress.getOrDefault(parent, PlayerSkillProgress.initial(parent)).level());
        }
        return Math.toIntExact(total / parents.size());
    }
}
