# CraftMMO Agent Instructions

## Baseline

- Write all repository files in English.
- Use Java 25, Maven Wrapper 3.9.16, Maven multi-module layout, and project version `0.1.0-SNAPSHOT`.
- Use package root `com.craftmmo`.
- Use Paper API `26.1.2.build.70-stable` as a `provided` dependency only.
- Use PostgreSQL as the authoritative persistent store.
- Redis is optional and must never be authoritative.
- Use JUnit 5, Testcontainers, Flyway, HikariCP, and plain JDBC.
- Use classic `plugin.yml`; the Paper plugin main class is `com.craftmmo.paper.CraftMmoPlugin`.

## Boundaries

- Production modules must not depend on `craftmmo-testkit`.
- Core domain code must not depend on Paper, JDBC, HikariCP, Flyway, Testcontainers, JUnit, Maven, Redis, or Bukkit APIs.
- Paper adapter code must not perform blocking database work on the Paper main thread.
- Storage code owns JDBC, HikariCP, Flyway, and transaction implementations.
- Unknown mcMMO mechanics must remain marked `NEEDS_REVIEW`.
- Do not add mcMMO as a dependency.
- Do not inspect or copy mcMMO source, messages, assets, or package design.

## Exclusions For Batch 001

Do not implement actual gameplay listeners, Mining listeners, verified or guessed mcMMO formulas, abilities, combat, loot, quests, mobs, bosses, parties, menus, custom items, integrations, mcMMO imports, NMS, or Folia support.

## Verification

Run these commands before reporting completion when the environment supports them:

```powershell
.\mvnw.cmd -B -ntp clean verify
docker compose config --services
docker compose config --profiles
git status --short --branch
```

Inspect the shaded plugin JAR and confirm Paper API, JUnit, Testcontainers, and Maven classes are not packaged.

## Audit Bundles

- Codex must never execute audit or review bundle scripts.
- Only the human developer runs audit or review bundle scripts.
- Codex may update audit bundle scripts when requested, but must only print the command needed to run them.
