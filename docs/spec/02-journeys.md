---
title: "create_metered_motor spec — journeys: the use cases end to end"
type: "spec"
category: "create_metered_motor"
---

# 02 — Journeys

Every step names who acts. `UC` ids are flat across the project; domain files reference them.

### `UC-001` — Buy a motor

Actor: player (`ACTORS-001`) · Goal: obtain a metered motor

| Step | Actor | Action |
|---|---|---|
| 1 | player | Levels a toolsmith to Journeyman, Expert or Master, or finds one. |
| 2 | server | When the villager's trades for that level are drawn, the metered-motor trade may be among the two picked; its offer item gets rolled stats then (`TRADE-REQ-002`). |
| 3 | player | Reads the offer's tooltip: tier, rpm, stress capacity, emeralds per minute at full load. |
| 4 | player | Pays the emeralds; receives the motor item with those stats. |

Fails when: the level's draw did not pick the trade → the player breeds or levels another toolsmith
(`TRADE-FAIL-001`, by design).

### `UC-002` — Place and power a network

Actor: player · Goal: turn shafts

| Step | Actor | Action |
|---|---|---|
| 1 | player | Places the motor with its shaft facing the machine, as with Create's creative motor. |
| 2 | server | Creates the block entity, copies the stats from the item (`MOTOR-REQ-003`); the motor is stopped: no emeralds. |
| 3 | player | Right-clicks the motor; the screen opens with five empty slots (`UI-UC-001`); drops emeralds in. |
| 4 | server | The motor starts: generated speed is its rpm, capacity is its stress capacity; Create's network picks it up (`MOTOR-REQ-004`). |
| 5 | player | Sees the shaft turn, the stress gauge show the capacity, the goggles show the meter. |

### `UC-003` — The meter runs

Actor: server (`ACTORS-004`) · Goal: burn emeralds in proportion to load

| Step | Actor | Action |
|---|---|---|
| 1 | server | Every second, reads the network's stress and capacity; adds `rate × stress / capacity / 60` to the motor's meter (`MOTOR-REQ-006`). |
| 2 | server | When the meter reaches one, takes one emerald from the inventory (an emerald block counts as nine, split on the spot) and subtracts one. |
| 3 | server | When the inventory is empty and the meter reaches one, stops the motor: speed 0, capacity 0, network updated (`MOTOR-REQ-007`). |
| 4 | player | Refills; the motor resumes on the next tick. |

### `UC-004` — Feed it by machine

Actor: player · Goal: keep the motor running unattended

| Step | Actor | Action |
|---|---|---|
| 1 | player | Places a hopper on top, or points a funnel, chute or mechanical arm at it. |
| 2 | game or Create | Inserts emeralds and emerald blocks through any face; anything else is refused (`MOTOR-REQ-008`). |
| 3 | player | Tries to pull emeralds out with a hopper below or a funnel: nothing comes out (`MOTOR-REQ-009`). |

### `UC-005` — Pause it

Actor: player · Goal: stop paying while a machine idles

| Step | Actor | Action |
|---|---|---|
| 1 | player | Powers the motor with redstone. |
| 2 | server | The motor stops as if empty; no emeralds burn; the signal off, it resumes (`MOTOR-REQ-010`). |

### `UC-006` — Move it

Actor: player · Goal: take the motor elsewhere without losing the roll

| Step | Actor | Action |
|---|---|---|
| 1 | player | Breaks the motor with any tool. |
| 2 | server | Drops one motor item carrying the block's stats and drops the emeralds inside as items (`MOTOR-REQ-011`). |
| 3 | player | Places it again; the stats are the same. |

### `UC-007` — Read it

Actor: player · Goal: know what the motor is doing

| Step | Actor | Action |
|---|---|---|
| 1 | player | Looks at the motor wearing Create's goggles, or opens its screen. |
| 2 | client | Shows tier, rpm, stress capacity, efficiency, burn at full load, current load, emeralds left and the time they last at the current load (`MOTOR-REQ-012`, `UI-REQ-003`). |
