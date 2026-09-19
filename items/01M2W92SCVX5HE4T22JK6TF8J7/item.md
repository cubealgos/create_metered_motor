---
schema_version: 1
id: 01M2W92SCVX5HE4T22JK6TF8J7
key: MM-12
type: docs
title: "Modrinth icon: the brass motor model on the cubealgos navy badge"
created_by: kevin
created_at: 2026-09-19T07:28:51Z
---

## Scope

Re-render the Modrinth icon on the cubealgos navy badge with the real block model (Kevin, 2026-09-19): the creative motor's item model recoloured to tier II brass, projected the way the inventory shows block items, at the gui rotation Kevin picks from the rotation sheet, on the navy badge. `tools/icon.py` replaced by a small renderer that parses the model JSON and textures; `docs/modrinth/placeholder-motor.png` deleted; `docs/modrinth/body.md`'s icon line updated.

## Approach

The renderer prototyped in the session scratchpad (`render_motor.py`: element projection with the model's own rotation, painter's order, per-face UV affine sampling, vanilla face shading) moves into `tools/icon.py`, reading MM-7's recoloured tier II textures and model from `src/main/resources/assets/metered_motor/` once MM-7 has merged (blocked by MM-7); palette from the cubealgos heimathafen layer (`standards/marketing/modrinth-collection-icon.md`). Fit box 320 px, LANCZOS for the already-rendered projection.

## Acceptance criteria

- [x] `just icon` regenerates `docs/modrinth/icon.png` (512 × 512, under 256 KiB) from the mod's own tier II model and textures at the chosen rotation.
- [x] `docs/modrinth/placeholder-motor.png` is gone and nothing references it.
- [x] `docs/modrinth/body.md` describes the icon as the brass motor on the navy badge.
- [x] Merged through a Forgejo pull request into `development`.

## Constraints and prior findings

Blocked by MM-7 (the recoloured model and textures). Rotation (Kevin, 2026-09-19, from three rendered sheets): gui rotation `[30, 315, -45]`, i.e. the classic shaft-end three-quarter view banked 45° anticlockwise, scale 0.625. Tier II brass. The projection renderer is kept in the cubealgos heimathafen layer at `standards/marketing/modrinth/block-model-render.py`.
