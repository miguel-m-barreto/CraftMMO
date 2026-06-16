package com.craftmmo.api.session;

import java.util.Objects;
import java.util.Optional;

public record SessionLeaseResult(SessionLeaseResultStatus status, Optional<SessionLease> lease) {
    public SessionLeaseResult {
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(lease, "lease");
        lease = lease.map(Objects::requireNonNull);
        if (status == SessionLeaseResultStatus.ACQUIRED && lease.isEmpty()) {
            throw new IllegalArgumentException("Acquired session results must include a lease");
        }
        if (status == SessionLeaseResultStatus.REJECTED && lease.isPresent()) {
            throw new IllegalArgumentException("Rejected session results must not include a lease");
        }
    }

    public static SessionLeaseResult acquired(SessionLease lease) {
        return new SessionLeaseResult(SessionLeaseResultStatus.ACQUIRED, Optional.of(lease));
    }

    public static SessionLeaseResult rejected() {
        return new SessionLeaseResult(SessionLeaseResultStatus.REJECTED, Optional.empty());
    }

    public boolean acquired() {
        return status == SessionLeaseResultStatus.ACQUIRED;
    }
}
