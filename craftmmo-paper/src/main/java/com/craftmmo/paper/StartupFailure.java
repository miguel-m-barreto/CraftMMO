package com.craftmmo.paper;

public record StartupFailure(
        StartupFailureCategory category,
        String sanitizedMessage,
        String detail
) {
    public StartupFailure {
        if (category == null) {
            throw new IllegalArgumentException("category is required");
        }
        sanitizedMessage = sanitizedMessage == null ? "" : sanitizedMessage.trim();
        detail = detail == null ? "" : detail.trim();
    }

    public static StartupFailure none() {
        return new StartupFailure(StartupFailureCategory.NONE, "", "");
    }
}
