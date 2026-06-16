package com.craftmmo.storage.cooldown;

import com.craftmmo.api.cooldown.Cooldown;
import com.craftmmo.api.cooldown.CooldownKey;
import com.craftmmo.api.identity.PlayerId;
import com.craftmmo.api.version.ContentVersion;
import com.craftmmo.api.version.RulesetVersion;
import com.craftmmo.storage.jdbc.JdbcConnectionContext;
import com.craftmmo.storage.jdbc.JdbcStorageException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public final class JdbcCooldownRepository {
    private final JdbcConnectionContext context;

    public JdbcCooldownRepository(JdbcConnectionContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    public boolean save(Cooldown previous, Cooldown next, Instant now) {
        Objects.requireNonNull(previous, "previous");
        Objects.requireNonNull(next, "next");
        Objects.requireNonNull(now, "now");
        if (!previous.key().equals(next.key())) {
            throw new IllegalArgumentException("Cooldown update requires the same key");
        }
        String sql = """
                insert into player_cooldowns (
                    player_uuid, namespace, cooldown_key, starts_at, ends_at, ruleset_version,
                    content_version, version, created_at, updated_at
                )
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (player_uuid, namespace, cooldown_key) do update
                set starts_at = excluded.starts_at,
                    ends_at = excluded.ends_at,
                    ruleset_version = excluded.ruleset_version,
                    content_version = excluded.content_version,
                    version = excluded.version,
                    updated_at = excluded.updated_at
                where player_cooldowns.version = ?
                """;
        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            bindCooldown(statement, next, now);
            statement.setLong(11, previous.version());
            return statement.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw new JdbcStorageException("Failed to save cooldown", ex);
        }
    }

    public Optional<Cooldown> find(CooldownKey key) {
        String sql = """
                select player_uuid, namespace, cooldown_key, starts_at, ends_at,
                       ruleset_version, content_version, version
                from player_cooldowns
                where player_uuid = ? and namespace = ? and cooldown_key = ?
                """;
        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            statement.setObject(1, key.playerId().value());
            statement.setString(2, key.namespace());
            statement.setString(3, key.key());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(readCooldown(resultSet));
            }
        } catch (SQLException ex) {
            throw new JdbcStorageException("Failed to read cooldown", ex);
        }
    }

    public boolean delete(CooldownKey key) {
        String sql = "delete from player_cooldowns where player_uuid = ? and namespace = ? and cooldown_key = ?";
        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            statement.setObject(1, key.playerId().value());
            statement.setString(2, key.namespace());
            statement.setString(3, key.key());
            return statement.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw new JdbcStorageException("Failed to delete cooldown", ex);
        }
    }

    private static void bindCooldown(PreparedStatement statement, Cooldown cooldown, Instant now) throws SQLException {
        statement.setObject(1, cooldown.key().playerId().value());
        statement.setString(2, cooldown.key().namespace());
        statement.setString(3, cooldown.key().key());
        statement.setTimestamp(4, Timestamp.from(cooldown.startsAt()));
        statement.setTimestamp(5, Timestamp.from(cooldown.endsAt()));
        statement.setString(6, cooldown.rulesetVersion().value());
        statement.setString(7, cooldown.contentVersion().value());
        statement.setLong(8, cooldown.version());
        statement.setTimestamp(9, Timestamp.from(now));
        statement.setTimestamp(10, Timestamp.from(now));
    }

    private static Cooldown readCooldown(ResultSet resultSet) throws SQLException {
        return new Cooldown(
                new CooldownKey(
                        PlayerId.of(resultSet.getObject("player_uuid", java.util.UUID.class)),
                        resultSet.getString("namespace"),
                        resultSet.getString("cooldown_key")
                ),
                resultSet.getTimestamp("starts_at").toInstant(),
                resultSet.getTimestamp("ends_at").toInstant(),
                new RulesetVersion(resultSet.getString("ruleset_version")),
                new ContentVersion(resultSet.getString("content_version")),
                resultSet.getLong("version")
        );
    }

    private Connection connection() {
        return context.current().orElseThrow(() -> new IllegalStateException("JDBC operation requires an explicit transaction"));
    }
}
