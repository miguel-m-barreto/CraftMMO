package com.craftmmo.storage.migration;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;

public final class FlywayMigrator {
    public void migrate(DataSource dataSource) {
        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }
}
