package com.craftmmo.paper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;

final class DatabaseSettingsResolverTest {
    @Test
    void environmentOverridesConfigValues() {
        var settings = DatabaseSettingsResolver.resolve(config(), Map.of(
                "CRAFTMMO_DATABASE_JDBC_URL", "jdbc:postgresql://127.0.0.1:15432/env",
                "CRAFTMMO_DATABASE_USERNAME", "env-user",
                "CRAFTMMO_DATABASE_PASSWORD", "env-password",
                "CRAFTMMO_DATABASE_MAXIMUM_POOL_SIZE", "8",
                "CRAFTMMO_DATABASE_CONNECTION_TIMEOUT_MS", "7000"
        ));

        assertEquals("jdbc:postgresql://127.0.0.1:15432/env", settings.jdbcUrl());
        assertEquals("env-user", settings.username());
        assertEquals("env-password", settings.password());
        assertEquals(8, settings.maximumPoolSize());
        assertEquals(Duration.ofMillis(7000), settings.connectionTimeout());
    }

    @Test
    void settingsSnapshotKeepsResolvedValues() {
        java.util.Map<String, String> environment = new java.util.HashMap<>();
        MutableConfig config = new MutableConfig("jdbc:postgresql://localhost:5432/first");
        CraftMmoSettings snapshot = new CraftMmoSettings(DatabaseSettingsResolver.resolve(config, environment));

        config.jdbcUrl = "jdbc:postgresql://localhost:5432/second";
        environment.put("CRAFTMMO_DATABASE_JDBC_URL", "jdbc:postgresql://localhost:5432/env");

        assertEquals("jdbc:postgresql://localhost:5432/first", snapshot.database().jdbcUrl());
    }

    @Test
    void invalidEnvironmentNumberFailsAsConfiguration() {
        CraftMmoStartupException ex = assertThrows(CraftMmoStartupException.class, () ->
                DatabaseSettingsResolver.resolve(config(), Map.of("CRAFTMMO_DATABASE_MAXIMUM_POOL_SIZE", "abc")));

        assertEquals(StartupFailureCategory.CONFIGURATION, ex.category());
    }

    @Test
    void rejectsInvalidDatabaseSettingsBeforeAsyncStartup() {
        assertConfigurationFailure(blankJdbcUrlConfig(), Map.of());
        assertConfigurationFailure(blankUsernameConfig(), Map.of());
        assertConfigurationFailure(Map.of("CRAFTMMO_DATABASE_MAXIMUM_POOL_SIZE", "0"));
        assertConfigurationFailure(Map.of("CRAFTMMO_DATABASE_CONNECTION_TIMEOUT_MS", "0"));
    }

    private static void assertConfigurationFailure(Map<String, String> environment) {
        assertConfigurationFailure(config(), environment);
    }

    private static void assertConfigurationFailure(DatabaseSettingsResolver.ConfigSource config, Map<String, String> environment) {
        CraftMmoStartupException ex = assertThrows(CraftMmoStartupException.class, () ->
                DatabaseSettingsResolver.resolve(config, environment));
        assertEquals(StartupFailureCategory.CONFIGURATION, ex.category());
    }

    private static DatabaseSettingsResolver.ConfigSource config() {
        return new DatabaseSettingsResolver.ConfigSource() {
            @Override
            public String stringValue(String path, String defaultValue) {
                return switch (path) {
                    case "database.jdbc-url" -> "jdbc:postgresql://localhost:5432/config";
                    case "database.username" -> "config-user";
                    case "database.password" -> "config-password";
                    default -> defaultValue;
                };
            }

            @Override
            public int intValue(String path, int defaultValue) {
                return 3;
            }

            @Override
            public long longValue(String path, long defaultValue) {
                return 4000L;
            }
        };
    }

    private static DatabaseSettingsResolver.ConfigSource blankJdbcUrlConfig() {
        return new MutableConfig(" ");
    }

    private static DatabaseSettingsResolver.ConfigSource blankUsernameConfig() {
        return new DatabaseSettingsResolver.ConfigSource() {
            @Override
            public String stringValue(String path, String defaultValue) {
                return switch (path) {
                    case "database.jdbc-url" -> "jdbc:postgresql://localhost:5432/config";
                    case "database.username" -> " ";
                    case "database.password" -> "config-password";
                    default -> defaultValue;
                };
            }

            @Override
            public int intValue(String path, int defaultValue) {
                return 3;
            }

            @Override
            public long longValue(String path, long defaultValue) {
                return 4000L;
            }
        };
    }

    private static final class MutableConfig implements DatabaseSettingsResolver.ConfigSource {
        private String jdbcUrl;

        private MutableConfig(String jdbcUrl) {
            this.jdbcUrl = jdbcUrl;
        }

        @Override
        public String stringValue(String path, String defaultValue) {
            return switch (path) {
                case "database.jdbc-url" -> jdbcUrl;
                case "database.username" -> "config-user";
                case "database.password" -> "config-password";
                default -> defaultValue;
            };
        }

        @Override
        public int intValue(String path, int defaultValue) {
            return 3;
        }

        @Override
        public long longValue(String path, long defaultValue) {
            return 4000L;
        }
    }
}
