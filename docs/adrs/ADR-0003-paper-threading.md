# ADR-0003 Paper Threading

## Status

Accepted.

## Decision

Blocking database startup, Flyway migration, and health checks run off the Paper main thread.

## Consequences

The Paper adapter uses a testable bootstrap and bounded shutdown behavior.
