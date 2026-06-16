# CraftMMO Development

CraftMMO is a Maven multi-module Paper plugin project. The current baseline is Java 25, Maven Wrapper 3.9.16, Paper API `26.1.2.build.70-stable`, PostgreSQL 17, optional Redis 7, JUnit 5, Testcontainers, Flyway, HikariCP, and plain JDBC.

## Windows Quick Start

1. Install JDK 25 and make sure `java -version` reports Java 25.
2. Install Docker Desktop.
3. Copy `.env.example` to `.env` if local port or credential overrides are needed.
4. Start PostgreSQL:

```powershell
.\scripts\Start-DevEnvironment.ps1
```

5. Verify the project:

```powershell
.\scripts\Invoke-Verify.ps1
```

## Audit Bundle

Create a local audit bundle with:

```powershell
.\scripts\New-AuditBundle.ps1
```

The bundle is written to `.audit-bundles\CraftMMO-audit-YYYYMMDD-HHmmss.zip`.
