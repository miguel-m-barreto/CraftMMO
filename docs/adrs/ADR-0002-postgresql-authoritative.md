# ADR-0002 PostgreSQL Authoritative Store

## Status

Accepted.

## Decision

PostgreSQL is the authoritative data store. Redis may be used only as optional non-authoritative infrastructure.

## Consequences

Correctness must be enforced in PostgreSQL migrations, JDBC repositories, transactions, and optimistic locking.
