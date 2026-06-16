package com.craftmmo.reference;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;

public final class ReferenceManifestParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReferenceManifest parse(String json) {
        try {
            ManifestJson parsed = objectMapper.readValue(json, ManifestJson.class);
            return new ReferenceManifest(
                    parsed.product(),
                    parsed.version(),
                    parsed.freezeDate(),
                    parsed.targetRuleset(),
                    parsed.defaultStatus(),
                    parsed.skills().stream()
                            .map(skill -> new ReferenceSkillEntry(skill.id(), skill.mechanics()))
                            .toList()
            );
        } catch (IOException | RuntimeException ex) {
            throw new ReferenceManifestParseException("Failed to parse reference manifest", ex);
        }
    }

    private record ManifestJson(String product, String version, String freezeDate, String targetRuleset, String defaultStatus, List<SkillJson> skills) {
        private ManifestJson {
            skills = skills == null ? List.of() : skills;
        }
    }

    private record SkillJson(String id, String mechanics) {
    }
}
