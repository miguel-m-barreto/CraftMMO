# ADR-0006 Package Naming And Ownership Boundaries

## Status

Accepted.

## Decision

All production Java packages live under `com.craftmmo`.

Modules own their boundaries:

- `craftmmo-api` owns stable domain types and ports.
- `craftmmo-core` owns application services and policies.
- `craftmmo-storage` owns JDBC, transactions, Flyway, and PostgreSQL repositories.
- `craftmmo-skills` owns the concrete immutable skill registry.
- `craftmmo-reference` owns reference manifest parsing and validation.
- `craftmmo-paper` owns Paper lifecycle and command adapters.

## Consequences

Production code must not depend on `craftmmo-testkit`, and adapters must not leak Paper, JDBC, or test infrastructure into core domain code.
