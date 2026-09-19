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

## Requirement-to-test table

| ID | Test class and method | Notes |
|---|---|---|
| `MOTOR-REQ-001` | `RegistrationGameTest.theBlockAndItemResolveAtTheirIdsAndTheItemIsStackableToOne` (block/item ids, stack-to-one, MM-9); `StatsTest`, `ComponentGameTest` (the stats component's shape); exercised throughout `PlacementGameTest`, `BreakGameTest`, `StateGameTest` | The stack-to-one clause had no direct assertion before MM-9; added. |
| `MOTOR-REQ-002` | `StateGameTest.fuelAndRedstoneDriveTheStateMachineAndTheNetwork` | The block extends Create's `DirectionalKineticBlock` unchanged; the test proves the shaft on the facing side turns while running. Placement-orientation logic itself is Create's own, inherited, not this mod's to test. |
| `MOTOR-REQ-003` | `PlacementGameTest.placingFromAnItemCopiesItsStats`; `BreakGameTest.breakingDropsExactlyOneItemWithTheSameStats` | |
| `MOTOR-REQ-004` | `StateGameTest.fuelAndRedstoneDriveTheStateMachineAndTheNetwork` | |
| `MOTOR-REQ-005` | `StatsTest.rateFollowsCapacityAndEfficiencyAtTheSpecsBoundExamples` | |
| `MOTOR-REQ-006` | `MeterTest.takesExactlyOneEmeraldAfterTheComputedNumberOfSecondsUnderAFixedLoad` (unit); `MeterGameTest.aKnownConsumerAdvancesTheMeterByTheComputedFraction`; `SplitGameTest.takingFromAnEmeraldBlockLeavesEightLooseEmeralds`, `aFullInventoryOfBlocksStillBurns` | |
| `MOTOR-REQ-007` | `MeterTest.holdAtOneLeavesTheMeterAtOneForTheNextArrival` (unit arithmetic); `EmptyGameTest.theLastEmeraldStopsTheMotorAndTheNextResumesIt` (end to end) | |
| `MOTOR-REQ-008` | `InsertGameTest.aHopperAboveInsertsEmeraldsAndRefusesCobblestone`, `createsLogisticsInsertThroughTheSameContainerAndRefuseCobblestone`; `SlotRulesGameTest.theFiveSlotsAcceptOnlyEmeraldsAndEmeraldBlocks`, `quickMoveFromThePlayerInventoryRefusesCobblestoneAndAcceptsEmeralds` | |
| `MOTOR-REQ-009` | `ExtractGameTest.aHopperBelowExtractsNothing` (no face extracts); `SlotRulesGameTest.quickMoveFromAMotorSlotMovesEmeraldsIntoThePlayerInventory` (a player in the screen does, MM-9) | The positive half — the screen actually lets a player take emeralds out — had no test before MM-9; added. |
| `MOTOR-REQ-010` | `StateGameTest.fuelAndRedstoneDriveTheStateMachineAndTheNetwork` | |
| `MOTOR-REQ-011` | GAP, not practically testable headless | `MeteredMotorBlockEntity.tick()` (line ~90) burns only inside its own `tick()`, which Minecraft calls only while the chunk ticks; this is the vanilla `BlockEntity` tick contract, not mod logic, and `GameTestHelper`'s bounded, always-force-loaded test region has no supported way to simulate an unloaded chunk. Verified by code inspection, not a dedicated test. |
| `MOTOR-REQ-012` | client checklist: the item tooltip and the goggles overlay, both content and that the overlay appends after Create's own lines | `MeteredMotorTooltip` registers on `ItemTooltipCallback` (client-only Fabric API) and `MotorTooltipBehaviour` extends a `com.zurrtum.create.client...` class — neither loads on the dedicated server a game test runs on, so this cannot be proven headless (`docs/spec/operations/testing.md`'s own Client row). The numbers shown go through the same `StatsText` formatting `SyncGameTest` already exercises against the block entity's real values. |
| `MOTOR-REQ-013` | `ModelAssetsTest` (every model and texture reference resolves, the six recoloured textures are 16×16); `TierStateGameTest` (the `tier` block state is set correctly at placement and survives break/replace) for the model-selection mechanism; client checklist for the actual in-game colours and shaft rendering | |
| `MOTOR-REQ-014` | `ComponentGameTest.aNewerVersionReadsBackIntactAndReadOnly`, `StatsTest.aVersionNewerThanThisBuildIsReadOnly` (kept intact, flagged read-only); `PlacementGameTest.aNewerStatsVersionRefusesPlacement` (refuses placement, MM-9) | **Does not fully hold**: the "shown as unknown" clause has no implementation found — `MeteredMotorTooltip` only special-cases a `null` stats component (`MOTOR-FAIL-003`'s "unrolled"), never `stats.readOnly()`; a read-only item's real numbers would print as if valid. Flagged for Kevin, not fixed here (out of this test-sweep ticket's scope, and the display half is client-only regardless). |
| `MOTOR-FAIL-001` | `InsertGameTest` (face); `SlotRulesGameTest` (screen slot) | |
| `MOTOR-FAIL-002` | `MeterGameTest.noConsumerReadsZeroLoadAndDoesNotAdvanceTheMeter` | |
| `MOTOR-FAIL-003` | `PlacementGameTest.anItemWithNoStatsPlacesRolledAtTheMiddleOfTierI` (the roll half); the tooltip's "unrolled" text is client checklist, same reasoning as `MOTOR-REQ-012` | |
| `MOTOR-FAIL-004` | `EmptyGameTest.theLastEmeraldStopsTheMotorAndTheNextResumesIt` | |
| `TRADE-REQ-001` | `TradeFileGameTest.theThreeTradesResolveAndAreTaggedIntoTheirLevel` | |
| `TRADE-REQ-002` | `RollGameTest.aTierIiRollLandsWithinItsBands`; `RollTest.aUniformRollLandsInsideItsTiersDefaultBand` (unit) | |
| `TRADE-REQ-003` | `TradeFileGameTest.theThreeTradesResolveAndAreTaggedIntoTheirLevel` | |
| `TRADE-REQ-004` | `RollGameTest.aMalformedBandFallsBackToTheTierDefaultAndLogs`; `RollTest.aBandWhoseMinimumExceedsItsMaximumFallsBackToTheTiersDefaultAndIsReported`, `aBandFarOutsideTheTiersDefaultByMoreThanAFactorOfFourFallsBackAndIsReported`, `aBandWithinFourTimesTheTiersDefaultIsHonoured` | |
| `TRADE-REQ-005` | client checklist: the trade screen's tooltip | Same client-only `MeteredMotorTooltip` mechanism as `MOTOR-REQ-012`. |
| `TRADE-REQ-006` | `NoDuplicateOfferGameTest.aVillagerAlreadyOfferingAMotorRefusesASecond` | |
| `UI-REQ-001` | client checklist: the screen's frame, textures and layout | `MeteredMotorScreen` is client rendering; the menu it sits on (`MeteredMotorMenu`, five real slots plus player inventory) is exercised by `SlotRulesGameTest` and `SyncGameTest`, but the frame itself is not headless-testable. |
| `UI-REQ-002` | `SlotRulesGameTest.theFiveSlotsAcceptOnlyEmeraldsAndEmeraldBlocks`, `quickMoveFromThePlayerInventoryRefusesCobblestoneAndAcceptsEmeralds` | |
| `UI-REQ-003` | `SyncGameTest.theReadoutFieldsMatchTheBlockEntityAfterABurnCycle`, `anIdleMotorFormatsAsIdleNotAsATime` | |
| `UI-REQ-004` | client checklist: line wrapping/truncation against the panel | Layout rendering, client-only. |
| `UI-REQ-005` | client checklist: goggles overlay ordering after Create's lines | `MotorTooltipBehaviour` calls `super.addToGoggleTooltip` before appending, provable only by code inspection (see `MOTOR-REQ-012`'s note on why this class cannot load headless). |
| `UI-REQ-006` | client checklist: the item tooltip's rolled-stats and "unrolled" text | Same client-only mechanism as `MOTOR-REQ-012`/`MOTOR-FAIL-003`. |
| `UI-REQ-007` | `SourceSurfaceTest.everyTranslationKeyNamedInCodeHasAnEnglishEntry` (every string is a translation key with an `en_us` entry) | **Possible spec/code mismatch, flagged, not resolved**: the requirement's second clause says numbers are "formatted by the client's locale," but `StatsText`'s own Javadoc says the opposite on purpose — every number goes through `Locale.ROOT` "so a client's own locale never turns a decimal point into a comma mid-readout." One of the two is wrong; a design question for Kevin, not decided here. |
| `UI-REQ-008` | `ExtractGameTest.aHopperBelowExtractsNothing` (no other path takes emeralds out); `SlotRulesGameTest.quickMoveFromAMotorSlotMovesEmeraldsIntoThePlayerInventory` (the screen does, MM-9) | |
| `COMP-REQ-001` | `SourceSurfaceTest.noNetworkingTypeIsReferencedByTheMod` | |
| `TEST-REQ-002` | Done once under MM-2 (2026-09-19): a deliberate Minecraft import was added to `metered_motor.model`, `verifyPurePackage` failed as required, then reverted; recorded in MM-2's item.md checklist. Restated here per `TEST-REQ-001`, not repeated as a standing test (the requirement's own wording is "once"). | |

## Verification runs

Three consecutive `just check` runs on this branch (`feature/mm-9-test-sweep`, worktree
`.worktrees/mm-9`), each preceded by `./gradlew clean`, 2026-09-19:

| Run | Result | Game test count line | Wall-clock |
|---|---|---|---|
| 1 | green | `All 34 required tests passed :)` | 15 s |
| 2 | green | `All 34 required tests passed :)` | 11 s |
| 3 | green | `All 34 required tests passed :)` | 10 s |

No flake across the three runs. 34 game tests is 31 (pre-MM-9) plus the three added under this
ticket (`RegistrationGameTest`, plus one method each on `PlacementGameTest` and
`SlotRulesGameTest`).

## Client checklist (Kevin)

- [ ] `/metered_motor debug I` gives a rolled tier I motor; place it, feed it, confirm it runs.
- [ ] `/metered_motor debug II` gives a rolled tier II motor; place it, feed it, confirm it runs.
- [ ] `/metered_motor debug III` gives a rolled tier III motor; place it, feed it, confirm it runs.
- [ ] The three tiers' casing colours read correctly: andesite grey (I), brass (II), gold (III), dark inner parts unchanged.
- [ ] The shaft turns with Create's kinetic visuals while running, and stops when paused or stopped.
- [ ] Goggles overlay: the readout appears after Create's own kinetic lines (speed, stress), in the documented order (tier, rpm, capacity, efficiency, rate, load, emeralds, remaining/idle, state).
- [ ] Item tooltip: rolled stats show correctly; an unrolled item (creative tab / `/give`) says "unrolled"; a read-only (future-version) item's tooltip — check whether it actually shows "unknown" as the spec requires (flagged above as possibly unimplemented).
- [ ] The motor screen: five slots, the player inventory below, the readout panel; no line overflows the frame.
- [ ] The screen refuses a shift-click of a non-emerald item from the player inventory, and refuses placing one directly into a slot.
- [ ] The screen's readout lines stay within the panel and match the live load and remaining time as the motor burns.
- [ ] A real toolsmith trade: level a villager to toolsmith Journeyman (level 3), confirm exactly one metered-motor offer appears with a rolled tooltip, buy it, place it, feed it, and watch both Create's stress gauge and this mod's meter respond together.
- [ ] A redstone pulse pauses a running motor (shaft stops, gauge and meter freeze) and releasing it resumes.
- [ ] An empty motor (no emeralds) sits stopped; feeding it starts it.

## Constraints and prior findings

`TEST-REQ-001`, `operations/testing.md`. `TEST-REQ-002` (the deliberate-break proof for `verifyPurePackage`) is already satisfied under MM-2 and only needs restating in the table here. Blocked by MM-5, MM-6 and MM-7 — the trade, screen and goggles/visual work this sweep proves.
