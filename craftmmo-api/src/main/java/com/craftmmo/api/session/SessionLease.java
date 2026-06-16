package com.craftmmo.api.session;

import com.craftmmo.api.identity.PlayerId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record SessionLease(PlayerId playerId, String serverId, UUID leaseId, Instant expiresAt, long version) {
    public SessionLease {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(serverId, "serverId");
        Objects.requireNonNull(leaseId, "leaseId");
        Objects.requireNonNull(expiresAt, "expiresAt");
        serverId = serverId.trim();
        if (serverId.isBlank()) {
            throw new IllegalArgumentException("Server ID must not be blank");
        }
        if (version < 0) {
            throw new IllegalArgumentException("Version must not be negative");
        }
    }
}
