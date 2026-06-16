package com.craftmmo.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.craftmmo.api.skill.SkillCategory;
import com.craftmmo.api.skill.SkillDefinition;
import com.craftmmo.api.skill.SkillId;
import com.craftmmo.api.skill.ReferenceStatus;
import com.craftmmo.api.version.ContentVersion;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

final class ContentSnapshotValidatorTest {
    @Test
    void acceptsCompleteSnapshot() {
        ContentValidationResult result = new ContentSnapshotValidator().validate(snapshotWithAllSkills());

        assertTrue(result.valid());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void rejectsDuplicateAndMissingSkillsDeterministically() {
        List<SkillDefinition> skills = new ArrayList<>(snapshotWithAllSkills().skills());
        skills.remove(skills.size() - 1);
        skills.add(skills.get(0));

        ContentValidationResult result = new ContentSnapshotValidator().validate(snapshot(skills));

        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(error -> error.contains("Duplicate skill definition")));
        assertTrue(result.errors().stream().anyMatch(error -> error.contains("Missing skill definition")));
    }

    @Test
    void activatorKeepsPreviousSnapshotWhenCandidateIsInvalid() {
        AtomicContentActivator activator = new AtomicContentActivator(new ContentSnapshotValidator());
        ContentSnapshot valid = snapshotWithAllSkills();
        assertTrue(activator.activate(valid).valid());

        ContentSnapshot invalid = snapshot(List.of(valid.skills().get(0)));
        assertFalse(activator.activate(invalid).valid());

        assertEquals(valid, activator.active().orElseThrow());
    }

    private static ContentSnapshot snapshotWithAllSkills() {
        List<SkillDefinition> definitions = new ArrayList<>();
        for (SkillId skillId : SkillId.values()) {
            definitions.add(new SkillDefinition(
                    skillId,
                    skillId.stableId(),
                    SkillCategory.GATHERING,
                    true,
                    false,
                    List.of(),
                    ReferenceStatus.NEEDS_REVIEW
            ));
        }
        return snapshot(definitions);
    }

    private static ContentSnapshot snapshot(List<SkillDefinition> definitions) {
        return new ContentSnapshot(new ContentVersion("test"), definitions, Instant.parse("2026-06-16T00:00:00Z"));
    }
}
