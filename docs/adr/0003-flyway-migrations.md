# 3. Schema owned by Flyway migrations

Date: 2026-10-22
Status: Accepted

## Context
The schema must be reproducible across local Docker, CI (Testcontainers) and the cloud.

## Decision
Flyway migrations in `backend/src/main/resources/db/migration` own the schema;
Hibernate runs with `ddl-auto=validate`. Invariants (NOT NULL, UNIQUE, CHECK, FKs) are
enforced in the database as well as in Java. Local demo data lives in `db/seed` and is
loaded only by the `local` profile.

## Consequences
Applied migrations are never edited; every change is a new migration. Schema drift is
caught at startup.
