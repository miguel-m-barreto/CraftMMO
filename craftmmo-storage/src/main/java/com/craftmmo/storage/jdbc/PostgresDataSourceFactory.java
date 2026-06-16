package com.craftmmo.storage.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.time.Duration;
import java.util.Objects;

public final class PostgresDataSourceFactory {
    public HikariDataSource create(PostgresSettings settings) {
        Objects.requireNonNull(settings, "settings");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(settings.jdbcUrl());
        config.setUsername(settings.username());
        config.setPassword(settings.password());
        config.setMaximumPoolSize(settings.maximumPoolSize());
        config.setConnectionTimeout(settings.connectionTimeout().toMillis());
        config.setPoolName("CraftMMO-PostgreSQL");
        config.addDataSourceProperty("ApplicationName", "CraftMMO");
        return new HikariDataSource(config);
    }

    public record PostgresSettings(
            String jdbcUrl,
            String username,
            String password,
            int maximumPoolSize,
            Duration connectionTimeout
    ) {
        public PostgresSettings {
            Objects.requireNonNull(jdbcUrl, "jdbcUrl");
            Objects.requireNonNull(username, "username");
            Objects.requireNonNull(password, "password");
            Objects.requireNonNull(connectionTimeout, "connectionTimeout");
            if (maximumPoolSize < 1) {
                throw new IllegalArgumentException("Maximum pool size must be positive");
            }
        }
    }
}
