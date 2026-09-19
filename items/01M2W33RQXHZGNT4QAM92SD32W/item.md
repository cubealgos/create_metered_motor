---
schema_version: 1
id: 01M2W33RQXHZGNT4QAM92SD32W
key: MM-4
type: feat
title: "Inventory and meter: WorldlyContainer, emerald-only insertion, once-a-second burn"
created_by: kevin
created_at: 2026-09-19T05:44:32Z
---

## Scope

The inventory and the meter (`ARCH-DEC-003`): `WorldlyContainer` with five slots accepting only emeralds and emerald blocks through every face (`MOTOR-REQ-008`), `canTakeItemThroughFace` always false so no extraction through any face (`MOTOR-REQ-009`). The once-a-second meter: reads the network's stress over capacity as Create's stress gauge reads it, adds `rate / 60 × min(1, stress / capacity)` to the fractional meter (`MOTOR-REQ-006`, `MOTOR-DEC-001`), takes one emerald when the meter reaches one (splitting an emerald block into nine on the spot), stops the motor when the meter reaches one and the inventory is empty (`MOTOR-REQ-007`, `MOTOR-FAIL-004`), does not advance while the block's chunk is not ticking (`MOTOR-REQ-011`), and treats a capacity-zero network (this motor alone on its network) as zero load so it idles free rather than stalling (`MOTOR-FAIL-002`).

## Approach

`WorldlyContainer` on the block entity from MM-3, reached by vanilla hoppers and by Create's logistics through `AllTransfer.getInventory` (spec `README.md` Verification 4); `getSlotsForFace` returns all five slots for every face, `canPlaceItemThroughFace` checks `Items.EMERALD`/`Items.EMERALD_BLOCK`, `canTakeItemThroughFace` returns false unconditionally. First thing to verify: confirm `AllTransfer.getInventory` and `InvManipulationBehaviour.getInventory()` both resolve a plain `WorldlyContainer` block entity without extra wiring (Verification 4), since a gap here would need a second insertion path. The meter ticks once per second server-side, reading the network the way Create's stress gauge does (`BlockStressValues`/the network accessor read at this ticket).

## Acceptance criteria

- [x] Game test: a hopper above and a Create funnel both insert emeralds and refuse cobblestone (`InsertGameTest`). The funnel half drives the `Container` a funnel inserts through (`AllTransfer.getInventory` → `ItemInventory.insert`), the ticket's fallback: a live funnel needs a belt headless.
- [x] Game test: a hopper below the block extracts nothing (`ExtractGameTest`).
- [x] Game test: under a known network load, the meter takes an emerald after the computed time (`rate`, load and the per-second increment fixed in the test) (`MeterGameTest`). Asserts the per-second fractional advance against rate/60 × load, the documented fallback; the take itself is covered by `SplitGameTest.aFullInventoryOfBlocksStillBurns` (review fix c37906d).
- [x] Game test: inserting an emerald block splits it into nine loose emeralds in the inventory (`SplitGameTest`).
- [x] Game test: the motor stops when the inventory empties and the meter reaches one, and resumes on the next emerald (`EmptyGameTest`).
- [x] Game test: a motor alone on its network (capacity equal to its own) reads zero load and does not advance the meter (`MOTOR-FAIL-002`).

## Constraints and prior findings

`MOTOR-REQ-006..009`, `MOTOR-FAIL-001`, `MOTOR-FAIL-002`, `MOTOR-FAIL-004`, `MOTOR-DEC-001`, `MOTOR-DEC-003`, `ARCH-DEC-003`. Verified: Create Fly reaches block inventories through vanilla `Container` (`AllTransfer.getInventory`; funnels, arms and chutes through `InvManipulationBehaviour.getInventory()`); a `WorldlyContainer` serves vanilla hoppers and Create's logistics alike (spec `README.md` Verification 4). No Fabric Transfer API storage of the mod's own (`ARCH-DEC-003`'s cost-if-wrong: confirm Fabric API's container bridge covers vanilla containers; a mod that only speaks the Transfer API cannot feed this motor, out of scope at 1.0). Blocked by MM-3 (the block entity this ticket's inventory and meter attach to).
