package com.craftmmo.paper;

import com.craftmmo.storage.jdbc.PostgresDataSourceFactory.PostgresSettings;
import java.util.Objects;

public record CraftMmoSettings(PostgresSettings database) {
    public CraftMmoSettings {
        Objects.requireNonNull(database, "database");
    }
}
