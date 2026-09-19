---
schema_version: 1
id: 01M2W33S1BJQNNVW9CEV1VYRNT
key: MM-9
type: test
title: "Game-test sweep and playable check: every MOTOR, TRADE and UI requirement named by a test"
created_by: kevin
created_at: 2026-09-19T05:44:32Z
---

## Scope

The requirement-to-test table (`TEST-REQ-001`): every `MOTOR-REQ`, `TRADE-REQ` and `UI-REQ` mapped to the game test or unit test that proves it, plus `COMP-REQ-001` (no network call, `SourceSurfaceTest`, established under MM-1), written into this ticket. Three green `just check` runs in a row. Kevin's client checklist: the screen, the goggles overlay, the tinted model, and trading with a real toolsmith (level up a villager, buy a rolled motor, place it, feed it, watch the stress gauge and the meter).

## Approach

One table in this ticket, filled from the tests written under MM-2 through MM-7; where a requirement cannot be proven headless (screen and overlay rendering, model tinting, restocking timing), the table names the reason and points at the client checklist instead. `just check` run three times in a row with no flaky failure, on a clean checkout.

## Acceptance criteria

- [ ] A requirement-to-test table in this ticket covers every `MOTOR-REQ`, `TRADE-REQ` and `UI-REQ` id, plus `COMP-REQ-001` (`TEST-REQ-001`).
- [ ] `just check` green three consecutive runs, recorded with their game test counts.
- [ ] Kevin's client checklist done and recorded: the screen, the goggles overlay, the tinted block model across the three tiers, and a real toolsmith trade bought and placed.

## Constraints and prior findings

`TEST-REQ-001`, `operations/testing.md`. `TEST-REQ-002` (the deliberate-break proof for `verifyPurePackage`) is already satisfied under MM-2 and only needs restating in the table here. Blocked by MM-5, MM-6 and MM-7 — the trade, screen and goggles/visual work this sweep proves.
