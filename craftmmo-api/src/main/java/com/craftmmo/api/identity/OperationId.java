package com.craftmmo.api.identity;

import java.util.Objects;

public record OperationId(String value) {
    public OperationId {
        Objects.requireNonNull(value, "value");
        value = value.trim();
        if (value.isBlank()) {
            throw new IllegalArgumentException("Operation ID must not be blank");
        }
    }
}
