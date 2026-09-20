---
title: "create_metered_motor spec — MOTOR: the block, its stats, its inventory and its meter"
type: "spec"
category: "create_metered_motor"
---

# `MOTOR` — the metered motor

## 1. Purpose

The block and its item: the fixed per-tier stats, how it turns Create's network, how it burns
emeralds, what it accepts, how it moves. Not how it is bought (`domains/trade.md`) and not its
screen or overlay (`domains/ui.md`).

## 2. Dimensions

| Dimension | Answer |
|---|---|
| **Actors** | Any player (`ACTORS-001`, `-002`) places, feeds, empties and breaks it; the server (`ACTORS-004`) runs the meter and the rotation; Create Fly (`ACTORS-005`) is the network it powers and the logistics that feed it; a hopper is the game's. No owner. |
| **Over time** | An item carrying a tier becomes a block with the same tier; the block is stopped until fed, runs while fed and unsignalled, burns in proportion to load, stops when empty, pauses under redstone, and becomes the same item again when broken. The tier never changes after purchase, and every motor of a tier is identical (`decisions/DEC-009-fixed-tiers.md`). The inventory and the meter are the block's only mutable state. |
| **Multiplicity** | One block, one item, three tiers. One inventory of five slots. Zero emeralds means stopped; 320 emeralds (five stacks of emerald blocks would be 2,880) means hours of running. A network may hold many motors, all at the same 64 rpm, so their capacities add like stacked steam engines (`DEC-009`); each meters its own share of the load. |
| **Unwanted** | Anything but emeralds and emerald blocks in a slot; automation pulling emeralds out; a client asking for stats or emeralds outside a menu; a motor running for free; a motor burning while paused, stopped or unloaded; a datapack setting a burn rate of zero (the formula is code). |
| **Not-you** | A player who never opens the screen still gets a working motor: hopper on top, shaft in front. A player with goggles reads the meter without a screen. A server admin sees a normal container in the save. A pack author retunes numbers in the trade files without touching the burn. |

## 3. Enumerations

### Motor state

| from ↓ / to → | `stopped` | `running` | `paused` |
|---|---|---|---|
| **`stopped`** (no emeralds) | — | ✅ an emerald arrives and no signal | ✅ a signal while empty (no effect until fed) |
| **`running`** | ✅ the meter takes the last emerald and finds none | — | ✅ a redstone signal |
| **`paused`** (signal) | ✅ the signal ends while empty | ✅ the signal ends with emeralds inside | — |

Generated speed is fixed at 64 rpm and added capacity is the tier's fixed capacity in `running`,
zero otherwise (`MOTOR-REQ-004`, `DEC-009`). The meter advances only in `running`.

### Cardinality

| Relation | Count | At zero | At the top |
|---|---|---|---|
| block → stats | exactly 1 (tier only, version 2) | illegal; a block without stats is the missing-component case (`DATA-REQ-003`) | N/A |
| block → slots | exactly 5, emeralds or emerald blocks | stopped | 5 × 64 blocks = 2,880 emeralds |
| block → meter | one fraction 0 ≤ m < 1 | fresh block | N/A |
| network → motors | 0..n | Create's idle network | each burns its own share; all at 64 rpm, so capacities add |
| item → stats | exactly 1 (tier only, version 2) | an item without a stats component defaults to tier I (`MOTOR-FAIL-003`) | N/A |

## 4. Use cases

`UC-002` to `UC-007` in `02-journeys.md`.

## 5. Requirements

| ID | Requirement | Priority | From |
|---|---|---|---|
| `MOTOR-REQ-001` | The system shall provide the block `metered_motor:metered_motor` and its block item, stackable to one, whose stats component holds only the tier (`contracts/data-contract.md`, component version 2). | Must | `DEC-006`, `DEC-009` |
| `MOTOR-REQ-002` | The block shall be a Create directional kinetic source placed like Create's creative motor, with its shaft on the facing side. | Must | `ARCH-DEC-002` |
| `MOTOR-REQ-003` | **When** the block is placed from an item, the system shall copy the item's stats into the block entity; **when** it is broken, drop exactly one item with those stats and drop the inventory's contents. | Must | `UC-006` |
| `MOTOR-REQ-004` | **While** running, the system shall generate 64 rpm and add the tier's fixed stress capacity to the network; **while** stopped or paused, generate nothing and add nothing, and tell the network on the change. Create's `KineticNetwork` sums a source's contribution as `calculateAddedStressCapacity() × abs(getGeneratedSpeed())` (confirmed by full bytecode disassembly, `vault/technical/minecraft/create-fly-steam-engines-26-2.md` §B) — stress capacity is wanted **per rpm**, not as a total — so `calculateAddedStressCapacity()` shall return the tier's capacity divided by 64 (16,384/65,536/294,912 ÷ 64 = 256/1,024/4,608 SU per rpm for I/II/III) while `getGeneratedSpeed()` returns 64; the product the network sums is then exactly the tier's capacity. | Must | `UC-002`, `UC-005`, `DEC-009` |
| `MOTOR-REQ-005` | The system shall compute the rate as `capacity / 92,160` emeralds per minute at full load, fixed per tier (0.18 / 0.71 / 3.2 a minute for I/II/III) and constant for the block's life; tier III at full load stays at the one-stack-a-day ceiling (64 emeralds per Minecraft day). | Must | `DEC-007`, `DEC-009`, Kevin 2026-09-20 |
| `MOTOR-REQ-006` | **While** running, once per second, the system shall add `rate / 60 × min(1, stress / capacity of the network)` to the meter; **when** the meter reaches one, take one emerald from the inventory (breaking an emerald block into nine on the spot) and subtract one. | Must | `UC-003` |
| `MOTOR-REQ-007` | **When** the meter reaches one and the inventory holds no emerald, the system shall stop the motor and leave the meter at one, so the next emerald is taken on arrival. | Must | `UC-003` |
| `MOTOR-REQ-008` | The inventory shall accept only emeralds and emerald blocks, through every face, from players, hoppers and Create's logistics. | Must | `UC-004` |
| `MOTOR-REQ-009` | The inventory shall refuse extraction through any face; only a player in the screen takes emeralds out. | Must | `UC-004` |
| `MOTOR-REQ-010` | **While** the block receives a redstone signal, the system shall pause it: no generation, no burn. | Must | `UC-005` |
| `MOTOR-REQ-011` | **When** the block's chunk is not ticking, the system shall neither burn nor count time; the meter resumes where it was. | Must | `ARCH-FAIL-002` |
| `MOTOR-REQ-012` | The item's tooltip and Create's goggles overlay on the block shall show tier, 64 rpm, capacity, rate, load, emeralds inside, the remaining time at the current load and the state; no efficiency row. **Where** Create's own goggle line for the block already shows "Generator Stats" (kinetic capacity at current speed), the overlay shall omit this mod's own capacity row rather than show it twice (`DEC-009`). | Should | `UC-007` |
| `MOTOR-REQ-013` | The block shall use Create's creative motor model geometry and its textures with the creative magenta replaced by the tier's colour (andesite grey for I, brass for II, gold for III), the dark inner parts unchanged, and render its shaft turning with Create's kinetic visuals. | Should | `MOTOR-DEC-004` |
| `MOTOR-REQ-014` | **Where** a component's version is newer than the build's, the system shall keep it intact, show the item as unknown and refuse placement. | Must | `contracts/data-contract.md` |

## 6. Failure modes

| ID | Failure | Response |
|---|---|---|
| `MOTOR-FAIL-001` | A non-emerald item is pushed in | Refused at the face; a player's click in the screen is refused by the slot. |
| `MOTOR-FAIL-002` | The network's capacity is zero while running (only this motor, nothing attached) | Load is zero; the meter does not advance; the motor idles free, as a water wheel would. |
| `MOTOR-FAIL-003` | An item without a stats component (creative tab, `/give`) | Defaults to tier I (`DATA-REQ-003`); placement sets the block entity to tier I so nothing is free or broken. |
| `MOTOR-FAIL-004` | The last emerald is taken mid-second | The motor stops on that tick; the network updates once. |

## 7. Open questions

| Question | Blocks | Decided by |
|---|---|---|
| (none open; the edges were ruled on 2026-09-19, `DEC-008`) | | |

## 8. Decisions

- `MOTOR-DEC-001` — **Metered by load, not by time** (Kevin, 2026-09-19): the burn follows the
  network's stress over capacity, read once a second as Create's stress gauge reads it. An idle
  network costs nothing. **Cost if wrong:** the ratio is the network's, not the motor's. Two metered motors on one
  network each burn their own rate times the same ratio, which splits the bill by capacity; a
  metered motor beside a free water wheel pays the ratio for load the wheel covers as well.
- `MOTOR-DEC-002` — **Empty stops, redstone pauses, emeralds and blocks, insert-only automation**
  (Kevin, 2026-09-19, `DEC-008`).
- `MOTOR-DEC-003` — **The rate divisor is 8,192 SU per emerald-minute** (Kevin, 2026-09-19: the
  luckiest draw must run sustainably, at most one stack of emeralds per Minecraft day). A
  full-boiler roll at the worst efficiency burns 3 emeralds a minute at full load, 60 a day; the
  best tier III roll 0.8 a minute; a water-wheel-class roll one every three to twenty minutes.
  **Cost if wrong:** a number in code; a rebalance is a release, not a datapack (`ARCH-DEC-004`).
  **Amended (Kevin, 2026-09-20, `DEC-009`):** the 8,192 estimate and the "3/min, 60/day" worked
  example above were wrong by a factor of 12 — `vault/technical/minecraft/create-fly-steam-engines-26-2.md`
  reads the real full-boiler figure (18 engines, a level-18 boiler) out of the 26.2 jar as 294,912
  SU, not the roughly 24,576 SU the 8,192-divisor example assumed. The divisor is now **92,160**,
  solved from `294912 / D × 20 = 64` (the one-stack-a-day bound applied exactly, not "under"), and
  applies uniformly across all three tiers (`MOTOR-REQ-005`).
- `MOTOR-DEC-004` — **The model is Create's creative motor, recoloured per tier** (Kevin,
  2026-09-19): the creative motor's geometry is unused in survival, so a survival motor wearing it
  reads as Create's own; the only difference is the casing colour, which is the tier. The textures
  are copied from Create Fly (CC0; upstream Create MIT) into this mod's assets, recoloured by a
  script under `tools/`, and credited in `NOTICE`. **Cost if wrong:** three texture files and a
  model copy; a later model of our own replaces them without touching code.
- `MOTOR-DEC-005` — **Fixed tiers as engine set-ups, fixed rpm** (Kevin, 2026-09-20, `decisions/DEC-009-fixed-tiers.md`):
  "when we have to get into this anyway let's make it not random: each tier should be as powerful
  as a specific fitting steam engine + consume appropriate emeralds," and "also make them all
  output the same rpm as a steam engine does; a user there needs to change speed manually too."
  Tier I is one engine on a level-1 boiler (16,384 SU), tier II is four engines on a level-4 boiler
  (65,536 SU), tier III is the full level-18 boiler with 18 engines (294,912 SU); all three at
  64 rpm, the engine's own active speed. Withdraws the rolled rpm/capacity/efficiency design
  (`DEC-006`, `TRADE-DEC-002`) for these three fixed points. "Fixed rpm also makes multi motor
  setups easier which fits the QoL vibe better" (Kevin) — Create generators at different speeds on
  one shaft conflict; identical rpm lets motors stack like engines, and their capacities add.
  **Cost if wrong:** the ladder is three numbers in code, same footprint as the old bands; a later
  tier or a reshuffled ladder is a release, not a datapack (`ARCH-DEC-004`).
