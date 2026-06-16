package com.craftmmo.paper;

import com.craftmmo.storage.jdbc.PostgresDataSourceFactory;
import com.craftmmo.storage.jdbc.PostgresDataSourceFactory.PostgresSettings;
import com.craftmmo.storage.migration.FlywayMigrator;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class CraftMmoPlugin extends JavaPlugin {
    private volatile CraftMmoBootstrap bootstrap;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        registerCommands();
        CraftMmoSettings settings = loadSettingsSnapshot();
        ExecutorService databaseExecutor = Executors.newSingleThreadExecutor(task -> {
            Thread thread = new Thread(task, "CraftMMO-Database-Startup");
            thread.setDaemon(true);
            return thread;
        });
        bootstrap = new CraftMmoBootstrap(databaseExecutor, () -> initializeDatabase(settings));
        bootstrap.start();
    }

    @Override
    public void onDisable() {
        CraftMmoBootstrap current = bootstrap;
        if (current != null) {
            current.stop(Duration.ofSeconds(10));
        }
    }

    public PluginLifecycleState lifecycleState() {
        CraftMmoBootstrap current = bootstrap;
        return current == null ? PluginLifecycleState.NEW : current.state();
    }

    public StartupFailure startupFailure() {
        CraftMmoBootstrap current = bootstrap;
        return current == null ? StartupFailure.none() : current.failure();
    }

    private void registerCommands() {
        PluginCommand command = Objects.requireNonNull(getCommand("craftmmo"), "craftmmo command missing from plugin.yml");
        command.setExecutor(new CraftMmoRootCommand(this));
    }

    private CraftMmoSettings loadSettingsSnapshot() {
        PostgresSettings settings = DatabaseSettingsResolver.resolve(new DatabaseSettingsResolver.ConfigSource() {
            @Override
            public String stringValue(String path, String defaultValue) {
                return getConfig().getString(path, defaultValue);
            }

            @Override
            public int intValue(String path, int defaultValue) {
                return getConfig().getInt(path, defaultValue);
            }

            @Override
            public long longValue(String path, long defaultValue) {
                return getConfig().getLong(path, defaultValue);
            }
        }, System.getenv());
        return new CraftMmoSettings(settings);
    }

    private AutoCloseable initializeDatabase(CraftMmoSettings settings) {
        PostgresSettings databaseSettings = settings.database();
        HikariDataSource created;
        try {
            created = new PostgresDataSourceFactory().create(databaseSettings);
        } catch (RuntimeException ex) {
            throw new CraftMmoStartupException(StartupFailureCategory.CONFIGURATION, "Invalid database configuration", ex);
        }
        try {
            new FlywayMigrator().migrate(created);
        } catch (RuntimeException ex) {
            created.close();
            throw new CraftMmoStartupException(StartupFailureCategory.MIGRATION, "Database migration failed", ex);
        }
        try (Connection ignored = created.getConnection()) {
            return created;
        } catch (Exception ex) {
            created.close();
            throw new CraftMmoStartupException(StartupFailureCategory.HEALTH_CHECK, "Database health check failed", ex);
        }
    }
}
