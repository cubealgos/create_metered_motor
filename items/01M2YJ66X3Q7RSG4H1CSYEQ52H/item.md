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
