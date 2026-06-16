package com.craftmmo.api.identity;

import java.util.Locale;
import java.util.Objects;

public record PayloadHash(String value) {
    private static final int SHA256_HEX_LENGTH = 64;

    public PayloadHash {
        Objects.requireNonNull(value, "value");
        value = value.trim().toLowerCase(Locale.ROOT);
        if (value.length() != SHA256_HEX_LENGTH || !value.matches("[0-9a-f]+")) {
            throw new IllegalArgumentException("Payload hash must be a SHA-256 hex value");
        }
    }
}
