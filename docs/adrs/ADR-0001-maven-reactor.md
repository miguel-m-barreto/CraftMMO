# ADR-0001 Maven Reactor

## Status

Accepted.

## Decision

Use a Maven multi-module reactor with the root POM as the single version authority.

## Consequences

Module boundaries are explicit, dependency convergence is enforced, and release metadata is reproducible.
