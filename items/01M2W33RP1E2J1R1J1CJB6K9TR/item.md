---
schema_version: 1
id: 01M2W33RP1E2J1R1J1CJB6K9TR
key: MM-3
type: feat
title: "Block and block entity: Create kinetic source, state machine, stats transfer, placeholder model"
created_by: kevin
created_at: 2026-09-19T05:44:32Z
---

## Scope

The block and block entity as a Create kinetic source (`ARCH-DEC-002`): `MeteredMotorBlock extends DirectionalKineticBlock implements IBE`, `MeteredMotorBlockEntity extends GeneratingKineticBlockEntity` overriding `getGeneratedSpeed()` and `calculateAddedStressCapacity()` from the rolled stats while running, else zero (`MOTOR-REQ-002`, `MOTOR-REQ-004`). The state machine stopped/running/paused (`docs/spec/domains/motor.md` §3), redstone pause (`MOTOR-REQ-010`), no burn or time counted while unloaded (`MOTOR-REQ-011`, `ARCH-FAIL-002`). Stats copied item → block on placement and block → item on breaking through `applyImplicitComponents`/`collectImplicitComponents` (`MOTOR-REQ-003`, `DATA-REQ-004`), loot drops the item with its stats and the inventory's contents (`MOTOR-REQ-003`), the block item stackable to one (`MOTOR-REQ-001`), an item with no stats component rolled at the middle of tier I's bands on placement (`MOTOR-FAIL-003`). A placeholder block model and the creative tab entry in Create's base tab.

## Approach

`content.kinetics.motor.CreativeMotorBlock`/`CreativeMotorBlockEntity` (spec `README.md` Verification 3) are the model to read first: same base classes, same `getGeneratedSpeed`/`calculateAddedStressCapacity` override pair. First thing to verify: read `CreativeMotorBlock` and `CreativeMotorBlockEntity` with `javap` (already sampled for the spec) to confirm the exact override signatures and the `KineticNetwork` update call the state transitions must trigger. Build the state machine as an enum field on the block entity, ticked server-side; `applyImplicitComponents`/`collectImplicitComponents` move the `metered_motor:stats` component from MM-2; a placeholder cube model registers the block so MM-4's inventory work has a real block entity to attach to.

## Acceptance criteria

- [ ] Game test: placing from an item copies its stats into the block entity (`PlacementGameTest`).
- [ ] Game test: breaking the block returns exactly one item carrying those stats, and drops the inventory's contents (`BreakGameTest`).
- [ ] Game test: running adds the rolled speed and stress capacity to the network; paused (redstone) and stopped (the state field, directly — the inventory backing "stopped" arrives in MM-4) add nothing (`StateGameTest`).
- [ ] Game test: a redstone signal pauses generation and ending the signal resumes it (`MOTOR-REQ-010`).
- [ ] An item with no stats component places rolled at the middle of tier I's bands (`MOTOR-FAIL-003`), asserted in a game test.
- [ ] The block item is stackable to one; the block appears in Create's base creative tab in `just client` (Kevin's check).

## Constraints and prior findings

`MOTOR-REQ-002..004`, `MOTOR-REQ-010`, `MOTOR-REQ-011`, `MOTOR-FAIL-003`, `ARCH-DEC-002`, `ARCH-DEC-005`, `DATA-REQ-004`. Verified: `content.kinetics.motor.CreativeMotorBlock extends DirectionalKineticBlock implements IBE`, `CreativeMotorBlockEntity extends GeneratingKineticBlockEntity`; `KineticBlockEntity` exposes `getGeneratedSpeed()`, `calculateAddedStressCapacity()`, `getOrCreateNetwork()`, `hasNetwork()`; `KineticNetwork` holds `currentStress`/`currentCapacity` (spec `README.md` Verification 3). Block entities move components via `applyImplicitComponents(DataComponentGetter)`/`collectImplicitComponents(DataComponentMap.Builder)` (Verification 7). Blocked by MM-2 (the stats component this ticket transfers).
