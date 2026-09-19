---
schema_version: 1
id: 01M2W3381PSHPYDS1WS9N5CEW0
key: M4
title: Release 1.0.0+26.2
status: backlog
created_at: 2026-09-19T05:44:15Z
---

## Goal

Version 1.0.0+26.2 published on Modrinth with every requirement named by a test and a security pass recorded before the cut.

## Scope

In: the requirement-to-test table and three green `just check` runs, Kevin's client checklist including a real toolsmith trade (MM-9); release preparation, listing assets, the security pass and the cut itself (MM-10). Out: any behavioural change — this milestone only proves and ships what M1 through M3 built.

## Exit criteria

- Tag on `production`, jar and checksum attached, the Modrinth listing live, `SUPPORT.md` present.
- The requirement-to-test table covers every `MOTOR-REQ`, `TRADE-REQ` and `UI-REQ`.

## Tickets

- MM-9 — Game-test sweep and playable check: every MOTOR, TRADE and UI requirement named by a test
- MM-10 — Release preparation and cut: 1.0.0+26.2

## Depends on

M3: MM-9 is blocked by MM-5, MM-6 and MM-7 — the trade, screen and goggles/visual work the sweep proves; MM-10 is blocked by MM-9.
