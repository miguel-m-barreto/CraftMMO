package com.craftmmo.storage.player;

import com.craftmmo.api.identity.PlayerId;
import com.craftmmo.api.profile.PlayerProfile;
import com.craftmmo.api.progression.PlayerSkillProgress;
import com.craftmmo.api.skill.SkillId;
import com.craftmmo.api.version.RulesetVersion;
import com.craftmmo.storage.jdbc.JdbcConnectionContext;
import com.craftmmo.storage.jdbc.JdbcStorageException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class JdbcPlayerRepository {
    private final JdbcConnectionContext context;

    public JdbcPlayerRepository(JdbcConnectionContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    public boolean insert(PlayerProfile profile) {
        if (profile.profileVersion() != 0 || profile.lockVersion() != 0) {
            throw new IllegalArgumentException("New player profiles must start at version 0");
        }
        String sql = """
                insert into players (
                    player_uuid, last_known_name, ruleset_version, profile_version, lock_version,
                    last_login_at, playtime_seconds, created_at, updated_at
                )
                values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (player_uuid) do nothing
                """;
        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            bindProfile(statement, profile);
            return statement.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw new JdbcStorageException("Failed to insert player", ex);
        }
    }

    public boolean update(PlayerProfile previous, PlayerProfile next) {
        if (!previous.playerId().equals(next.playerId())) {
            throw new IllegalArgumentException("Player profile update requires the same player ID");
        }
        validateNextVersions(previous, next);
        String sql = """
                update players
                set last_known_name = ?,
                    ruleset_version = ?,
                    profile_version = ?,
                    lock_version = ?,
                    last_login_at = ?,
                    playtime_seconds = ?,
                    updated_at = ?
                where player_uuid = ? and lock_version = ?
                """;
        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            statement.setString(1, next.lastKnownName());
            statement.setString(2, next.rulesetVersion().value());
            statement.setLong(3, next.profileVersion());
            statement.setLong(4, next.lockVersion());
            statement.setTimestamp(5, Timestamp.from(next.lastLoginAt()));
            statement.setLong(6, next.playtimeSeconds());
            statement.setTimestamp(7, Timestamp.from(next.updatedAt()));
            statement.setObject(8, next.playerId().value());
            statement.setLong(9, previous.lockVersion());
            return statement.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw new JdbcStorageException("Failed to update player", ex);
        }
    }

    public void upsert(PlayerProfile profile) {
        if (!insert(initialForInsert(profile))) {
            PlayerProfile existing = find(profile.playerId()).orElseThrow();
            if (!update(existing, existing.withProfileUpdate(
                    profile.lastKnownName(),
                    profile.rulesetVersion(),
                    profile.lastLoginAt(),
                    profile.playtimeSeconds(),
                    Math.addExact(existing.profileVersion(), 1L),
                    profile.updatedAt()
            ))) {
                throw new JdbcStorageException("Failed to optimistically upsert player", null);
            }
        }
    }

    public Optional<PlayerProfile> find(PlayerId playerId) {
        String sql = """
                select player_uuid, last_known_name, ruleset_version, profile_version, lock_version,
                       last_login_at, playtime_seconds, created_at, updated_at
                from players
                where player_uuid = ?
                """;
        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            statement.setObject(1, playerId.value());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(new PlayerProfile(
                        PlayerId.of(resultSet.getObject("player_uuid", java.util.UUID.class)),
                        resultSet.getString("last_known_name"),
                        new RulesetVersion(resultSet.getString("ruleset_version")),
                        loadProgress(playerId),
                        resultSet.getTimestamp("last_login_at").toInstant(),
                        resultSet.getLong("playtime_seconds"),
                        resultSet.getLong("profile_version"),
                        resultSet.getLong("lock_version"),
                        resultSet.getTimestamp("created_at").toInstant(),
                        resultSet.getTimestamp("updated_at").toInstant()
                ));
            }
        } catch (SQLException ex) {
            throw new JdbcStorageException("Failed to read player", ex);
        }
    }

    private static void bindProfile(PreparedStatement statement, PlayerProfile profile) throws SQLException {
        statement.setObject(1, profile.playerId().value());
        statement.setString(2, profile.lastKnownName());
        statement.setString(3, profile.rulesetVersion().value());
        statement.setLong(4, profile.profileVersion());
        statement.setLong(5, profile.lockVersion());
        statement.setTimestamp(6, Timestamp.from(profile.lastLoginAt()));
        statement.setLong(7, profile.playtimeSeconds());
        statement.setTimestamp(8, Timestamp.from(profile.createdAt()));
        statement.setTimestamp(9, Timestamp.from(profile.updatedAt()));
    }

    private static void validateNextVersions(PlayerProfile previous, PlayerProfile next) {
        long expectedLockVersion = Math.addExact(previous.lockVersion(), 1L);
        long expectedProfileVersion = Math.addExact(previous.profileVersion(), 1L);
        if (next.lockVersion() != expectedLockVersion) {
            throw new IllegalArgumentException("Next lock version must equal previous lock version plus one");
        }
        if (next.profileVersion() != expectedProfileVersion) {
            throw new IllegalArgumentException("Next profile version must equal previous profile version plus one");
        }
    }

    private static PlayerProfile initialForInsert(PlayerProfile profile) {
        return new PlayerProfile(
                profile.playerId(),
                profile.lastKnownName(),
                profile.rulesetVersion(),
                profile.progress(),
                profile.lastLoginAt(),
                profile.playtimeSeconds(),
                0L,
                0L,
                profile.createdAt(),
                profile.updatedAt()
        );
    }

    private Map<SkillId, PlayerSkillProgress> loadProgress(PlayerId playerId) throws SQLException {
        String sql = "select skill_id, level, current_xp, total_xp, version from player_skill_progress where player_uuid = ?";
        Map<SkillId, PlayerSkillProgress> progress = new EnumMap<>(SkillId.class);
        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            statement.setObject(1, playerId.value());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    SkillId skillId = SkillId.fromStableId(resultSet.getString("skill_id"));
                    progress.put(skillId, new PlayerSkillProgress(
                            skillId,
                            resultSet.getInt("level"),
                            resultSet.getLong("current_xp"),
                            resultSet.getLong("total_xp"),
                            resultSet.getLong("version")
                    ));
                }
            }
        }
        return progress;
    }

    private Connection connection() {
        return context.current().orElseThrow(() -> new IllegalStateException("JDBC operation requires an explicit transaction"));
    }
}
