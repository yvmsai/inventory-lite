# Inventory Lite: Implementation Plan

## Summary

Inventory Lite helps a small business track products, suppliers and stock levels. STAFF
record stock coming in (IN) and going out (OUT); ADMINs also manage the catalog and users.
Everyone sees a dashboard of totals, low-stock items and recent movements.

## Roles

| Role | Can |
|---|---|
| ADMIN | Everything: manage users, suppliers and products; record stock movements; view dashboard |
| STAFF | View suppliers and products; record stock movements; view movement history; view dashboard |

## Entities

All tables also carry `id BIGINT identity`, `version`, `created_at`, `updated_at`,
`created_by`, `updated_by` (see A-3).

### User (table `app_user`)

| Field | Type | Required | Constraints |
|---|---|---|---|
| email | string(200) | yes | unique case-insensitive (stored lower-cased + unique index on `lower(email)`), valid email |
| fullName | string(150) | yes | not blank |
| passwordHash | string(100) | yes | BCrypt, never returned |
| role | enum ADMIN, STAFF | yes | |
| active | boolean | yes | default true |

### Supplier

| Field | Type | Required | Constraints |
|---|---|---|---|
| name | string(150) | yes | unique (case-insensitive, A-6), not blank |
| email | string(200) | no | valid email |
| phone | string(30) | no | |

### Product

| Field | Type | Required | Constraints |
|---|---|---|---|
| sku | string(64) | yes | unique, `^[A-Z0-9-]+$`, max 64 |
| name | string(200) | yes | not blank |
| unitPrice | decimal(19,2) | yes | ≥ 0, max 2 decimal places |
| quantity | integer | yes | ≥ 0, default 0, changed only by stock movements |
| reorderLevel | integer | yes | ≥ 0, default 10 |
| supplier | Supplier | yes | FK |

Relationships: Product *—1 Supplier (many products belong to one supplier)

### StockMovement

| Field | Type | Required | Constraints |
|---|---|---|---|
| product | Product | yes | FK |
| type | enum IN, OUT | yes | |
| quantity | integer | yes | > 0 |
| note | string(500) | no | |
| createdBy | User | yes | set from the JWT subject, never from the request |
| quantityAfter | integer | yes | product quantity after this movement (A-8) |

Relationships: StockMovement *—1 Product, StockMovement *—1 User (createdBy)

## Business rules

- BR-1: Login fails with the same 401 message for an unknown email, a wrong password and a deactivated user.
- BR-2: Only ADMIN can create users; there is no self-registration.
- BR-3: User email is unique, case-insensitive (`Bob@x.com` and `bob@x.com` collide → 409).
- BR-4: A deactivated user cannot log in.
- BR-5: An ADMIN cannot deactivate themselves (→ 422).
- BR-6: Supplier name is unique (→ 409).
- BR-7: A supplier with products cannot be deleted (→ 409).
- BR-8: Product SKU is unique (→ 409) and matches `^[A-Z0-9-]+$`, max 64 chars (→ 400).
- BR-9: Unit price is ≥ 0 with at most 2 decimal places (→ 400).
- BR-10: Reorder level is an integer ≥ 0, default 10.
- BR-11: Product quantity cannot be set via create/update; it changes only via stock movements.
- BR-12: A product with stock movements cannot be deleted (→ 409).
- BR-13: Movement quantity is a positive integer (→ 400).
- BR-14: An OUT movement that would make stock negative is rejected (→ 422); quantity stays unchanged.
- BR-15: Recording a movement updates the product quantity (IN adds, OUT subtracts) in the same transaction.
- BR-16: Movements are immutable: no update or delete endpoints exist.
- BR-17: Concurrent movements on the same product must not lose updates (pessimistic row lock `SELECT … FOR UPDATE` on the product; DB `CHECK (quantity >= 0)` as a backstop).
- BR-18: Movement history per product is ordered newest first.
- BR-19: Dashboard totals: product count, supplier count, total stock value = Σ(quantity × unitPrice).
- BR-20: Low-stock list: products where quantity ≤ reorderLevel, ordered by quantity ascending.
- BR-21: Dashboard shows the 10 most recent stock movements.
- BR-22: Every change records who did it and when (`created_by/at`, `updated_by/at`).

## API endpoints

Base path `/api/v1`. Lists are paginated (`page`, `size` ≤ 100, `sort`).

| Method | Path | Purpose | Roles |
|---|---|---|---|
| POST | /auth/login | Log in, returns JWT | public |
| GET | /auth/me | Current user profile | ADMIN, STAFF |
| GET | /users | List users, paginated | ADMIN |
| POST | /users | Create user (email, fullName, password, role) | ADMIN |
| PATCH | /users/{id} | Change role / fullName | ADMIN |
| POST | /users/{id}/deactivate | Deactivate user | ADMIN |
| POST | /users/{id}/activate | Reactivate user (A-9) | ADMIN |
| GET | /suppliers?q= | List / search by name, paginated | ADMIN, STAFF |
| GET | /suppliers/{id} | Get supplier | ADMIN, STAFF |
| POST | /suppliers | Create supplier | ADMIN |
| PUT | /suppliers/{id} | Update supplier | ADMIN |
| DELETE | /suppliers/{id} | Delete supplier (BR-7) | ADMIN |
| GET | /products?q=&supplierId= | List / search by name or SKU, filter by supplier | ADMIN, STAFF |
| GET | /products/{id} | Get product | ADMIN, STAFF |
| POST | /products | Create product | ADMIN |
| PUT | /products/{id} | Update product (not quantity) | ADMIN |
| DELETE | /products/{id} | Delete product (BR-12) | ADMIN |
| GET | /products/{id}/movements | Movement history, newest first, paginated | ADMIN, STAFF |
| POST | /products/{id}/movements | Record movement | ADMIN, STAFF |
| GET | /dashboard | Totals, low-stock list, 10 recent movements | ADMIN, STAFF |

## Screens

| Route | Purpose | Roles |
|---|---|---|
| /login | Sign in | public |
| / | Dashboard: totals, low-stock list (links to product), recent movements | ADMIN, STAFF |
| /suppliers | Paginated searchable table; add / edit / delete (ADMIN only) | ADMIN, STAFF |
| /products | Paginated table: SKU, name, supplier, qty, price, low-stock badge; search + supplier filter; add / edit / delete (ADMIN only) | ADMIN, STAFF |
| /products/:id | Product info + movement history; record movement form | ADMIN, STAFF |
| /users | Users table; add user, change role, deactivate / activate | ADMIN |

## Non-functional requirements

- Desktop-first, usable on a tablet (responsive down to 768 px).
- Lists load in < 1 s for 10,000 products: server-side pagination, indexes on `lower(name)`, `sku`, `supplier_id`, `(product_id, created_at)`; trigram indexes (`pg_trgm`) for contains-search on product name/SKU and supplier name.
- Audit: every table records who created/last changed a row and when (BR-22); stock movements are an immutable log.
- Accessibility: labelled form controls, keyboard navigable, visible focus.

## Assumptions

- A-1: Roles are `ADMIN` and `STAFF` (from the doc, replacing the factory default `USER`).
- A-2: IDs are `BIGINT` identity; optimistic locking via `version` on all mutable tables.
- A-3: "Who and when" is implemented with Spring Data JPA auditing: `created_by`/`updated_by` (user id) and `created_at`/`updated_at` columns on every table.
- A-4: Hard delete for suppliers and products (guarded by BR-7 / BR-12); users are never deleted, only deactivated.
- A-5: Search is case-insensitive "contains" matching.
- A-6: Supplier-name uniqueness is case-insensitive (consistent with emails).
- A-7: The ADMIN creating a user sets the initial password (min 8 chars); there is no password reset by email (out of scope). Users can't change their own password in v1.
- A-8: Each movement stores `quantityAfter` so the history shows the running balance.
- A-9: An ADMIN can reactivate a deactivated user (natural inverse of deactivate, low cost).
- A-10: Changing a user's role is allowed, except an ADMIN cannot demote themselves (same reasoning as BR-5, keeps at least one admin reachable).
- A-11: JWTs stay stateless, so deactivating a user blocks new logins immediately but doesn't revoke a token already issued; it expires within 30 min (see Q-1).
- A-12: Money is `NUMERIC(19,2)` / `BigDecimal` / JSON string. Timestamps are `timestamptz` UTC / ISO-8601.
- A-13: Default list sort: suppliers and products by name ascending; users by email; movements by `createdAt` desc.
- A-14: Local seed data: one ADMIN (`admin@inventory.local`) and one STAFF (`staff@inventory.local`), with dev passwords documented in the README (local profile only), plus a few sample suppliers/products. In prod, the first ADMIN comes from `ADMIN_EMAIL` / `ADMIN_PASSWORD`.
- A-15: The access token lives in memory; a page reload requires logging in again (ADR 0002).
- A-16: The package is `com.inventorylite` (no organization named in the doc).
- A-17: The dashboard low-stock list shows the 50 lowest items plus the total low-stock count, so it stays fast with 10,000 products.
- A-18: Supplier and product edits send the `version` they loaded; a stale version returns 409 ("modified by someone else, reload").
- A-19: Recording a movement returns 201 without a `Location` header, because movements have no get-by-id endpoint (they're read through the product's history).

## Open questions

- Q-1: Should deactivating a user immediately invalidate their current session, rather than within ≤ 30 min of token expiry? (non-blocking, A-11)
- Q-2: Should STAFF be allowed to see the Users screen read-only? The doc says no; kept ADMIN-only. (non-blocking)
- Q-3: Is a currency symbol needed in the UI? Displayed as a plain number with 2 decimals for now. (non-blocking)

## Out of scope

- Multiple warehouses or locations (design doc §7)
- Purchase orders and invoicing (design doc §7)
- Password reset by email (design doc §7)
- Self-service password change (not requested; A-7)
- Editing or deleting stock movements (BR-16, by design)

## Tasks

- [x] T1 foundation: scaffolding, base entity + auditing, error handling (problem details), security (JWT), app_user migration + seed, `POST /auth/login`, `GET /auth/me`, login page, app shell + routing
- [x] T2 users: user management API (create, list, patch role, deactivate/activate), tests (BR-2..5, A-10), Users screen + tests
- [x] T3 suppliers: migration, CRUD + search API, tests (BR-6, BR-7), Suppliers screen + tests
- [x] T4 products: migration, CRUD + search/filter API, tests (BR-8..12), Products screen + tests
- [x] T5 stock movements: migration, record + history API with row locking, tests (BR-13..18 incl. concurrency), Product detail screen + tests
- [x] T6 dashboard: dashboard API (BR-19..21), tests, Dashboard screen + tests
- [x] T7 verify: full test run, coverage gate, `npm audit`, OWASP checklist
- [x] T8 packaging: Dockerfiles, docker-compose, `.env.example`, launch and health check
- [ ] T9 CI: GitHub Actions workflow (phase 9, after checkpoint 2)
- [ ] T10 deploy: render.yaml + deploy guide (phase 10, after checkpoint 2)

## Verification (phase 7)

- Backend `./mvnw verify`: 102 tests, 0 failures (unit, `@WebMvcTest` slices, Testcontainers PostgreSQL 17 integration incl. concurrent IN/OUT movements). JaCoCo line coverage: services 99.5% (gate ≥ 80%), overall 96.8%.
- Frontend: `npm run lint` clean, `npm test` 31 tests passing, `npm run build` OK, line coverage 82%. `npm audit`: 0 vulnerabilities.
- End-to-end against Docker Compose: login, role checks (401/403), stock OUT beyond stock (422), delete product with movements (409), dashboard totals, audit columns.

### OWASP Top 10 checklist

- [x] A01 Access control: every endpoint has a role rule; tests cover 401 (no token) and 403 (wrong role).
- [x] A02 Crypto: BCrypt (strength 10); JWT secret validated ≥ 32 bytes at startup; HTTPS in prod via Render.
- [x] A03 Injection: Spring Data/JPQL with bound parameters only; LIKE wildcards in user input are escaped.
- [x] A04 Insecure design: all business rules enforced server-side and in DB constraints.
- [x] A05 Misconfiguration: Actuator exposes health/info only; no stack traces in responses; Swagger disabled in `prod`.
- [x] A06 Components: Spring Boot 4.1.1, current npm packages, `npm audit` clean.
- [x] A07 Auth failures: one generic 401 message for unknown email / wrong password / deactivated user (with equal BCrypt cost); 30-min token expiry. **Gap:** no login rate limiting or lockout yet.
- [x] A08 Integrity: Docker builds use `npm ci` from the lockfile.
- [x] A09 Logging: login failures logged at WARN without email or password.
- [x] A10 SSRF: no endpoint fetches user-supplied URLs.
