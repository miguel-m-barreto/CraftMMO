# Contributing

Use English for code, documentation, comments, tests, and commit messages.

Before proposing changes, run:

```powershell
.\mvnw.cmd -B -ntp clean verify
docker compose config --services
docker compose config --profiles
```

Do not add Lombok, Spring, Hibernate, JPA, Kotlin, Gradle, mcMMO dependencies, NMS, or Folia support.

Production modules must not depend on `craftmmo-testkit`.
