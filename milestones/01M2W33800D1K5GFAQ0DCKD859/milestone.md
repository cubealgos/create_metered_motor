---
schema_version: 1
id: 01M2W33800D1K5GFAQ0DCKD859
key: M3
title: The screen, goggles, visuals, debug command
status: todo
created_at: 2026-09-19T05:44:15Z
---

## Goal

The motor is readable and pleasant to use without ever opening a config: a real screen for hand-feeding and reading the meter, a goggles overlay and tooltip for reading it without one, tier-tinted visuals, and a development tool for screenshots and manual checks.

## Scope

In: the motor screen on Create Fly's framework (MM-6), the goggles overlay, item tooltip, shaft visual and tinted block model (MM-7), the development-only `/metered_motor debug` command (MM-8). Out: anything that changes the motor's numbers — this milestone only reads and displays them.

## Exit criteria

- Game tests for the screen's slot rules and its synced readout fields.
- The client-side goggles-overlay attachment mechanism is found and recorded, and Kevin's client check confirms the overlay, tooltip, shaft visual and tier tints.
- The debug command gives a motor of a chosen tier with rolled or explicit stats and a full inventory, proven by a game test and registered only in a development environment.

## Tickets

- MM-6 — The motor screen on Create Fly's framework
- MM-7 — Goggles overlay, tooltip, shaft visual and tinted block model
- MM-8 — Development-only /metered_motor debug command

## Depends on

M1: MM-6 and MM-7 are blocked by MM-4 (the inventory and meter they expose or overlay); MM-8 is blocked by MM-3 (the block entity it targets).
