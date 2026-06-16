package com.craftmmo.api.version;

import java.util.Objects;

public record PluginVersion(String value) {
    public PluginVersion {
        Objects.requireNonNull(value, "value");
        value = value.trim();
        if (value.isBlank()) {
            throw new IllegalArgumentException("Plugin version must not be blank");
        }
    }
}
