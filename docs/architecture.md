# Architecture

The project is a Maven reactor with API, core, storage, content, skills, reference, integrations, migration, testkit, and Paper modules.

Dependency direction is inward toward API and core ports. Production modules must not depend on `craftmmo-testkit`.

The Paper module adapts lifecycle and commands to a testable bootstrap. Database startup runs off the main thread.
