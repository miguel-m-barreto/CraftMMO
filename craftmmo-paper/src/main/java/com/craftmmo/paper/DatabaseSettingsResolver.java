package com.craftmmo.paper;

import com.craftmmo.storage.jdbc.PostgresDataSourceFactory.PostgresSettings;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

public final class DatabaseSettingsResolver {
    public interface ConfigSource {
        String stringValue(String path, String defaultValue);

        int intValue(String path, int defaultValue);

        long longValue(String path, long defaultValue);
    }

    private DatabaseSettingsResolver() {
    }

    public static PostgresSettings resolve(ConfigSource config, Map<String, String> environment) {
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(environment, "environment");
        String jdbcUrl = firstPresent(environment, "CRAFTMMO_DATABASE_JDBC_URL",
                config.stringValue("database.jdbc-url", "jdbc:postgresql://localhost:5432/craftmmo"));
        String username = firstPresent(environment, "CRAFTMMO_DATABASE_USERNAME",
                config.stringValue("database.username", "craftmmo"));
        String password = firstPresent(environment, "CRAFTMMO_DATABASE_PASSWORD",
                config.stringValue("database.password", "craftmmo"));
        int maximumPoolSize = parseInt(firstPresent(environment, "CRAFTMMO_DATABASE_MAXIMUM_POOL_SIZE",
                Integer.toString(config.intValue("database.maximum-pool-size", 5))));
        long connectionTimeoutMs = parseLong(firstPresent(environment, "CRAFTMMO_DATABASE_CONNECTION_TIMEOUT_MS",
                Long.toString(config.longValue("database.connection-timeout-ms", 5000L))));
        if (jdbcUrl.isBlank()) {
            throw new CraftMmoStartupException(StartupFailureCategory.CONFIGURATION, "PostgreSQL JDBC URL must not be blank", null);
        }
        if (username.isBlank()) {
            throw new CraftMmoStartupException(StartupFailureCategory.CONFIGURATION, "PostgreSQL username must not be blank", null);
        }
        if (maximumPoolSize < 1) {
            throw new CraftMmoStartupException(StartupFailureCategory.CONFIGURATION, "PostgreSQL pool size must be positive", null);
        }
        if (connectionTimeoutMs < 1) {
            throw new CraftMmoStartupException(StartupFailureCategory.CONFIGURATION, "PostgreSQL connection timeout must be positive", null);
        }
        return new PostgresSettings(jdbcUrl, username, password, maximumPoolSize, Duration.ofMillis(connectionTimeoutMs));
    }

    private static String firstPresent(Map<String, String> environment, String key, String fallback) {
        String value = environment.get(key);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private static int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new CraftMmoStartupException(StartupFailureCategory.CONFIGURATION, "Invalid integer setting", ex);
        }
    }

    private static long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new CraftMmoStartupException(StartupFailureCategory.CONFIGURATION, "Invalid long setting", ex);
        }
    }
}
