package com.craftmmo.storage.progression;

import com.craftmmo.api.identity.OperationId;
import com.craftmmo.api.identity.PlayerId;
import com.craftmmo.api.progression.PlayerSkillProgress;
import com.craftmmo.api.skill.SkillId;
import com.craftmmo.core.progression.OperationRecordResult;
import com.craftmmo.core.progression.ProgressionRepository;
import com.craftmmo.storage.jdbc.JdbcConnectionContext;
import com.craftmmo.storage.jdbc.JdbcStorageException;
import com.craftmmo.core.progression.SkillProgressionRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public final class JdbcProgressionRepository implements ProgressionRepository {
    private final JdbcConnectionContext context;

    public JdbcProgressionRepository(JdbcConnectionContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    @Override
    public OperationRecordResult recordOperationStart(SkillProgressionRequest request, Instant now) {
        String sql = """
                insert into progression_operations (
                    operation_id, player_uuid, skill_id, operation_type, source, payload_hash, xp, status, created_at, updated_at
                )
                values (?, ?, ?, ?, ?, ?, ?, 'STARTED', ?, ?)
                on conflict (operation_id) do nothing
                """;
        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            statement.setString(1, request.operationId().value());
            statement.setObject(2, request.playerId().value());
            statement.setString(3, request.skillId().stableId());
            statement.setString(4, request.operationType().name());
            statement.setString(5, request.source());
            statement.setString(6, request.payloadHash().value());
            statement.setLong(7, request.xp());
            statement.setTimestamp(8, Timestamp.from(now));
            statement.setTimestamp(9, Timestamp.from(now));
            if (statement.executeUpdate() == 1) {
                return OperationRecordResult.started();
            }
            return compareExistingOperation(request);
        } catch (SQLException ex) {
            throw new JdbcStorageException("Failed to record progression operation", ex);
        }
    }

    private OperationRecordResult compareExistingOperation(SkillProgressionRequest request) throws SQLException {
        String sql = """
                select player_uuid, skill_id, operation_type, source, payload_hash, xp, result_level, result_current_xp,
                       result_total_xp, result_version
                from progression_operations
                where operation_id = ?
                """;
        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            statement.setString(1, request.operationId().value());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return OperationRecordResult.identityConflict();
                }
                boolean same = request.playerId().value().equals(resultSet.getObject("player_uuid", java.util.UUID.class))
                        && request.skillId().stableId().equals(resultSet.getString("skill_id"))
                        && request.operationType().name().equals(resultSet.getString("operation_type"))
                        && request.source().equals(resultSet.getString("source"))
                        && request.xp() == resultSet.getLong("xp")
                        && request.payloadHash().value().equals(resultSet.getString("payload_hash").trim());
                if (!same) {
                    return OperationRecordResult.identityConflict();
                }
                Integer level = (Integer) resultSet.getObject("result_level");
                if (level == null) {
                    return OperationRecordResult.duplicateReplay(Optional.empty());
                }
                return OperationRecordResult.duplicateReplay(Optional.of(new PlayerSkillProgress(
                        request.skillId(),
                        level,
                        resultSet.getLong("result_current_xp"),
                        resultSet.getLong("result_total_xp"),
                        resultSet.getLong("result_version")
                )));
            }
        }
    }

    @Override
    public Optional<PlayerSkillProgress> findForUpdate(PlayerId playerId, SkillId skillId) {
        String sql = """
                select skill_id, level, current_xp, total_xp, version
                from player_skill_progress
                where player_uuid = ? and skill_id = ?
                for update
                """;
        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            statement.setObject(1, playerId.value());
            statement.setString(2, skillId.stableId());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(new PlayerSkillProgress(
                        SkillId.fromStableId(resultSet.getString("skill_id")),
                        resultSet.getInt("level"),
                        resultSet.getLong("current_xp"),
                        resultSet.getLong("total_xp"),
                        resultSet.getLong("version")
                ));
            }
        } catch (SQLException ex) {
            throw new JdbcStorageException("Failed to read skill progress", ex);
        }
    }

    @Override
    public boolean save(PlayerId playerId, PlayerSkillProgress previous, PlayerSkillProgress next, Instant now) {
        if (previous.version() == 0) {
            return insertOrUpdateInitial(playerId, previous, next, now);
        }
        String sql = """
                update player_skill_progress
                set level = ?, current_xp = ?, total_xp = ?, version = ?, updated_at = ?
                where player_uuid = ? and skill_id = ? and version = ?
                """;
        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            statement.setInt(1, next.level());
            statement.setLong(2, next.currentXp());
            statement.setLong(3, next.totalXp());
            statement.setLong(4, next.version());
            statement.setTimestamp(5, Timestamp.from(now));
            statement.setObject(6, playerId.value());
            statement.setString(7, next.skillId().stableId());
            statement.setLong(8, previous.version());
            return statement.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw new JdbcStorageException("Failed to update skill progress", ex);
        }
    }

    private boolean insertOrUpdateInitial(PlayerId playerId, PlayerSkillProgress previous, PlayerSkillProgress next, Instant now) {
        String sql = """
                insert into player_skill_progress (player_uuid, skill_id, level, current_xp, total_xp, version, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (player_uuid, skill_id) do update
                set level = excluded.level,
                    current_xp = excluded.current_xp,
                    total_xp = excluded.total_xp,
                    version = excluded.version,
                    updated_at = excluded.updated_at
                where player_skill_progress.version = ?
                """;
        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            statement.setObject(1, playerId.value());
            statement.setString(2, next.skillId().stableId());
            statement.setInt(3, next.level());
            statement.setLong(4, next.currentXp());
            statement.setLong(5, next.totalXp());
            statement.setLong(6, next.version());
            statement.setTimestamp(7, Timestamp.from(now));
            statement.setTimestamp(8, Timestamp.from(now));
            statement.setLong(9, previous.version());
            return statement.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw new JdbcStorageException("Failed to insert skill progress", ex);
        }
    }

    @Override
    public void markOperationApplied(OperationId operationId, PlayerSkillProgress result, Instant now) {
        String sql = """
                update progression_operations
                set status = 'APPLIED',
                    result_level = ?,
                    result_current_xp = ?,
                    result_total_xp = ?,
                    result_version = ?,
                    updated_at = ?,
                    applied_at = ?
                where operation_id = ?
                  and status = 'STARTED'
                """;
        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            statement.setInt(1, result.level());
            statement.setLong(2, result.currentXp());
            statement.setLong(3, result.totalXp());
            statement.setLong(4, result.version());
            statement.setTimestamp(5, Timestamp.from(now));
            statement.setTimestamp(6, Timestamp.from(now));
            statement.setString(7, operationId.value());
            int updated = statement.executeUpdate();
            if (updated != 1) {
                throw new JdbcStorageException("Progression operation was not in STARTED state", null);
            }
        } catch (SQLException ex) {
            throw new JdbcStorageException("Failed to mark progression operation applied", ex);
        }
    }

    private Connection connection() {
        return context.current().orElseThrow(() -> new IllegalStateException("JDBC operation requires an explicit transaction"));
    }
}
