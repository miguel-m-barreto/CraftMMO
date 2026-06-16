package com.craftmmo.storage.jdbc;

public final class JdbcStorageException extends RuntimeException {
    public JdbcStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
