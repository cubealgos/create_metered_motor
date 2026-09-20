---
schema_version: 1
id: 01M2YJ66X3Q7RSG4H1CSYEQ52H
key: MM-21
type: feat
title: "Fixed tiers as steam engine set-ups: 64 rpm, capacity per rpm, divisor 92,160, no roll, component version 2"
created_by: kevin
created_at: 2026-09-20T04:46:29Z
---

## Scope

The fixed-tier rework (`decisions/DEC-009-fixed-tiers.md`, Kevin 2026-09-20): no roll; each tier is a steam engine set-up at 64 rpm: I 16,384 SU (one engine on a level-1 boiler), II 65,536 SU (four engines, level 4), III 294,912 SU (the full level-18 boiler); rate = capacity / 92,160 emeralds per minute at full load (0.18 / 0.71 / 3.2); the stats component becomes version 2 `{version, tier}` with migration from version 1 (keep the tier, drop rpm/capacity/efficiency); `calculateAddedStressCapacity()` returns capacity / 64 because Create multiplies by speed (the network saw 153 × the capacity, the goggles showed 1,488,384 su); the trade files carry the tier component in their `gives` template and the roll loot function is withdrawn (`TRADE-REQ-002` withdrawn); the tooltip, the screen and the goggles drop efficiency and the goggles omit their capacity row while Create's generator line shows it; the debug command takes only `I|II|III`. Ids: `MOTOR-REQ-001`, `-004`, `-005`, `-012`, `MOTOR-DEC-003` (amended), `MOTOR-DEC-005`, `TRADE-DEC-002/003` (amended), `DATA-REQ-*`, `UI-REQ-003/005/006`.

## Approach

`just spec-sync` first and read the amended spec. Model: `Tier` gains `rpm()` (64 for all), `capacity()`, `ratePerMinute()`; `Stats` shrinks to `(version, tier)` or is replaced by `Tier` plus the version in the codec (choose the smaller diff; `Stats.readOnly()` semantics for a newer version stay); `Roll`, `Band`, `RollFunction`, `RollGameTest`, `BandTest`, `RollTest` go; `Meter` unchanged; `StatsCodec` reads version 1 (`tier`, ignore the rest) and 2; block entity `getGeneratedSpeed()` returns 64 while running, `calculateAddedStressCapacity()` returns `capacity / 64f`; trade JSONs: `gives` with `components` (verify the 26.2 villager trade codec accepts components on the item template: `javap` the trade/item template codec; if it does not, keep one minimal loot function `metered_motor:tier` that only writes the tier, and record it); `NoMotorOffered` unchanged; `MotorTier.of(ItemStack)` from the component; `StatsText` drops efficiency; screen table rows become Speed / Capacity, Rate / Load, Inside / Remaining (recheck `Layout` and the mock, keep MM-20's margins); `MotorTooltipBehaviour.rows` without Efficiency and, when `super.addToGoggleTooltip` returned true, without Capacity; `MeteredMotorTooltip.lines` without efficiency; debug command `I|II|III` only; `TradeFileGameTest`, `NoDuplicateOfferGameTest`, `MeterGameTest`, `SyncGameTest`, `TooltipGameTest`, `TierPropertyGameTest`, `PlacementGameTest`, `ComponentGameTest` updated; new `CapacityGameTest`: a placed running tier II motor's network capacity (read through Create's `KineticNetwork`/the block entity's `capacity` field) equals 65,536 within rounding, and a millstone load reads the expected fraction; a `MigrationGameTest`: a version-1 component `{version:1, tier:"iii", rpm:.., capacity:.., efficiency:..}` on an item reads as tier III version 2. `docs/modrinth/body.md` numbers and the README updated to the fixed tiers.

## Acceptance criteria

- [ ] Game test: a running tier I/II/III motor contributes 16,384 / 65,536 / 294,912 SU to its network at 64 rpm (`MOTOR-REQ-004`, `CapacityGameTest`).
- [ ] Unit tests: `Tier.ratePerMinute()` is 16384/92160, 65536/92160, 294912/92160; the meter takes an emerald after the computed time under a fixed load (`MOTOR-REQ-005/006`).
- [ ] Game test: version-1 components migrate to version 2 keeping the tier; a version-3 component stays read-only and unknown (`DATA-REQ-*`).
- [ ] Game test: the three toolsmith trade files give tier I/II/III motors with the component set, no roll function referenced anywhere (`TRADE-REQ-001`, `TRADE-REQ-002` withdrawn).
- [ ] Unit test: tooltip, screen rows and goggles rows contain no efficiency; goggles rows omit Capacity when Create's generator line is present (`MOTOR-REQ-012`, `UI-REQ-005`).
- [ ] `docs/spec/` synced; `docs/modrinth/body.md` and `README.md` state the fixed tiers.
- [ ] `just client`: goggles show Create's capacity once and ours agree; the meter burns at the tier's rate under load (Kevin's check).
- [ ] Merged through a Forgejo pull request into `development`.

## Constraints and prior findings

Research note `vault/technical/minecraft/create-fly-steam-engines-26-2.md` (cubealgos layer): `KineticNetwork.getActualCapacityOf(be) = sources.get(be) * abs(getGeneratedSpeed())`. MM-4's prepaid credit and MM-14's unknown-version tooltip stay. The screen layout (MM-18/20) and goggles builders (MM-19) are the current code.

## Findings

**Trade-template verdict: yes, `gives` accepts `components` directly — confirmed by full bytecode
disassembly of the merged-deobf 26.2 jar, not just the Create Fly jar.** `VillagerTrade.gives` is
typed `net.minecraft.world.item.ItemStackTemplate` (`javap -p -c net.minecraft.world.item.trading.VillagerTrade`).
`ItemStackTemplate`'s `MAP_CODEC` lambda (`javap -p -c -constants net.minecraft.world.item.ItemStackTemplate`,
method `lambda$static$0`) builds a `RecordCodecBuilder` group over exactly three fields, string
constants read straight from the constant pool: `Item.CODEC.fieldOf("id")`,
`ExtraCodecs.intRange(1, 99).optionalFieldOf("count", 1)`, and
`DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY)` — the same
`DataComponentPatch` codec every vanilla item-stack `components` block uses (`/give`, loot table
item entries). So `RollFunction` is deleted (not kept as a fallback) and the three trade JSONs
carry `"gives": {"id": "metered_motor:metered_motor", "components": {"metered_motor:stats":
{"version": 2, "tier": 1|2|3}}}` with no `given_item_modifiers` at all — confirmed end-to-end by
`TradeFileGameTest.eachTradeGivesItsFixedTierWithNoItemModifiers`, which calls
`VillagerTrade.getOffer` twice per trade file with two independently-random `LootContext`s and
asserts the two resulting `MerchantOffer`s carry byte-identical `metered_motor:stats` components
(proof nothing is rolled at offer creation any more), plus
`TradeFileGameTest.theRollLootFunctionTypeIsNoLongerRegistered` asserting
`BuiltInRegistries.LOOT_FUNCTION_TYPE.getOptional(metered_motor:roll)` is empty.

**`KineticNetwork.getActualCapacityOf` reconfirmed directly in the Create Fly jar at this
ticket** (not only from the vault note): `javap -p -c com.zurrtum.create.content.kinetics.KineticNetwork`
shows `getActualCapacityOf(be) = sources.get(be).floatValue() * getStressMultiplierForSpeed(be.getGeneratedSpeed())`
and `getStressMultiplierForSpeed(speed) = Math.abs(speed)` verbatim in bytecode — `calculateAddedStressCapacity()`
must return `capacity / 64f` while `getGeneratedSpeed()` returns `64`, which is what
`MeteredMotorBlockEntity.calculateAddedStressCapacity()` now does (`stats.capacity() / (float) stats.rpm()`).

**`CapacityGameTest` proof numbers** (all three tiers, `just gametest`, all passing): for tier
I/II/III, `calculateAddedStressCapacity()` returns `256.0f` / `1024.0f` / `4608.0f` SU per rpm
(`capacity / 64` exactly — every division lands whole since capacity is a multiple of 64 for all
three tiers), `getGeneratedSpeed()` returns `64.0f` for every tier, and the product
(`calculateAddedStressCapacity() * |getGeneratedSpeed()|`) equals the tier's fixed capacity exactly:
`16,384` / `65,536` / `294,912` SU. `motor.networkCapacity()` (the block entity's own synced
`capacity` field, `KineticNetwork`'s cached total) converges to the same three numbers within the
test's 1 SU tolerance, confirming Create's own network machinery — not just the isolated formula —
lands on the tier's advertised capacity.

**A second, more subtle instance of the same class of bug, found and fixed while writing
`CapacityGameTest`/`MeterGameTest`'s millstone-load assertions.** The *load ratio* (`stress /
capacity`) the meter reads was never actually broken by the pre-DEC-009 bug — the network's shared
rpm scaled both a generator's capacity contribution *and* a consumer's stress contribution
identically, so the rpm factor cancelled in the ratio regardless of whether
`calculateAddedStressCapacity()` was correctly scaled. Once `calculateAddedStressCapacity()` was
fixed to `capacity / 64`, the *raw* (unscaled) millstone impact figure `BlockStressValues.getImpact(...)`
no longer has a matching unscaled capacity to divide against — the correct load formula is
`(impact * 64) / capacity`, not `impact / capacity` (the old `MeterGameTest` formula, which only
happened to read correctly before because the network capacity it divided by was *itself* still
64x-inflated by the old bug — two wrongs cancelling). Both `MeterGameTest` and the new
`CapacityGameTest` now use `impact * rpm / capacity`; each carries a comment recording why.

**`docs/spec/` verified in sync** (`just spec-sync`, diff included in the commit): DEC-009,
`domains/motor.md` `MOTOR-DEC-005`, `domains/trade.md`, `domains/ui.md`,
`contracts/data-contract.md` version 2 all read as the ticket's Scope describes.

**Check line:** `just check` green end to end — `verifyPurePackage` and `check` (lint), `map-check`
(17 files current), unit tests (`Ran 4 tests ... OK` for `tools/`), `./gradlew test` (**40 unit
tests, 0 failures**), `./gradlew runGameTest` (**"All 50 required tests passed :)"**, 0 failures).

**Left for Kevin, not done here:** the acceptance criterion `just client: goggles show Create's
capacity once and ours agree; the meter burns at the tier's rate under load` is explicitly Kevin's
own manual check, not automatable — not attempted. The merge-through-a-Forgejo-PR criterion is
also explicitly out of this ticket's scope (`kontor pr`/`kontor merge`, not run). `README.md`
still declares "MIT (LICENSE)" while `CLAUDE.md`/`DEC-003-licence.md` call for GPL-3.0-only — a
pre-existing mismatch, unrelated to MM-21, left untouched. `CHANGELOG.md`'s `[Unreleased]`
section was empty before this ticket and other merged tickets (MM-3 through MM-20) never added
entries either, so none was added here either, matching existing practice — flagging in case that
practice itself should change.
