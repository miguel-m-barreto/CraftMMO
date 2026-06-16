package com.craftmmo.reference;

import java.util.Objects;

public record ReferenceSkillEntry(String id, String mechanics) {
    public ReferenceSkillEntry {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(mechanics, "mechanics");
        id = id.trim();
        mechanics = mechanics.trim();
        if (id.isBlank() || mechanics.isBlank()) {
            throw new IllegalArgumentException("Reference skill entries must not be blank");
        }
    }
}
