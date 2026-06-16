package com.craftmmo.content;

import com.craftmmo.api.skill.SkillDefinition;
import com.craftmmo.api.skill.SkillId;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public final class ContentSnapshotValidator {
    public ContentValidationResult validate(ContentSnapshot snapshot) {
        List<String> errors = new ArrayList<>();
        EnumSet<SkillId> seen = EnumSet.noneOf(SkillId.class);
        for (SkillDefinition skill : snapshot.skills()) {
            if (!seen.add(skill.id())) {
                errors.add("Duplicate skill definition: " + skill.id().stableId());
            }
        }
        for (SkillId skillId : SkillId.values()) {
            if (!seen.contains(skillId)) {
                errors.add("Missing skill definition: " + skillId.stableId());
            }
        }
        return errors.isEmpty() ? ContentValidationResult.ok() : ContentValidationResult.failed(errors);
    }
}
