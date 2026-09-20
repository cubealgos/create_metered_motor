---
title: "create_metered_motor spec — data contract: the stats component and the block entity's saved state"
type: "spec"
category: "create_metered_motor"
---

# Data contract (`DATA`)

## The component

`metered_motor:stats`, a data component on the item stack and copied into the block entity,
codec-serialised into the world save like any vanilla component.

**Version 2** (current, `decisions/DEC-009-fixed-tiers.md`):

```
{
  version: 2,
  tier: 1 | 2 | 3
}
```

- `version` is the schema version.
- `tier` is the only stored field; rpm (64, fixed), stress capacity and rate are derived from the
  tier in code (`domains/motor.md` `MOTOR-REQ-004`, `MOTOR-REQ-005`), never stored, and never
  rolled — every motor of a tier is identical.

**Version 1** (superseded 2026-09-20, kept for the migration below):

```
{
  version: 1,
  tier: 1 | 2 | 3,
  rpm: int,
  capacity: int,      // SU
  efficiency: float
}
```

`tier`, `rpm`, `capacity` and `efficiency` were the values `metered_motor:roll` wrote onto the
offer (withdrawn, `domains/trade.md` `TRADE-REQ-002`); they never changed after the roll.

**Migration, version 1 → version 2** (`DATA-REQ-002`): read `tier`; drop `rpm`, `capacity` and
`efficiency` — a version-1 item's or block entity's rolled rpm, capacity and efficiency are
discarded, and the item or block behaves from then on exactly as any other motor of that tier
(fixed 64 rpm, the tier's fixed capacity and rate). The block entity's inventory, meter and
prepaid credit are untouched by the migration; only the `stats` copy changes.

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
| `DATA-REQ-002` | A version bump shall ship a migration that reads every older version forward; migrations never run backward. Version 1 → 2 keeps `tier` and drops `rpm`, `capacity` and `efficiency` (above). |
| `DATA-REQ-003` | A malformed or missing component shall default to tier I (`MOTOR-FAIL-003`); placement shall set the block entity to tier I so nothing is free or broken. |
| `DATA-REQ-004` | The block entity's copy of `stats` and the item's copy shall be moved only by `applyImplicitComponents` (item → block, on placement) and `collectImplicitComponents` (block → item, on breaking), and shall never diverge between the two while the block stands. |

## Out of scope (sheet §8)

No import from other power mods' stored stats. No conversion of Create Fly's creative motor into
a metered motor or back.
