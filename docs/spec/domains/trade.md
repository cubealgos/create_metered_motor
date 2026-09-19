---
title: "create_metered_motor spec — TRADE: the toolsmith's offer and the roll"
type: "spec"
category: "create_metered_motor"
---

# `TRADE` — buying a motor

## 1. Purpose

How a motor enters the world: the toolsmith's trades at three levels, the tiers, the roll that
gives each offer its stats, and the data files a pack can retune. Not the motor's behaviour
(`domains/motor.md`).

## 2. Dimensions

| Dimension | Answer |
|---|---|
| **Actors** | The toolsmith (`ACTORS-003`) offers; the server (`ACTORS-004`) rolls when the offer is created; the player (`ACTORS-001`) buys, and breeds or levels villagers for a better roll; a datapack author (`ACTORS-006`) changes prices, levels and bands. |
| **Over time** | An offer is created when a villager reaches a level (two trades drawn from that level's tag) and rolled then; it restocks with the same stats; a new villager or the next level is a new draw and a new roll. Bought motors keep their roll forever. |
| **Multiplicity** | Three trade files, one per tier, each in one level's tag. A level draws two of its trades, so a given toolsmith at a given level offers the motor with the tag's odds (vanilla's level 4 tag lists four trades plus the common smith group). One offer per villager per level at most. |
| **Unwanted** | A roll outside its band; a price of zero; the trade appearing at a level it was not meant for; a datapack disabling the roll and selling a fixed motor (allowed, it is data); a client claiming a roll (impossible, the server rolls). |
| **Not-you** | A player who never reads the tooltip still gets a working motor. A pack author who copies the trade file to another profession's tag gets a motor from a weaponsmith, which is fine. A translator sees the tooltip keys. |

## 3. Enumerations

### Tiers

| Tier | Toolsmith level | Price (emeralds) | rpm band | Stress capacity band (SU) | Efficiency band | Replaces |
|---|---|---|---|---|---|---|
| I | Journeyman (3) | 24 | 16 to 64 | 512 to 2,048 | 0.75 to 1.25 | water wheels, windmills, a small steam engine at the top |
| II | Expert (4) | 40 | 32 to 128 | 2,048 to 8,192 | 0.75 to 1.25 | a mid steam engine |
| III | Master (5) | 64 | 64 to 256 | 8,192 to 18,432 | 0.75 to 1.25 | a full-sized steam engine at the luckiest draw (Create's largest boiler, 18 levels at 1,024 SU each, to confirm at the first ticket) |

Each of rpm, capacity and efficiency is a uniform roll within its band, independent of the others.
Rate at full load follows from capacity and efficiency (`MOTOR-REQ-005`): tier I, one emerald
every 3 to 20 minutes; tier II, one every 45 seconds to 5 minutes; tier III, 0.8 to 3 a minute,
at most 60 a Minecraft day.

### Cardinality

| Relation | Count | At zero | At the top |
|---|---|---|---|
| tier → trade file | exactly 1 | tier absent from the game | N/A |
| level tag → motor trades | 0..1 | no motor at that level | N/A |
| offer → roll | exactly 1, at creation | unrolled item (`MOTOR-FAIL-003`) | N/A |
| trade → uses before restock | 2 | N/A | the same roll twice |

## 4. Use cases

`UC-001` in `02-journeys.md`.

## 5. Requirements

| ID | Requirement | Priority | From |
|---|---|---|---|
| `TRADE-REQ-001` | The mod shall ship three villager trade files under `data/metered_motor/villager_trade/toolsmith/<level>/emerald_metered_motor_<tier>.json`, each wanting the tier's emerald price and giving one motor, `max_uses` 2, `xp` as vanilla's tools at that level, `reputation_discount` 0.2. | Must | Tiers |
| `TRADE-REQ-002` | Each trade file shall list `metered_motor:roll` among its given-item modifiers with the tier and the three bands as parameters; the function shall write the stats component with uniform rolls from those bands using the loot context's random source. | Must | `ARCH-DEC-004` |
| `TRADE-REQ-003` | The mod shall append each trade to the matching vanilla tag `data/minecraft/tags/villager_trade/toolsmith/level_<n>.json` with `replace: false`. | Must | Verifications 5 |
| `TRADE-REQ-004` | **Where** a datapack replaces a trade file or a tag, the system shall use the datapack's; the roll function shall reject a band whose minimum exceeds its maximum or leaves the tier's documented range by more than a factor of four, logging once and using the tier's default band. | Should | `ACTORS-006` |
| `TRADE-REQ-005` | The offered item's tooltip shall show its rolled stats in the trade screen before purchase. | Must | `UC-001` |
| `TRADE-REQ-006` | A toolsmith shall never hold more than one metered-motor offer: each trade file carries a merchant predicate (`metered_motor:no_motor_offered`, a loot condition on the merchant) that fails when the villager's offers already contain a metered motor, so a later level's draw skips the trade. | Must | Kevin, 2026-09-19 |

## 6. Failure modes

| ID | Failure | Response |
|---|---|---|
| `TRADE-FAIL-001` | The level's draw skipped the motor | By design: breed or level another toolsmith. |
| `TRADE-FAIL-002` | Trade rebalance datapack (vanilla's experiment) is on | Its toolsmith tags replace vanilla's; the mod's tag entries still append to them (the tag path is the same). Verified at the first ticket. |
| `TRADE-FAIL-003` | A pack sets nonsense bands | `TRADE-REQ-004`. |

## 7. Open questions

| Question | Blocks | Decided by |
|---|---|---|
| Whether 26.2's `merchant_predicate` sees the villager entity and its current offers when a level's trades are drawn, which `TRADE-REQ-006` needs; if not, a villager-level hook replaces the condition | `TRADE-REQ-006` | the first trade ticket |
| Create Fly's largest boiler figure, taken here as 18 levels at 1,024 SU (18,432), to confirm tier III's ceiling | tier III's band | the first ticket reads `CStress` and `BoilerData` |

## 8. Decisions

- `TRADE-DEC-001` — **No recipe; the toolsmith sells it** (Kevin, 2026-09-19): mid-game
  availability, and the roll makes breeding villagers worthwhile. **Cost if wrong:** a server
  without villagers has no motors; a datapack can add a recipe.
- `TRADE-DEC-002` — **Three tiers at Journeyman, Expert and Master; the luckiest tier III roll
  replaces a full-sized steam engine; its burn stays under a stack a day** (Kevin, 2026-09-19).
  The bands above are the sheet's proposal against Create's generators; the ceiling is confirmed
  at the first ticket. **Cost if wrong:** numbers in data files and one divisor in code.
- `TRADE-DEC-004` — **One motor offer per toolsmith, ever** (Kevin, 2026-09-19): a villager that
  already offers a motor never draws a second at a later level; a better roll means another
  villager. **Cost if wrong:** the merchant predicate must see the villager's offers; verified at
  the first trade ticket, with a villager-level hook as the fallback.
- `TRADE-DEC-003` — **Stats are rolled when the offer is created, not when bought** (from the
  26.2 trade system, verifications 5 and 6): the item template is fixed, the modifiers run on
  offer creation, so the tooltip in the trade screen shows the real roll. **Cost if wrong:** two
  purchases of one offer are twins.
