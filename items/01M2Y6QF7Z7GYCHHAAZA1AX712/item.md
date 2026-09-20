---
schema_version: 1
id: 01M2Y6QF7Z7GYCHHAAZA1AX712
key: MM-16
type: bug
title: The placed motor's shaft half renders crooked instead of along the facing axis
created_by: kevin
created_at: 2026-09-20T01:26:12Z
---

## Scope

The placed motor's shaft half renders crooked (Kevin, 2026-09-20, client check, screenshot in the session scratchpad `mm16-crooked-shaft.png`): a brass motor facing the player shows the grey shaft half tilted at an angle inside the front face, not along the facing axis as Create's creative motor draws it. The shaft shall sit on the facing axis, centred, and turn about that axis, exactly as the creative motor beside it (`MOTOR-REQ-002`, `MOTOR-REQ-013`).

## Approach

Compare byte for byte with Create Fly: `javap -p -c` on `CreativeMotorRenderer` (the fallback renderer: `KineticBlockEntityRenderer.renderSafe` and how it picks the rendered block state / rotation for the shaft: `getRenderedBlockState`, `shaft(...)`, `rotateToFace`, `KineticBlockEntityRenderer.standardKineticRotationTransform`) and the visual `AllBlockEntityRenders`' `OrientedRotatingVisual.of(AllPartialModels.SHAFT_HALF)` (what `Direction`/rotation it derives from `facing`, whether it uses `getRotationAxis` from the block and a `rotateToFace(Direction)` on the partial). Read MM-7's `MeteredMotorRenderer` and `MotorVisuals` against those. Suspects: the fallback renderer rotating the shaft with the block's facing as a rotation axis instead of orienting the partial to the face; the visual passing `Direction.UP`-relative rotation where the creative motor passes the facing; both paths wrong for the horizontal facings only. Decide which path the client was on (Flywheel is bundled with Create Fly, so the visual path is the live one unless Flywheel is off) and fix both.

## Acceptance criteria

- [ ] `MeteredMotorRenderer` and `MotorVisuals` orient the shaft half the way Create's creative motor does for all six facings; the difference to Create's code is only the block entity type.
- [ ] A unit test on the orientation helper (the pure part: facing → rotation) if one can be separated; otherwise the ticket records why not.
- [ ] `just client`: the shaft sits on the facing axis and turns while running, for a horizontal and a vertical facing (Kevin's check).
- [ ] Merged through a Forgejo pull request into `development`.

## Constraints and prior findings

MM-7's Findings on `OrientedRotatingVisual` and `AllBlockEntityRenders.register()`; the block model itself contains no shaft geometry.
