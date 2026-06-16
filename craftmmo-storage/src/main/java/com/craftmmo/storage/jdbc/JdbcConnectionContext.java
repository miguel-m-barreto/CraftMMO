package com.craftmmo.storage.jdbc;

import java.sql.Connection;
import java.util.Optional;

public final class JdbcConnectionContext {
    private final ThreadLocal<Connection> current = new ThreadLocal<>();

    public Optional<Connection> current() {
        return Optional.ofNullable(current.get());
    }

    void set(Connection connection) {
        current.set(connection);
    }

    void clear() {
        current.remove();
    }
}
