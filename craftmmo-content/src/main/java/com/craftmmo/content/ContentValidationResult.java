package com.craftmmo.content;

import java.util.List;

public record ContentValidationResult(boolean valid, List<String> errors) {
    public ContentValidationResult {
        errors = List.copyOf(errors);
    }

    public static ContentValidationResult ok() {
        return new ContentValidationResult(true, List.of());
    }

    public static ContentValidationResult failed(List<String> errors) {
        return new ContentValidationResult(false, errors);
    }
}
