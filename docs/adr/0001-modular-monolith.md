# 1. Modular monolith

Date: 2026-10-22
Status: Accepted

## Context
Inventory Lite is a small-business app with two roles, four entities and one team. The
design doc names no need for independent scaling, separate teams or release cycles.

## Decision
One Spring Boot API (`backend/`), one React SPA (`frontend/`) and one PostgreSQL
database, in a single monorepo. The backend is organised package-by-feature
(`auth`, `user`, `supplier`, `product`, `stock`, `dashboard`, `common`); features
talk to each other only through services, never another feature's repository.

## Consequences
Simple to build, test, deploy and run on free hosting. A feature could be extracted later
if a real need appears, because package boundaries are already explicit.
