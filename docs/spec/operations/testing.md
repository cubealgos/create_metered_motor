---
title: "create_metered_motor spec — testing"
type: "spec"
category: "create_metered_motor"
---

# Testing (`TEST`)

| Layer | What | Where |
|---|---|---|
| Unit | The stats codec and its migrations, the roll's band arithmetic, the meter's arithmetic under load and efficiency; no Minecraft needed for the pure parts, which live in a package with no Minecraft imports and a build check, `verifyPurePackage` | `src/test` |
| Game tests | Placing from an item copies stats into the block entity and breaking returns them; running adds capacity and speed to a network, stopped adds nothing; the meter takes an emerald after the computed time under a known load, splits an emerald block on the spot, stops when the inventory is empty; hoppers and a Create funnel insert emeralds and refuse other items; nothing can extract; redstone pauses; the trade files parse, the tags contain them, and a mock toolsmith offer carries a roll within its band | `src/gametest`, Loom `runGameTest` |
| Client | The screen, the goggles overlay, the tinted block model: Kevin's checklist per release (screens and overlays cannot be game-tested headless) | release checklist |
| Development tool | `/metered_motor debug <tier> [rpm] [capacity] [efficiency]` gives a motor of the chosen tier with chosen or rolled stats and fills it, for screenshots and screen checks; registered only when Fabric reports a development environment, as `create_brass_compass`'s `/brass_compass debug` | `metered_motor.debug`, `just client` |

`TEST-REQ-001`: every `MOTOR-REQ`, `TRADE-REQ` and `UI-REQ` names its test in the ticket that
implements it.
`TEST-REQ-002`: a deliberate-break proof for the pure-package check, once.
