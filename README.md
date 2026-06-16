# CraftMMO

CraftMMO is a Paper plugin project for activity-based MMO progression. Sprint 1 is foundation-only: domain models, persistence, migrations, content/reference validation, lifecycle, and developer infrastructure.

## Requirements

- Windows development environment
- Java 25
- Maven Wrapper 3.9.16
- Docker Desktop with PostgreSQL 17
- Optional Redis 7 profile

## Verify

```powershell
.\mvnw.cmd -B -ntp clean verify
docker compose config --services
docker compose config --profiles
git status --short --branch
```

## Development Database

```powershell
.\scripts\Start-DevEnvironment.ps1
.\scripts\Stop-DevEnvironment.ps1
```

Destructive reset requires typing `RESET`:

```powershell
.\scripts\Reset-DevEnvironment.ps1
```

Redis is optional and never authoritative.

```powershell
docker compose --profile redis up -d
```

## Scope

This repository must not include mcMMO source, messages, assets, imports, or package design. Unknown mechanics remain `NEEDS_REVIEW`.
