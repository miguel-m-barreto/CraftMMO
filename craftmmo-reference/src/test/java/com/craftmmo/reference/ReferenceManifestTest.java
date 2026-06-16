package com.craftmmo.reference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class ReferenceManifestTest {
    @Test
    void validatesBundledManifest() {
        ReferenceManifest manifest = new ReferenceManifestLoader().load();

        ReferenceManifestValidationResult result = new ReferenceManifestValidator().validate(manifest);

        assertTrue(result.valid(), result.diagnostics().toString());
    }

    @Test
    void rejectsDuplicatesAndUnknownStatusesDeterministically() {
        String json = """
                {
                  "product": "mcMMO",
                  "version": "2.2.053",
                  "freezeDate": "2026-06-16",
                  "targetRuleset": "craftmmo-1.0.0",
                  "defaultStatus": "BAD",
                  "skills": [
                    {"id": "mining", "mechanics": "NEEDS_REVIEW"},
                    {"id": "mining", "mechanics": "BAD"}
                  ]
                }
                """;

        ReferenceManifest manifest = new ReferenceManifestParser().parse(json);
        ReferenceManifestValidationResult result = new ReferenceManifestValidator().validate(manifest);

        assertFalse(result.valid());
        assertTrue(result.diagnostics().contains("Duplicate skill: mining"));
        assertTrue(result.diagnostics().contains("Unknown status at defaultStatus: BAD"));
        assertTrue(result.diagnostics().contains("Unknown status at skill.mining.mechanics: BAD"));
    }

    @Test
    void rejectsHeaderMismatches() {
        assertDiagnostic(manifestJson("Other", "2.2.053", "2026-06-16", "craftmmo-1.0.0"), "Unexpected product");
        assertDiagnostic(manifestJson("mcMMO", "2.2.052", "2026-06-16", "craftmmo-1.0.0"), "Unexpected version");
        assertDiagnostic(manifestJson("mcMMO", "2.2.053", "2026-06-15", "craftmmo-1.0.0"), "Unexpected freezeDate");
        assertDiagnostic(manifestJson("mcMMO", "2.2.053", "2026-06-16", "other"), "Unexpected targetRuleset");
    }

    @Test
    void parserRejectsUnknownFields() {
        String json = """
                {
                  "product": "mcMMO",
                  "version": "2.2.053",
                  "freezeDate": "2026-06-16",
                  "targetRuleset": "craftmmo-1.0.0",
                  "defaultStatus": "NEEDS_REVIEW",
                  "unexpected": true,
                  "skills": []
                }
                """;

        assertThrows(ReferenceManifestParseException.class, () -> new ReferenceManifestParser().parse(json));
    }

    private static void assertDiagnostic(String json, String expectedPrefix) {
        ReferenceManifest manifest = new ReferenceManifestParser().parse(json);
        ReferenceManifestValidationResult result = new ReferenceManifestValidator().validate(manifest);

        assertFalse(result.valid());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic -> diagnostic.startsWith(expectedPrefix)), result.diagnostics().toString());
    }

    private static String manifestJson(String product, String version, String freezeDate, String targetRuleset) {
        return """
                {
                  "product": "%s",
                  "version": "%s",
                  "freezeDate": "%s",
                  "targetRuleset": "%s",
                  "defaultStatus": "NEEDS_REVIEW",
                  "skills": [
                    {"id": "acrobatics", "mechanics": "NEEDS_REVIEW"},
                    {"id": "alchemy", "mechanics": "NEEDS_REVIEW"},
                    {"id": "archery", "mechanics": "NEEDS_REVIEW"},
                    {"id": "axes", "mechanics": "NEEDS_REVIEW"},
                    {"id": "crossbows", "mechanics": "NEEDS_REVIEW"},
                    {"id": "excavation", "mechanics": "NEEDS_REVIEW"},
                    {"id": "fishing", "mechanics": "NEEDS_REVIEW"},
                    {"id": "herbalism", "mechanics": "NEEDS_REVIEW"},
                    {"id": "maces", "mechanics": "NEEDS_REVIEW"},
                    {"id": "mining", "mechanics": "NEEDS_REVIEW"},
                    {"id": "repair", "mechanics": "NEEDS_REVIEW"},
                    {"id": "salvage", "mechanics": "NEEDS_REVIEW"},
                    {"id": "smelting", "mechanics": "NEEDS_REVIEW"},
                    {"id": "spears", "mechanics": "NEEDS_REVIEW"},
                    {"id": "swords", "mechanics": "NEEDS_REVIEW"},
                    {"id": "taming", "mechanics": "NEEDS_REVIEW"},
                    {"id": "tridents", "mechanics": "NEEDS_REVIEW"},
                    {"id": "unarmed", "mechanics": "NEEDS_REVIEW"},
                    {"id": "woodcutting", "mechanics": "NEEDS_REVIEW"}
                  ]
                }
                """.formatted(product, version, freezeDate, targetRuleset);
    }
}
