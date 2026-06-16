package com.craftmmo.storage.session;

import com.craftmmo.api.identity.PlayerId;
import com.craftmmo.api.session.SessionLease;
import com.craftmmo.api.session.SessionLeaseResult;
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
import java.util.UUID;

public final class JdbcSessionLeaseRepository {
    private final JdbcConnectionContext context;

    public JdbcSessionLeaseRepository(JdbcConnectionContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    public SessionLeaseResult acquire(PlayerId playerId, String serverId, UUID leaseId, Instant now, Instant expiresAt) {
        validateExpiry(now, expiresAt);
        String sql = """
                insert into session_ownership (player_uuid, server_id, lease_id, expires_at, version, created_at, updated_at)
                values (?, ?, ?, ?, 1, ?, ?)
                on conflict (player_uuid) do update
                set server_id = excluded.server_id,
                    lease_id = excluded.lease_id,
                    expires_at = excluded.expires_at,
                    version = session_ownership.version + 1,
                    updated_at = excluded.updated_at
                where session_ownership.expires_at <= ?
                returning server_id, lease_id, expires_at, version
                """;
        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            statement.setObject(1, playerId.value());
            statement.setString(2, serverId);
            statement.setObject(3, leaseId);
            statement.setTimestamp(4, Timestamp.from(expiresAt));
            statement.setTimestamp(5, Timestamp.from(now));
            statement.setTimestamp(6, Timestamp.from(now));
            statement.setTimestamp(7, Timestamp.from(now));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return SessionLeaseResult.rejected();
                }
                return SessionLeaseResult.acquired(readLease(playerId, resultSet));
            }
        } catch (SQLException ex) {
            throw new JdbcStorageException("Failed to acquire session lease", ex);
        }
    }

    public Optional<SessionLease> renew(PlayerId playerId, UUID leaseId, Instant now, Instant expiresAt) {
        validateExpiry(now, expiresAt);
        String sql = """
                update session_ownership
                set expires_at = ?, version = version + 1, updated_at = ?
                where player_uuid = ? and lease_id = ? and expires_at > ?
                returning server_id, lease_id, expires_at, version
                """;
        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.from(expiresAt));
            statement.setTimestamp(2, Timestamp.from(now));
            statement.setObject(3, playerId.value());
            statement.setObject(4, leaseId);
            statement.setTimestamp(5, Timestamp.from(now));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(readLease(playerId, resultSet));
            }
        } catch (SQLException ex) {
            throw new JdbcStorageException("Failed to renew session lease", ex);
        }
    }

    public boolean release(PlayerId playerId, UUID leaseId) {
        String sql = "delete from session_ownership where player_uuid = ? and lease_id = ?";
        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            statement.setObject(1, playerId.value());
            statement.setObject(2, leaseId);
            return statement.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw new JdbcStorageException("Failed to release session lease", ex);
        }
    }

    private SessionLease readLease(PlayerId playerId, ResultSet resultSet) throws SQLException {
        return new SessionLease(
                playerId,
                resultSet.getString("server_id"),
                resultSet.getObject("lease_id", UUID.class),
                resultSet.getTimestamp("expires_at").toInstant(),
                resultSet.getLong("version")
        );
    }

    private Connection connection() {
        return context.current().orElseThrow(() -> new IllegalStateException("JDBC operation requires an explicit transaction"));
    }

    private static void validateExpiry(Instant now, Instant expiresAt) {
        Objects.requireNonNull(now, "now");
        Objects.requireNonNull(expiresAt, "expiresAt");
        if (!expiresAt.isAfter(now)) {
            throw new IllegalArgumentException("Session lease expiry must be after the current time");
        }
    }
}
