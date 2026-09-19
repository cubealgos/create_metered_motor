---
schema_version: 1
id: 01M2W33FHHX1QH4VE9BRDEBBMS
key: MM-2
type: feat
title: "Stats model and component: pure package, versioned codec, roll arithmetic"
created_by: kevin
created_at: 2026-09-19T05:44:22Z
---

## Scope

The pure package `metered_motor.model`: a `Stats` record (`tier` 1|2|3, `rpm`, `capacity` in SU, `efficiency`), the rate formula `capacity / 8192 / efficiency` emeralds per minute (`MOTOR-REQ-005`, `MOTOR-DEC-003`), the meter arithmetic `rate / 60 × min(1, stress / capacity)` (`MOTOR-REQ-006`), and the roll's band arithmetic (a uniform draw within `{min, max}` for rpm, capacity and efficiency, `docs/spec/domains/trade.md` §3) with unit tests; and the `metered_motor:stats` data component with persistent and stream codecs mirroring the model (`contracts/data-contract.md`, `DATA-REQ-001..004`, `MOTOR-REQ-001`, `MOTOR-REQ-005`, `MOTOR-REQ-014`, `ARCH-DEC-005`).

## Approach

A record-based model (`Stats`, `Band`, `Roll`) in `metered_motor.model`, no Minecraft imports, checked by `verifyPurePackage`; a Fabric-side `DataComponentType<Stats>` whose codec carries `version` first so a component whose version is newer than the build's is read intact and reported unknown, never edited or placed (`MOTOR-REQ-014`, `DATA-REQ-001`). First thing to verify: the 26.2 data component codec shape (a persistent `Codec` plus a `StreamCodec` for sync) against a small existing Create Fly or vanilla component, since this is the first component the mod registers.

## Acceptance criteria

- [x] Unit tests: the rate formula at the documented bounds (tier I worst case one emerald per ~20 minutes, tier III best case ~0.8/minute, tier III worst case 3/minute, per `docs/spec/domains/trade.md` §3), the meter's per-second increment under a known stress/capacity ratio, a uniform roll landing inside its band, codec round trip, and a newer `version` read back intact and flagged unknown (`StatsTest`, `RollTest`).
- [x] `verifyPurePackage` passes on `metered_motor.model` with no Minecraft imports, and a deliberate import proves the check fails (`TEST-REQ-002`, once).
- [x] The component survives a save round trip through `ItemStack.CODEC` with registry ops in a game test (`StatsComponentGameTest`).

## Constraints and prior findings

Review 2026-09-19: 18 unit tests, 4 game tests, reviewer's own `just check` green; the pure-package break proof done (`TEST-REQ-002`). The data contract was amended so the meter may sit at exactly one while held (`MOTOR-REQ-007`). `Roll` takes raw band numbers so it can reject a malformed band before a `Band` exists; the factor-of-four rule is applied per axis against the tier default.

`docs/spec/contracts/data-contract.md`, `MOTOR-REQ-001`, `MOTOR-REQ-005`, `MOTOR-REQ-014`, `ARCH-DEC-005`, `MOTOR-DEC-003` (the burn divisor, 8,192 SU per emerald-minute, is code, not data). `DATA-REQ-003`: a malformed or missing component reads as unrolled and placement rolls it at the middle of tier I's bands. Modelled on `create_brass_compass`'s BC-2 (`Destinations` component, `verifyPurePackage`, codec round trip via game test). Blocked by MM-1 (the Gradle project and toolchain this ticket's package and tests build on).
