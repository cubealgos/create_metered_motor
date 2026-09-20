---
title: "create_metered_motor spec — TRADE: the toolsmith's offer and the roll"
type: "spec"
category: "create_metered_motor"
---

# `TRADE` — buying a motor

## 1. Purpose

How a motor enters the world: the toolsmith's trades at three levels, the tiers, the fixed stats
each tier carries, and the data files a pack can retune. Not the motor's behaviour
(`domains/motor.md`).

## 2. Dimensions

| Dimension | Answer |
|---|---|
| **Actors** | The toolsmith (`ACTORS-003`) offers; the server (`ACTORS-004`) creates the offer from the trade file's fixed `gives` template (nothing rolled, `decisions/DEC-009-fixed-tiers.md`); the player (`ACTORS-001`) buys, and levels villagers to reach a higher tier; a datapack author (`ACTORS-006`) changes prices, levels and which trade sits at which level. |
| **Over time** | An offer is created when a villager reaches a level (two trades drawn from that level's tag); it restocks with the same fixed stats; a new villager or the next level is a new draw of the same fixed tiers. Bought motors keep their tier forever, and every motor of a tier is identical. |
| **Multiplicity** | Three trade files, one per tier, each in one level's tag. A level draws two of its trades, so a given toolsmith at a given level offers the motor with the tag's odds (vanilla's level 4 tag lists four trades plus the common smith group). One offer per villager per level at most. |
| **Unwanted** | A price of zero; the trade appearing at a level it was not meant for; a datapack changing a tier's capacity or rpm through the trade file (not possible: those are fixed in code, `DEC-009`); a client claiming a tier the offer's template does not carry (impossible, the server creates the offer from the fixed template). |
| **Not-you** | A player who never reads the tooltip still gets a working motor. A pack author who copies the trade file to another profession's tag gets a motor from a weaponsmith, which is fine. A translator sees the tooltip keys. |

## 3. Enumerations

### Tiers

Fixed, not rolled (`DEC-009`): each tier is pinned to a real steam-engine set-up, all three at the
engine's own active speed, 64 rpm.

| Tier | Toolsmith level | Price (emeralds) | rpm | Stress capacity (SU) | Rate at full load | Real set-up |
|---|---|---|---|---|---|---|
| I | Journeyman (3) | 24 | 64 | 16,384 | 0.18/min (11/day) | 1 engine, level-1 boiler |
| II | Expert (4) | 40 | 64 | 65,536 | 0.71/min (43/day) | 4 engines, level-4 boiler |
| III | Master (5) | 64 | 64 | 294,912 | 3.2/min (64/day) | 18 engines, level-18 boiler (full) |

Rate follows directly from capacity, `capacity / 92,160` (`MOTOR-REQ-005`); cost per SU is
identical across tiers, so a tier only changes the up-front price and the toolsmith level needed,
not the running cost per unit of power. Tier III at full load lands exactly on the one-stack-a-day
ceiling.

### Cardinality

| Relation | Count | At zero | At the top |
|---|---|---|---|
| tier → trade file | exactly 1 | tier absent from the game | N/A |
| level tag → motor trades | 0..1 | no motor at that level | N/A |
| offer → stats component | exactly 1, fixed on the `gives` template | an item without the component defaults to tier I (`MOTOR-FAIL-003`) | N/A |
| trade → uses before restock | 2 | N/A | the same tier twice |

## 4. Use cases

`UC-001` in `02-journeys.md`.

## 5. Requirements

| ID | Requirement | Priority | From |
|---|---|---|---|
| `TRADE-REQ-001` | The mod shall ship three villager trade files under `data/metered_motor/villager_trade/toolsmith/<level>/emerald_metered_motor_<tier>.json`, each wanting the tier's emerald price and giving one motor, `max_uses` 2, `xp` as vanilla's tools at that level, `reputation_discount` 0.2. | Must | Tiers |
| `TRADE-REQ-002` | ~~Each trade file shall list `metered_motor:roll` among its given-item modifiers with the tier and the three bands as parameters; the function shall write the stats component with uniform rolls from those bands using the loot context's random source.~~ **Withdrawn** (`DEC-009`, 2026-09-20): there is no roll function. Each trade file's `gives` item template shall instead carry the tier's stats component directly, fixed at authoring time, e.g. `"gives": {"item": "metered_motor:metered_motor", "components": {"metered_motor:stats": {"version": 2, "tier": "ii"}}}`. **To verify at the ticket:** whether 26.2's villager-trade `gives` item template accepts a `components` block the way it accepts vanilla item-stack components generally; the verifications table (`README.md` §Verifications 5) confirms the template's other fields (`wants`, `additional_wants`, `given_item_modifiers`, `max_uses`, `reputation_discount`, `xp`) but not literal component embedding on `gives`. | Withdrawn | `DEC-009` |
| `TRADE-REQ-003` | The mod shall append each trade to the matching vanilla tag `data/minecraft/tags/villager_trade/toolsmith/level_<n>.json` with `replace: false`. | Must | Verifications 5 |
| `TRADE-REQ-004` | **Where** a datapack replaces a trade file or a tag, the system shall use the datapack's. Retuning through the trade file changes price, level and which tag the trade joins only; a tier's rpm, capacity and rate are fixed in code (`DEC-009`) and are not parameters a trade file carries. | Should | `ACTORS-006` |
| `TRADE-REQ-005` | The offered item's tooltip shall show its tier's fixed stats in the trade screen before purchase. | Must | `UC-001` |
| `TRADE-REQ-006` | A toolsmith shall never hold more than one metered-motor offer: each trade file carries a merchant predicate (`metered_motor:no_motor_offered`, a loot condition on the merchant) that fails when the villager's offers already contain a metered motor, so a later level's draw skips the trade. | Must | Kevin, 2026-09-19 |

## 6. Failure modes

| ID | Failure | Response |
|---|---|---|
| `TRADE-FAIL-001` | The level's draw skipped the motor | By design: level another toolsmith, or wait for a restock draw. |
| `TRADE-FAIL-002` | Trade rebalance datapack (vanilla's experiment) is on | Its toolsmith tags replace vanilla's; the mod's tag entries still append to them (the tag path is the same). Verified at the first ticket. |
| `TRADE-FAIL-003` | A pack sets a nonsense price (e.g. zero) | `TRADE-REQ-004`; only price, level and tag membership are datapack parameters now (`DEC-009`). |

## 7. Open questions

| Question | Blocks | Decided by |
|---|---|---|
| Whether 26.2's `merchant_predicate` sees the villager entity and its current offers when a level's trades are drawn, which `TRADE-REQ-006` needs; if not, a villager-level hook replaces the condition | `TRADE-REQ-006` | the first trade ticket |
| Whether 26.2's villager-trade `gives` item template accepts a `components` block for the fixed tier stats | `TRADE-REQ-002` | the first trade ticket (marked "to verify at the ticket" there) |

Closed: Create Fly's largest boiler figure, previously taken as 18 levels at 1,024 SU (18,432) and
left to confirm at the first ticket — resolved by bytecode read at 294,912 SU for a full level-18,
18-engine boiler (`vault/technical/minecraft/create-fly-steam-engines-26-2.md`), fixed as tier
III's capacity (`DEC-009`).

## 8. Decisions

- `TRADE-DEC-001` — **No recipe; the toolsmith sells it** (Kevin, 2026-09-19): mid-game
  availability, and needing to level a toolsmith to Expert or Master to unlock the higher tiers
  makes levelling villagers worthwhile. **Cost if wrong:** a server without villagers has no
  motors; a datapack can add a recipe.
- `TRADE-DEC-002` — **Three tiers at Journeyman, Expert and Master; tier III's burn stays at a
  stack a day** (Kevin, 2026-09-19). **Amended (Kevin, 2026-09-20, `DEC-009`):** the bands this
  entry originally described (a rolled range per tier, with tier III's ceiling to confirm) are
  withdrawn. Each tier is now a single fixed point, pinned to a real steam-engine set-up read from
  the 26.2 jar — see the tiers table above and `decisions/DEC-009-fixed-tiers.md`. **Cost if
  wrong:** numbers in code and one price per tier in data files.
- `TRADE-DEC-004` — **One motor offer per toolsmith, ever** (Kevin, 2026-09-19): a villager that
  already offers a motor never draws a second at a later level; a higher tier means levelling
  another villager to Expert or Master. **Cost if wrong:** the merchant predicate must see the
  villager's offers; verified at the first trade ticket, with a villager-level hook as the
  fallback.
- `TRADE-DEC-003` — **Nothing is rolled** (Kevin, 2026-09-20, `DEC-009`, amending the original
  "stats are rolled when the offer is created, not when bought"): the trade's `gives` item
  template carries the tier's stats component directly, fixed at authoring time; the tooltip in
  the trade screen always shows the tier's fixed numbers, and two offers of the same tier are
  always twins, by design. **Cost if wrong:** none left — there is no random source in the trade
  path any more to get wrong.
