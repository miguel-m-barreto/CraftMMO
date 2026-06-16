# Security

Do not commit credentials, local `.env` files, keys, tokens, database dumps, server runtime folders, or audit bundles.

Startup and health messages shown to normal users must expose only sanitized failure categories. Detailed failure output requires `craftmmo.admin.health.details`.

PostgreSQL is authoritative. Redis is optional cache infrastructure only.
