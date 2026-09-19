---
title: "create_metered_motor DEC-008 — Empty stops it, redstone pauses it, five slots for emeralds and emerald blocks, automation inserts only"
type: "spec"
category: "create_metered_motor"
---

# `DEC-008` — Empty stops it, redstone pauses it, five slots for emeralds and emerald blocks, automation inserts only

**Status:** decided by Kevin, 2026-09-19.

Four edges ruled together, closing the motor's state machine (`domains/motor.md` §3): an empty
inventory stops generation and stress contribution outright (`MOTOR-REQ-007`); a redstone signal
pauses it the same way regardless of fuel (`MOTOR-REQ-010`); the inventory holds five slots
accepting only emeralds and emerald blocks, an emerald block splitting into nine on the spot when
needed (`MOTOR-REQ-008`, `MOTOR-REQ-006`); and every face accepts insertion by hand, hopper or
Create logistics but refuses extraction — only the screen takes emeralds out
(`MOTOR-REQ-009`, `UI-REQ-008`).

Alternative considered: no redstone control at all, leaving a motor running whenever fed, as
Create's water wheels and windmills have no switch. Rejected: a motor that cannot be paused can
only be stopped by emptying its inventory, and a player who wants to hold a machine still for a
while would have to take the emeralds out and put them back. Cost if wrong: one block state and
its wiring; `domains/motor.md` §3 carries the paused state.
