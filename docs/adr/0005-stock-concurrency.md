# 5. Pessimistic locking for stock movements

Date: 2026-10-22
Status: Accepted

## Context
Design doc §3.5: concurrent movements on the same product must not lose updates, and an
OUT movement must never drive stock negative.

## Decision
Recording a movement runs in one transaction that loads the product with
`PESSIMISTIC_WRITE` (`SELECT … FOR UPDATE`), checks the OUT rule, updates the quantity
and inserts the movement. A `CHECK (quantity >= 0)` constraint is the backstop.
Optimistic locking (`version`) still protects ordinary product edits.

## Consequences
Concurrent movements on one product are serialized rather than failing with retryable
conflicts; contention is negligible at small-business scale. Product edits use a
`version` check, and recording a movement also bumps the version, so an edit form opened
before a movement gets a 409 and is reloaded.
