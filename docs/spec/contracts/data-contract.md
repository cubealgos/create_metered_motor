---
title: "create_metered_motor spec — data contract: the stats component and the block entity's saved state"
type: "spec"
category: "create_metered_motor"
---

# Data contract (`DATA`)

## The component

`metered_motor:stats`, a data component on the item stack and copied into the block entity,
codec-serialised into the world save like any vanilla component.

```
{
  version: 1,
  tier: 1 | 2 | 3,
  rpm: int,
  capacity: int,      // SU
  efficiency: float
}
```

- `version` is the schema version; 1 at 1.0.
- `tier`, `rpm`, `capacity` and `efficiency` are the values `metered_motor:roll` wrote onto the
  offer; they never change after the roll (`03-glossary.md`).

## The block entity's saved state

The five-slot inventory (emeralds and emerald blocks only, `MOTOR-REQ-008`), the meter fraction
(`0 ≤ m ≤ 1`; exactly one means the meter is held because nothing could be taken, `MOTOR-REQ-007`),
`stats` copied in whole from the item at placement (`MOTOR-REQ-003`), and a small prepaid credit
(`0 ≤ prepaid ≤ 8`) carrying the loose emeralds an emerald-block split could not place when every
slot was already full (`MOTOR-REQ-006`; review `2026-09-19`).

## Rules

| ID | Rule |
|---|---|
| `DATA-REQ-001` | The component shall carry `version`; a build shall refuse to edit or place a component with a version newer than its own, keeping it intact and listing it read-only (`MOTOR-REQ-014`). |
| `DATA-REQ-002` | A version bump shall ship a migration that reads every older version forward; migrations never run backward. |
| `DATA-REQ-003` | A malformed or missing component shall be treated as "unrolled" for display (`MOTOR-FAIL-003`); placement shall roll it at the middle of tier I's bands so nothing is free or broken. |
| `DATA-REQ-004` | The block entity's copy of `stats` and the item's copy shall be moved only by `applyImplicitComponents` (item → block, on placement) and `collectImplicitComponents` (block → item, on breaking), and shall never diverge between the two while the block stands. |

## Out of scope (sheet §8)

No import from other power mods' stored stats. No conversion of Create Fly's creative motor into
a metered motor or back.
