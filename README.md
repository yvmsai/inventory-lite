# Inventory Lite

Track products, suppliers and stock levels for a small business.
Spring Boot 4 (Java 21) API + React 19 / TypeScript SPA + PostgreSQL 17.

- Design: [docs/design.md](docs/design.md) · Plan and status: [docs/plan.md](docs/plan.md) · Decisions: [docs/adr](docs/adr)

## Run locally (Docker only)

```bash
cp .env.example .env     # then set DB_PASSWORD and JWT_SECRET (openssl rand -base64 32)
docker compose up --build -d
```

| What | URL |
|---|---|
| App | http://localhost:3000 |
| API | http://localhost:${API_PORT:-8080}/api/v1 |
| Swagger UI | http://localhost:${API_PORT:-8080}/swagger-ui.html |

If ports 8080 or 5432 are already in use, set `API_PORT` / `DB_PORT` in `.env`.

Demo accounts (local profile only, from `backend/src/main/resources/db/seed`):

| Role | Email | Password |
|---|---|---|
| ADMIN | admin@inventory.local | `Admin123!` |
| STAFF | staff@inventory.local | `Staff123!` |

The token is kept in memory only, so reloading the page means logging in again (ADR 0002).

Reset the local database (deletes all local data): `docker compose down -v`.

## Develop

Backend (needs Java 21 and Docker for Testcontainers):

```bash
cd backend
./mvnw verify                          # unit + integration tests + coverage gate
```

Without a local Java 21, run the same thing in a container:

```bash
docker run --rm -v "$PWD":/app -w /app -v /var/run/docker.sock:/var/run/docker.sock \
  -e TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal maven:3.9-eclipse-temurin-21 ./mvnw -B verify
```

Frontend:

```bash
cd frontend
npm ci
API_PROXY_TARGET=http://localhost:8080 npm run dev   # http://localhost:5173, /api proxied to the API
npm run lint && npm test && npm run build
API_DOCS_URL=http://localhost:8080/v3/api-docs npm run gen:api   # regenerate API types after an endpoint changes
```

## Production

The first ADMIN is created at startup from `ADMIN_EMAIL` / `ADMIN_PASSWORD` when no admin exists.
Cloud deployment (Render + Neon) is set up in a later phase.
