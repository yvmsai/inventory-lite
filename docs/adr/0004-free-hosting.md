# 4. Free hosting on Render + Neon

Date: 2026-10-22
Status: Accepted

## Context
The app should be reachable on the internet at no cost.

## Decision
API as a Render Docker web service, SPA as a Render static site, PostgreSQL on Neon's free
tier, described by a `render.yaml` Blueprint and a `prod` Spring profile. Secrets are
entered by the user in the provider dashboards.

## Consequences
Free-tier services sleep when idle (cold starts of ~30–60 s) and have limited storage and
compute; acceptable for a small business pilot. Upgrading is a plan change, not a rewrite.
