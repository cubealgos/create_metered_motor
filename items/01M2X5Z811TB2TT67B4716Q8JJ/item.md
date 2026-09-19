---
schema_version: 1
id: 01M2X5Z811TB2TT67B4716Q8JJ
key: MM-14
type: bug
title: A newer stats component shows as unknown in the tooltip instead of printing its numbers
created_by: kevin
created_at: 2026-09-19T15:53:44Z
---

## Scope

`MOTOR-REQ-014`'s "shown as unknown" clause is unimplemented (found by the MM-9 sweep): an item whose stats component carries a newer version than the build is kept intact and refuses placement, but `MeteredMotorTooltip` only special-cases a missing component ("Unrolled") and prints a read-only component's numbers as if they were valid. The tooltip shall say "Unknown motor (newer version)" for a read-only component and show no numbers (`MOTOR-REQ-014`, `UI-REQ-006`, `DATA-REQ-003`). Also syncs the spec copy: `UI-REQ-007` now says numbers use `Locale.ROOT` (Kevin, 2026-09-19).

## Approach

`Stats.readOnly()` already exists; branch on it in `MeteredMotorTooltip` (one translation key `tooltip.metered_motor.unknown_version`). The block cannot be placed from such an item, so the screen and goggles paths need nothing; verify that claim against `MeteredMotorBlock.getStateForPlacement` and say so. `just spec-sync` first.

## Acceptance criteria

- [ ] Unit or game test: an item with a stats component of version `current + 1` renders the tooltip line "Unknown motor (newer version)" and no stat lines; a current-version item is unchanged.
- [ ] `SourceSurfaceTest` covers the new key.
- [ ] `docs/spec/` synced from the vault (`UI-REQ-007` reads `Locale.ROOT`).
- [ ] Merged through a Forgejo pull request into `development`.

## Constraints and prior findings

`contracts/data-contract.md` `DATA-REQ-003`; MM-9's requirement table row for `MOTOR-REQ-014`.
