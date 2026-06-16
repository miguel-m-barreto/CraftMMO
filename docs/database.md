# Database

PostgreSQL is authoritative for players, skill progress, cooldowns, session ownership, progression operations, and audit logs.

Persistence uses plain JDBC, prepared statements, explicit transactions, optimistic locking, and Flyway migrations.

Redis is optional and not authoritative.
