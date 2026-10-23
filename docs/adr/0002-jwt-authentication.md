# 2. Stateless JWT authentication

Date: 2026-10-22
Status: Accepted

## Context
The SPA and API may be served from different origins in the cloud. Only admins create
users; there is no self-registration or email-based reset.

## Decision
`POST /api/v1/auth/login` checks email + BCrypt password and returns an HS256 JWT
(30-minute TTL, claims `sub`, `email`, `roles`) signed with `JWT_SECRET`. Requests are
validated by Spring Security's OAuth2 resource server; no custom JWT filter. Method
security (`@PreAuthorize`) enforces roles. CSRF is disabled because no cookies are used.
The SPA keeps the token in memory only, so a page reload requires logging in again.

## Consequences
No server-side session state. Deactivating a user blocks new logins immediately, but an
already-issued token stays valid until it expires (≤ 30 min). Refresh tokens or a
revocation check can be added later if needed (plan Q-1).
