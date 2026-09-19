---
schema_version: 1
id: 01M2W33RXHT0SEGW4DPHD861RT
key: MM-7
type: feat
title: Goggles overlay, tooltip, shaft visual and tinted block model
created_by: kevin
created_at: 2026-09-19T05:44:32Z
---

## Scope

Goggles overlay through Create Fly's client tooltip behaviour: the readout added after Create's own kinetic lines (speed, stress) when a player wearing Create's goggles looks at the motor (`MOTOR-REQ-012`, `UI-REQ-005`, `UI-UC-002`). The item tooltip showing tier, rpm, stress capacity, efficiency and rate at full load, or "unrolled" for an item with no roll (`UI-REQ-006`, `UI-UC-003`). The shaft visual with `SingleAxisRotatingVisual` and a fallback renderer for when Flywheel is unavailable, and the block model: Create's creative motor geometry and textures copied into this mod's assets, the creative magenta recoloured per tier — andesite grey, brass, gold — by a `tools/` script, credited in `NOTICE` (`MOTOR-REQ-013`, `MOTOR-DEC-004`, Kevin 2026-09-19: the creative motor's model is unused in survival). `just spec-sync` first: the vault spec carries the decision.

## Approach

First step: read how an add-on attaches a client-side `TooltipBehaviour` to a block entity type it did not itself define as one of Create's own kinetic types — Create's own kinetic block entities implement no client interface for this, so the attachment point is read from Create Fly's client registration before writing the overlay (spec `README.md` Verification 10, only partly verified). `SingleAxisRotatingVisual.of(PartialModel)` and `SimpleBlockEntityVisualizer.builder(type)` are the visual entry points, following `CreativeMotorRenderer`'s shape (Verification 8); the fallback is a plain `BlockEntityRenderer` for a Flywheel-less client. The model: copy `assets/create/models/block/creative_motor/{block,block_vertical,item}.json` and `textures/block/{creative_motor,creative_casing}.png` from the Create Fly jar into this mod's namespace, three texture sets produced by `tools/recolour.py` (hue replacement of the magenta band, luminance kept, dark inner parts untouched); the block state selects the tier's model set from the block entity's tier through a block state property `tier` (I, II, III) set on placement from the component, so vanilla model selection does the switching and no custom renderer is needed for the casing.

## Acceptance criteria

- [ ] The client-side attachment mechanism for a `TooltipBehaviour` on this mod's block entity type is recorded in this ticket's Constraints section before the overlay is written.
- [ ] `just client`: goggles show the readout after Create's kinetic lines; the item tooltip shows rolled stats or "unrolled"; the shaft visibly turns; the casing is Create's creative motor recoloured andesite/brass/gold across the three tiers (Kevin's check — goggles, tooltips and rendering are not game-testable headless, per `operations/testing.md`).
- [ ] Every added string is a translation key with an `en_us` entry (`SourceSurfaceTest`, extended).

## Constraints and prior findings

`MOTOR-REQ-012`, `MOTOR-REQ-013`, `UI-REQ-005`, `UI-REQ-006`, `UI-UC-002`, `UI-UC-003`. Verified, partly: client visuals use `SingleAxisRotatingVisual.of(PartialModel)` and `SimpleBlockEntityVisualizer.builder(type)` from Create Fly's bundled Flywheel; the creative motor uses `CreativeMotorRenderer`; goggles read `client.api.goggles.IHaveGoggleInformation.addToGoggleTooltip` — but how an add-on attaches a `TooltipBehaviour` to its own block entity type is explicitly marked "to be read at the first ticket" (spec `README.md` Verification 10). Blocked by MM-4 (a running block entity with a real inventory to show in the overlay).
