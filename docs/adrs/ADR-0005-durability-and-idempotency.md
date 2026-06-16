# ADR-0005 Durability And Idempotency

## Status

Accepted.

## Decision

PostgreSQL is authoritative for progression durability and operation identity.

Every progression operation records operation ID, target player, target skill, operation type, source, canonical payload hash, status, timestamps, and result metadata.

## Consequences

Duplicate operations with the same identity and payload replay the prior result. Reusing an operation ID with a different payload is an identity conflict and must never be silently accepted.
