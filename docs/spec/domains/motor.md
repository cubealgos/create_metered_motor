---
title: "create_metered_motor spec — MOTOR: the block, its stats, its inventory and its meter"
type: "spec"
category: "create_metered_motor"
---

# `MOTOR` — the metered motor

## 1. Purpose

The block and its item: the rolled stats, how it turns Create's network, how it burns emeralds,
what it accepts, how it moves. Not how it is bought (`domains/trade.md`) and not its screen or
overlay (`domains/ui.md`).

## 2. Dimensions

| Dimension | Answer |
|---|---|
| **Actors** | Any player (`ACTORS-001`, `-002`) places, feeds, empties and breaks it; the server (`ACTORS-004`) runs the meter and the rotation; Create Fly (`ACTORS-005`) is the network it powers and the logistics that feed it; a hopper is the game's. No owner. |
| **Over time** | An item with fixed stats becomes a block with the same stats; the block is stopped until fed, runs while fed and unsignalled, burns in proportion to load, stops when empty, pauses under redstone, and becomes the same item again when broken. Stats never change after the roll. The inventory and the meter are the block's only mutable state. |
| **Multiplicity** | One block, one item, three tiers. One inventory of five slots. Zero emeralds means stopped; 320 emeralds (five stacks of emerald blocks would be 2,880) means hours of running. A network may hold many motors; each meters its own share of the load. |
| **Unwanted** | Anything but emeralds and emerald blocks in a slot; automation pulling emeralds out; a client asking for stats or emeralds outside a menu; a motor running for free; a motor burning while paused, stopped or unloaded; a datapack setting a burn rate of zero (the formula is code). |
| **Not-you** | A player who never opens the screen still gets a working motor: hopper on top, shaft in front. A player with goggles reads the meter without a screen. A server admin sees a normal container in the save. A pack author retunes numbers in the trade files without touching the burn. |

## 3. Enumerations

### Motor state

| from ↓ / to → | `stopped` | `running` | `paused` |
|---|---|---|---|
| **`stopped`** (no emeralds) | — | ✅ an emerald arrives and no signal | ✅ a signal while empty (no effect until fed) |
| **`running`** | ✅ the meter takes the last emerald and finds none | — | ✅ a redstone signal |
| **`paused`** (signal) | ✅ the signal ends while empty | ✅ the signal ends with emeralds inside | — |

Generated speed and added capacity are the rolled values in `running`, zero otherwise. The meter
advances only in `running`.

### Cardinality

| Relation | Count | At zero | At the top |
|---|---|---|---|
| block → stats | exactly 1 | illegal; a block without stats is the missing-component case (`DATA-REQ-003`) | N/A |
| block → slots | exactly 5, emeralds or emerald blocks | stopped | 5 × 64 blocks = 2,880 emeralds |
| block → meter | one fraction 0 ≤ m < 1 | fresh block | N/A |
| network → motors | 0..n | Create's idle network | each burns its own share |
| item → stats | exactly 1 | a creative-tab item with no roll shows tier and "unrolled" (`MOTOR-FAIL-003`) | N/A |

## 4. Use cases

`UC-002` to `UC-007` in `02-journeys.md`.

## 5. Requirements

| ID | Requirement | Priority | From |
|---|---|---|---|
| `MOTOR-REQ-001` | The system shall provide the block `metered_motor:metered_motor` and its block item, stackable to one, whose stats component holds tier, rpm, stress capacity and efficiency (`contracts/data-contract.md`). | Must | `DEC-006` |
| `MOTOR-REQ-002` | The block shall be a Create directional kinetic source placed like Create's creative motor, with its shaft on the facing side. | Must | `ARCH-DEC-002` |
| `MOTOR-REQ-003` | **When** the block is placed from an item, the system shall copy the item's stats into the block entity; **when** it is broken, drop exactly one item with those stats and drop the inventory's contents. | Must | `UC-006` |
| `MOTOR-REQ-004` | **While** running, the system shall generate the rolled rpm and add the rolled stress capacity to the network; **while** stopped or paused, generate nothing and add nothing, and tell the network on the change. | Must | `UC-002`, `UC-005` |
| `MOTOR-REQ-005` | The system shall compute the rate as `capacity / 8192 / efficiency` emeralds per minute at full load and keep it fixed for the block's life; the largest possible roll burns under one stack of emeralds per Minecraft day at full load. | Must | `DEC-007`, Kevin 2026-09-19 |
| `MOTOR-REQ-006` | **While** running, once per second, the system shall add `rate / 60 × min(1, stress / capacity of the network)` to the meter; **when** the meter reaches one, take one emerald from the inventory (breaking an emerald block into nine on the spot) and subtract one. | Must | `UC-003` |
| `MOTOR-REQ-007` | **When** the meter reaches one and the inventory holds no emerald, the system shall stop the motor and leave the meter at one, so the next emerald is taken on arrival. | Must | `UC-003` |
| `MOTOR-REQ-008` | The inventory shall accept only emeralds and emerald blocks, through every face, from players, hoppers and Create's logistics. | Must | `UC-004` |
| `MOTOR-REQ-009` | The inventory shall refuse extraction through any face; only a player in the screen takes emeralds out. | Must | `UC-004` |
| `MOTOR-REQ-010` | **While** the block receives a redstone signal, the system shall pause it: no generation, no burn. | Must | `UC-005` |
| `MOTOR-REQ-011` | **When** the block's chunk is not ticking, the system shall neither burn nor count time; the meter resumes where it was. | Must | `ARCH-FAIL-002` |
| `MOTOR-REQ-012` | The item's tooltip and Create's goggles overlay on the block shall show tier, rpm, stress capacity, efficiency and rate; the overlay adds load, emeralds inside and the remaining time at the current load. | Should | `UC-007` |
| `MOTOR-REQ-013` | The block shall use Create's creative motor model geometry and its textures with the creative magenta replaced by the tier's colour (andesite grey for I, brass for II, gold for III), the dark inner parts unchanged, and render its shaft turning with Create's kinetic visuals. | Should | `MOTOR-DEC-004` |
| `MOTOR-REQ-014` | **Where** a component's version is newer than the build's, the system shall keep it intact, show the item as unknown and refuse placement. | Must | `contracts/data-contract.md` |

## 6. Failure modes

| ID | Failure | Response |
|---|---|---|
| `MOTOR-FAIL-001` | A non-emerald item is pushed in | Refused at the face; a player's click in the screen is refused by the slot. |
| `MOTOR-FAIL-002` | The network's capacity is zero while running (only this motor, nothing attached) | Load is zero; the meter does not advance; the motor idles free, as a water wheel would. |
| `MOTOR-FAIL-003` | An item without a stats component (creative tab, `/give`) | Shows tier I and "unrolled"; placement rolls it at the middle of tier I so nothing is free or broken. |
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
- `MOTOR-DEC-004` — **The model is Create's creative motor, recoloured per tier** (Kevin,
  2026-09-19): the creative motor's geometry is unused in survival, so a survival motor wearing it
  reads as Create's own; the only difference is the casing colour, which is the tier. The textures
  are copied from Create Fly (CC0; upstream Create MIT) into this mod's assets, recoloured by a
  script under `tools/`, and credited in `NOTICE`. **Cost if wrong:** three texture files and a
  model copy; a later model of our own replaces them without touching code.
