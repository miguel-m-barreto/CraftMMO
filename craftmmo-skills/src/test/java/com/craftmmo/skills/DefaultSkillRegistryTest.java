package com.craftmmo.skills;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.craftmmo.api.skill.ReferenceStatus;
import com.craftmmo.api.skill.SkillCategory;
import com.craftmmo.api.skill.SkillDefinition;
import com.craftmmo.api.skill.SkillId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

final class DefaultSkillRegistryTest {
    @Test
    void containsAllSkillsExactlyOnce() {
        DefaultSkillRegistry registry = DefaultSkillRegistry.create();

        assertEquals(19, registry.all().size());
        assertTrue(registry.require(SkillId.SALVAGE).childSkill());
        assertEquals(List.of(SkillId.REPAIR, SkillId.FISHING), registry.require(SkillId.SALVAGE).parentSkills());
        assertEquals(List.of(SkillId.MINING, SkillId.REPAIR), registry.require(SkillId.SMELTING).parentSkills());
    }

    @Test
    void rejectsDuplicateDefinitions() {
        List<SkillDefinition> definitions = new ArrayList<>(DefaultSkillRegistry.create().all());
        definitions.add(new SkillDefinition(SkillId.MINING, "Mining copy", SkillCategory.GATHERING, true, false, List.of(), ReferenceStatus.NEEDS_REVIEW));

        assertThrows(IllegalArgumentException.class, () -> DefaultSkillRegistry.fromDefinitions(definitions));
    }
}
