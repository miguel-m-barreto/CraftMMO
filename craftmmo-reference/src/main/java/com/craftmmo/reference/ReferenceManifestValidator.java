package com.craftmmo.reference;

import com.craftmmo.api.skill.ReferenceStatus;
import com.craftmmo.api.skill.SkillId;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public final class ReferenceManifestValidator {
    private static final String EXPECTED_PRODUCT = "mcMMO";
    private static final String EXPECTED_VERSION = "2.2.053";
    private static final String EXPECTED_FREEZE_DATE = "2026-06-16";
    private static final String EXPECTED_TARGET_RULESET = "craftmmo-1.0.0";

    public ReferenceManifestValidationResult validate(ReferenceManifest manifest) {
        List<String> diagnostics = new ArrayList<>();
        validateEquals("product", EXPECTED_PRODUCT, manifest.product(), diagnostics);
        validateEquals("version", EXPECTED_VERSION, manifest.version(), diagnostics);
        validateEquals("freezeDate", EXPECTED_FREEZE_DATE, manifest.freezeDate(), diagnostics);
        validateEquals("targetRuleset", EXPECTED_TARGET_RULESET, manifest.targetRuleset(), diagnostics);
        validateStatus("defaultStatus", manifest.defaultStatus(), diagnostics);
        EnumSet<SkillId> seen = EnumSet.noneOf(SkillId.class);
        for (ReferenceSkillEntry skill : manifest.skills()) {
            SkillId skillId = parseSkill(skill.id(), diagnostics);
            validateStatus("skill." + skill.id() + ".mechanics", skill.mechanics(), diagnostics);
            if (skillId != null && !seen.add(skillId)) {
                diagnostics.add("Duplicate skill: " + skillId.stableId());
            }
        }
        for (SkillId skillId : SkillId.values()) {
            if (!seen.contains(skillId)) {
                diagnostics.add("Missing skill: " + skillId.stableId());
            }
        }
        diagnostics.sort(String::compareTo);
        return new ReferenceManifestValidationResult(diagnostics.isEmpty(), diagnostics);
    }

    private static SkillId parseSkill(String id, List<String> diagnostics) {
        try {
            return SkillId.fromStableId(id);
        } catch (IllegalArgumentException ex) {
            diagnostics.add("Unknown skill: " + id);
            return null;
        }
    }

    private static void validateStatus(String field, String status, List<String> diagnostics) {
        try {
            ReferenceStatus.valueOf(status);
        } catch (IllegalArgumentException ex) {
            diagnostics.add("Unknown status at " + field + ": " + status);
        }
    }

    private static void validateEquals(String field, String expected, String actual, List<String> diagnostics) {
        if (!expected.equals(actual)) {
            diagnostics.add("Unexpected " + field + ": expected " + expected + " but was " + actual);
        }
    }
}
