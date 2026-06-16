package com.craftmmo.paper;

public final class CraftMmoStartupException extends RuntimeException {
    private final StartupFailureCategory category;

    public CraftMmoStartupException(StartupFailureCategory category, String message, Throwable cause) {
        super(message, cause);
        this.category = category;
    }

    public StartupFailureCategory category() {
        return category;
    }
}
