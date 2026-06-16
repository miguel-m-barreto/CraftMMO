package com.craftmmo.api.version;

import java.util.Objects;

public record RulesetVersion(String value) {
    public RulesetVersion {
        Objects.requireNonNull(value, "value");
        value = value.trim();
        if (value.isBlank()) {
            throw new IllegalArgumentException("Ruleset version must not be blank");
        }
    }
}
