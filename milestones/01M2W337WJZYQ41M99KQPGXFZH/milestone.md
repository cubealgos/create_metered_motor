---
schema_version: 1
id: 01M2W337WJZYQ41M99KQPGXFZH
key: M1
title: "The block: kinetics, stats, inventory, meter"
status: todo
created_at: 2026-09-19T05:44:14Z
---

## Goal

A metered motor block that exists as a Create kinetic source: rolled stats travel between item and block, the block turns a shaft and adds to a network's stress while running, and an inventory of emeralds feeds a meter that burns at a rate proportional to load.

## Scope

In: the pure stats model and the `metered_motor:stats` component (MM-2), the block and block entity as a Create kinetic source with its state machine (MM-3), the `WorldlyContainer` inventory and the once-a-second meter (MM-4). Out: how a motor is bought (M2) and how it is read or shown (M3).

## Exit criteria

- Game tests for placing (copies stats), breaking (returns them), running (adds speed and capacity to a network), and stopped/paused (add nothing).
- Game tests for insertion by hopper and Create funnel, refusal of non-emeralds, no extraction through any face, the meter taking an emerald after the computed time under a known load, an emerald block splitting, and stopping when empty.

## Tickets

- MM-2 — Stats model and component: pure package, versioned codec, roll arithmetic
- MM-3 — Block and block entity: Create kinetic source, state machine, stats transfer, placeholder model
- MM-4 — Inventory and meter: WorldlyContainer, emerald-only insertion, once-a-second burn

## Depends on

M0: MM-1 bootstraps the Gradle project and toolchain that MM-2 through MM-4 build on; MM-1 blocks MM-2.
