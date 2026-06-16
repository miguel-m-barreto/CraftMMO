package com.craftmmo.api.skill;

import java.util.Collection;
import java.util.Optional;

public interface SkillRegistry {
    Collection<SkillDefinition> all();

    Optional<SkillDefinition> find(SkillId id);

    default SkillDefinition require(SkillId id) {
        return find(id).orElseThrow(() -> new IllegalArgumentException("Unknown skill: " + id.stableId()));
    }
}
