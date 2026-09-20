---
title: "create_metered_motor spec — testing"
type: "spec"
category: "create_metered_motor"
---

# Testing (`TEST`)

| Layer | What | Where |
|---|---|---|
| Unit | The stats codec and its migrations (including version 1 → 2), the tier→capacity/rate lookup, the meter's arithmetic under load; no Minecraft needed for the pure parts, which live in a package with no Minecraft imports and a build check, `verifyPurePackage` | `src/test` |
| Game tests | Placing from an item copies stats into the block entity and breaking returns them; running adds capacity and speed to a network, stopped adds nothing; **the network's added capacity equals the tier's fixed capacity exactly** — the game test reads `KineticNetwork`'s total after joining and asserts it equals 16,384 / 65,536 / 294,912 for tier I/II/III, proving `calculateAddedStressCapacity()` (capacity ÷ 64) and `getGeneratedSpeed()` (64) multiply back to the tier's capacity and not 64× or 1/64× it (`MOTOR-REQ-004`, `DEC-009`); **a version-1 saved component (`{version:1, tier, rpm, capacity, efficiency}`) loaded by a version-2 build migrates to `{version:2, tier}`, keeps the tier, drops rpm/capacity/efficiency, and the block entity then behaves as a normal motor of that tier** (`DATA-REQ-002`, `contracts/data-contract.md`); the meter takes an emerald after the computed time under a known load, splits an emerald block on the spot, stops when the inventory is empty; hoppers and a Create funnel insert emeralds and refuse other items; nothing can extract; redstone pauses; the trade files parse, the tags contain them, and a mock toolsmith offer's `gives` template carries the tier's fixed component | `src/gametest`, Loom `runGameTest` |
| Client | The screen, the goggles overlay, the tinted block model: Kevin's checklist per release (screens and overlays cannot be game-tested headless) | release checklist |
| Development tool | `/metered_motor debug <tier>` gives a motor of the chosen tier and fills it, for screenshots and screen checks; registered only when Fabric reports a development environment, as `create_brass_compass`'s `/brass_compass debug` | `metered_motor.debug`, `just client` |

`TEST-REQ-001`: every `MOTOR-REQ`, `TRADE-REQ` and `UI-REQ` names its test in the ticket that
implements it.
`TEST-REQ-002`: a deliberate-break proof for the pure-package check, once.
