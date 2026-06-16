package com.craftmmo.reference;

import java.util.List;
import java.util.Objects;

public record ReferenceManifest(
        String product,
        String version,
        String freezeDate,
        String targetRuleset,
        String defaultStatus,
        List<ReferenceSkillEntry> skills
) {
    public ReferenceManifest {
        Objects.requireNonNull(product, "product");
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(freezeDate, "freezeDate");
        Objects.requireNonNull(targetRuleset, "targetRuleset");
        Objects.requireNonNull(defaultStatus, "defaultStatus");
        Objects.requireNonNull(skills, "skills");
        product = product.trim();
        version = version.trim();
        freezeDate = freezeDate.trim();
        targetRuleset = targetRuleset.trim();
        defaultStatus = defaultStatus.trim();
        skills = List.copyOf(skills);
        if (product.isBlank() || version.isBlank() || freezeDate.isBlank() || targetRuleset.isBlank() || defaultStatus.isBlank()) {
            throw new IllegalArgumentException("Reference manifest identifiers must not be blank");
        }
    }
}
