package com.craftmmo.api.skill;

import java.util.List;
import java.util.Objects;

public record SkillDefinition(
        SkillId id,
        String displayName,
        SkillCategory category,
        boolean enabled,
        boolean childSkill,
        List<SkillId> parentSkills,
        ReferenceStatus referenceStatus
) {
    public SkillDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(parentSkills, "parentSkills");
        Objects.requireNonNull(referenceStatus, "referenceStatus");
        displayName = displayName.trim();
        parentSkills = List.copyOf(parentSkills);
        if (displayName.isBlank()) {
            throw new IllegalArgumentException("Display name must not be blank");
        }
        if (childSkill && parentSkills.isEmpty()) {
            throw new IllegalArgumentException("Child skills must declare parent skills");
        }
    }
}
