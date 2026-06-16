# Testing

Unit tests cover domain invariants, progression policies, idempotency, content validation, reference validation, skill registry behavior, and Paper bootstrap state transitions.

Integration tests use Testcontainers with PostgreSQL 17 Alpine.

The shaded plugin JAR is inspected to prevent packaging Paper API, JUnit, Testcontainers, or Maven classes.
