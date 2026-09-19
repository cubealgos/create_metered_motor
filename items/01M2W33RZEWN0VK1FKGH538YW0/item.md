---
schema_version: 1
id: 01M2W33RZEWN0VK1FKGH538YW0
key: MM-8
type: feat
title: Development-only /metered_motor debug command
created_by: kevin
created_at: 2026-09-19T05:44:32Z
---

## Scope

The development-only `/metered_motor debug <tier> [rpm] [capacity] [efficiency]` command (`operations/testing.md`): gives a motor of the chosen tier with chosen or rolled stats (any omitted stat is rolled within its tier's band) and fills its five-slot inventory, for screenshots and screen checks. Registered only when Fabric reports a development environment, as `create_brass_compass`'s `/brass_compass debug`.

## Approach

`metered_motor.debug.DebugCommand` on Fabric's command API, wired from the entrypoint behind `FabricLoader.isDevelopmentEnvironment()`, following `create_brass_compass`'s BC-15 shape: a plain builder method the game test calls directly, wrapped by the command for the dispatcher path. First thing to verify: the command needs a real placed block (not just an item) to fill an inventory on, so it must place or target a block — confirm whether it places one at the player's look target or requires an existing motor under the cursor, and record the choice.

## Acceptance criteria

- [x] Game test: the command, invoked with a tier and no other arguments, places or targets a motor with stats rolled inside that tier's bands and a full inventory; given explicit rpm/capacity/efficiency, the block carries exactly those values (`DebugCommandGameTest`).
- [x] Game test: the command parses and executes through the server's dispatcher on a development server, proving registration (`DebugCommandGameTest`).
- [x] The entrypoint registers the command only behind `FabricLoader.isDevelopmentEnvironment()`, confirmed by code review noted in this ticket.

## Constraints and prior findings

Review 2026-09-19: 13 game tests on the branch merged with development; reviewer's own `just check` green. The command fills a looked-at motor's inventory once MM-4 makes the block entity a `Container` (checked at runtime, no code change needed). Finding: a mock player's `pick()` reads the previous tick's position, so tests call `setOldPosAndRot()` after moving it.

`operations/testing.md`'s development-tool row. Modelled on `create_brass_compass`'s BC-15 (`/brass_compass debug`): registered only in development, a plain builder method shared by the game test and the command, feedback through a translation key. Blocked by MM-3 (a real block entity for the command to target or place).
