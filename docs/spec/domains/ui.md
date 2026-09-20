---
title: "create_metered_motor spec — UI: the motor screen, the tooltip and the goggles overlay"
type: "spec"
category: "create_metered_motor"
---

# `UI` — reading and feeding the motor

## 1. Purpose

The one screen (five slots and a meter readout), the item tooltip, and the goggles overlay. Not
what the numbers mean (`domains/motor.md`).

## 2. Dimensions

| Dimension | Answer |
|---|---|
| **Actors** | The player sees and clicks; the server opens the menu and owns the slots; Create Fly supplies the frame, textures, slot art and the goggles overlay. |
| **Over time** | The screen is a live container view: slots sync as any chest does; the readout refreshes from the block entity's synced state every second. Nothing is remembered between openings. |
| **Multiplicity** | One screen open at a time (vanilla). Five slots, always. The readout has fixed rows. |
| **Unwanted** | A click placing a non-emerald into a slot; a client asking the server for anything but the container moves vanilla already validates; text that overflows the panel (learned on the compass). |
| **Not-you** | A player without goggles gets everything from the screen; a player with goggles never needs the screen except to take emeralds out; a hopper never needs either. |

## 3. Enumerations

| Interaction | Path | Server checks |
|---|---|---|
| open | right-click the block, not sneaking | the block entity exists at that position |
| insert or take emeralds | vanilla container slot clicks | the slot accepts only emeralds and emerald blocks |
| read | synced fields on the block entity | none; read-only |

## 4. Use cases

### `UI-UC-001` — the motor screen
Actor: player. 1. Right-clicks the motor. 2. The screen shows a header line, `"Tier <numeral> ·
<State>"` (e.g. "Tier III · Stopped"), five slots in a row, the player's inventory below, and a
readout: tier, 64 rpm, stress capacity, rate at full load, current load, emeralds inside,
remaining time at the current load, and the state (running, stopped, paused). No efficiency row
(`decisions/DEC-009-fixed-tiers.md`). 3. The player moves emeralds in or out. 4. Closes.

### `UI-UC-002` — the goggles overlay
Actor: player wearing Create's goggles, looking at the motor. The overlay lists the same readout
under Create's usual kinetic lines (speed, stress), except capacity: Create's own "Generator
Stats" line already shows the block's kinetic capacity at its current speed, so the overlay omits
this mod's own capacity row rather than show the number twice (`DEC-009`).

### `UI-UC-003` — the tooltip
Actor: player hovering the item, in a trade screen or an inventory. The tooltip shows tier, 64
rpm, stress capacity and rate at full load; an item without a stats component defaults to tier I
and shows as a normal tier I motor (`MOTOR-FAIL-003`).

## 5. Requirements

| ID | Requirement | Priority | From |
|---|---|---|---|
| `UI-REQ-001` | The screen shall be drawn with Create Fly's `AbstractSimiContainerScreen` over a `MenuBase` with five real slots and the player inventory, on Create's own frame textures, so it looks like Create's. | Must | `ARCH-DEC-002` of the compass, reused |
| `UI-REQ-002` | The five slots shall accept only emeralds and emerald blocks; shift-click from the player inventory shall respect the same rule. | Must | Unwanted |
| `UI-REQ-003` | The readout shall show tier, 64 rpm, stress capacity, rate at full load, current load as a percentage, emeralds inside (blocks counted as nine), remaining time at the current load, and the state (no efficiency row, `DEC-009`), refreshed at least once a second; the screen's header line reads `"Tier <numeral> · <State>"` (e.g. "Tier III · Stopped"). | Must | `UC-007` |
| `UI-REQ-004` | Every readout line shall be wrapped or truncated to its panel; no string may run past the frame. | Must | the compass, BC-10 |
| `UI-REQ-005` | The goggles overlay shall add the readout's lines after Create's kinetic lines, **except** capacity: **where** Create's own "Generator Stats" line already shows the block's kinetic capacity at its current speed, the overlay shall omit this mod's own capacity row rather than duplicate it. | Should | `UI-UC-002`, `DEC-009` |
| `UI-REQ-006` | The item tooltip shall show the tier's fixed stats; an item without a stats component shall default to tier I and show as a normal tier I motor. | Must | `UI-UC-003` |
| `UI-REQ-007` | Every string shall be a translation key with an `en_us` entry; numbers are formatted with `Locale.ROOT` (a dot decimal in every client language), so the tooltip, the screen, the goggles and the trade files never disagree (Kevin, 2026-09-19, from the MM-9 sweep; Create's own goggles do the same). | Must | Not-you |
| `UI-REQ-008` | The screen shall be the only way to take emeralds out. | Must | `MOTOR-REQ-009` |

## 6. Failure modes

| ID | Failure | Response |
|---|---|---|
| `UI-FAIL-001` | Block broken with the screen open | Vanilla closes the menu; emeralds drop. |
| `UI-FAIL-002` | Readout when the network is gone | Load shows 0 %, remaining time shows "idle". |

## 7. Open questions

| Question | Blocks | Decided by |
|---|---|---|
| Which Create frame: the stock keeper's request window with its five-slot footer strip, or the packager's | the screen ticket | Kevin, at the mock-up, as with the compass |

## 8. Decisions

- `UI-DEC-001` — **Create Fly's own frames and textures, tinted glyphs for actions, every
  string a key** (carried from `create_brass_compass` `UI-DEC-001`). **Cost if wrong:** a resource
  pack changing Create's textures changes us.
- `UI-DEC-002` — **The readout is synced state, not a packet** (this sheet): the block entity's
  load, meter and state are part of its client sync, so goggles and screen read the same fields and
  no new packet exists. **Cost if wrong:** one sync per second per motor with a viewer; negligible.
