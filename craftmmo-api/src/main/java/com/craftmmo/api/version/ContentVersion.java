package com.craftmmo.api.version;

import java.util.Objects;

public record ContentVersion(String value) {
    public ContentVersion {
        Objects.requireNonNull(value, "value");
        value = value.trim();
        if (value.isBlank()) {
            throw new IllegalArgumentException("Content version must not be blank");
        }
    }
}
