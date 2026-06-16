# Batch 001 Implementation Plan

## Scope

Batch 001 creates the CraftMMO project foundation and runtime skeleton. It establishes module boundaries, immutable core domain models, PostgreSQL persistence contracts and implementations, content and reference manifests, Paper lifecycle startup, development tooling, CI, and audit bundle creation. It does not implement gameplay mechanics.

## Steps

1. Confirm repository state and keep branch `main`.
2. Create Maven parent reactor and the required modules:
   - `craftmmo-api`
   - `craftmmo-core`
   - `craftmmo-storage`
   - `craftmmo-content`
   - `craftmmo-skills`
   - `craftmmo-paper`
   - `craftmmo-reference`
   - `craftmmo-integrations`
   - `craftmmo-migration`
   - `craftmmo-testkit`
3. Configure Java 25, Maven Wrapper 3.9.16, dependency management, compiler, surefire, failsafe, jar, shade, dependency, and enforcer plugins.
4. Define strict dependencies:
   - `api` has no internal dependencies.
   - `core` depends on `api`.
   - `storage`, `content`, `skills`, `reference`, `integrations`, and `migration` depend only on public API/core as needed.
   - `paper` depends on production modules and keeps Paper API as `provided`.
   - `testkit` depends on `api` and `core` for deterministic fakes only.
5. Add immutable domain models for players, skills, progression, versions, cooldowns, sessions, operation IDs, clocks, and random services.
6. Add all 19 stable skill IDs and immutable skill registry support.
7. Add child skill derivation for Salvage and Smelting using explicit derived policies without guessing reference formulas.
8. Add configurable Power Level policy.
9. Add deterministic test clock and random implementations in `craftmmo-testkit`.
10. Add progression service using repository and transaction ports, optimistic locking, and duplicate operation protection.
11. Add PostgreSQL repositories with prepared statements and explicit transactions.
12. Add Flyway migrations for players, skill progress, cooldowns, session ownership, progression operations, and admin audit logs.
13. Add session lease acquire, renew, and release.
14. Add PostgreSQL integration tests using Testcontainers.
15. Add content snapshot validation and atomic activation.
16. Add versioned mcMMO `2.2.053` reference manifest with unknown mechanics marked `NEEDS_REVIEW`.
17. Add Paper lifecycle states `NEW`, `STARTING`, `READY`, `FAILED`, `STOPPING`, and `STOPPED`, async database startup, `/craftmmo help`, `/craftmmo version`, `/craftmmo health`, sanitized startup failures, and safe shutdown.
18. Add Docker Compose, `.env.example`, Windows PowerShell scripts for environment start, stop, reset, verification, and audit bundle creation.
19. Add GitHub Actions verification.
20. Run build, Docker Compose validation, Git status, and packaging inspection.

## Architecture Decisions

- PostgreSQL is the only authoritative store.
- Redis is optional and excluded from Batch 001 production authority.
- Child skills are derived values, not persisted authoritative progress.
- mcMMO reference metadata is recorded for traceability, while unverified mechanics remain `NEEDS_REVIEW`.
- Paper startup performs blocking storage initialization on a dedicated executor and only observes state on the main thread.
- The shaded plugin packages CraftMMO production code and runtime storage libraries only; Paper API, tests, Testcontainers, JUnit, and Maven artifacts are excluded.

## Review Notes

This plan intentionally builds a compileable foundation with ports, policies, migrations, and tests. Batch 001 will not include gameplay formulas, event listeners, abilities, combat, loot, menus, imports, NMS, or Folia support.
