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

## Findings

**The `TooltipBehaviour` attachment mechanism** (Verification 10, now fully read from the jar):
`com.zurrtum.create.api.behaviour.BlockEntityBehaviour` — the `api` package, the add-on-facing
surface — exposes three static registries an add-on populates directly: `add`/`addFirstRead`
(server, `REGISTRY`/`FIRST_READ_REGISTRY`) and `addClient(BlockEntityType<T>, Function<T,
BlockEntityBehaviour<?>>)` (client, `CLIENT_REGISTRY`). Create Fly's own internal
`com.zurrtum.create.client.AllBlockEntityBehaviours.add(...)` populates the same
`CLIENT_REGISTRY` for Create's own blocks; `addClient` is the same call open to add-ons. The
goggle overlay (`GoggleOverlayRenderer`) reads the hovered block entity's behaviour through
`BlockEntityBehaviour.get(level, pos, TooltipBehaviour.TYPE)` and, when it implements
`IHaveGoggleInformation`, calls `addToGoggleTooltip(List<Component>, boolean)`. For any
`GeneratingKineticBlockEntity` (our own block entity's supertype), the reusable base is
`client.foundation.blockEntity.behaviour.tooltip.GeneratingKineticTooltipBehaviour<T extends
KineticBlockEntity>`: its `addToGoggleTooltip` already draws Create's "generator stats"/"capacity
provided" lines, so `metered_motor.client.visual.MotorTooltipBehaviour` extends it, calls `super`
first, then appends the motor's own lines. Registered as
`BlockEntityBehaviour.addClient(MotorBlocks.BLOCK_ENTITY_TYPE, MotorTooltipBehaviour::new)` from
`MotorVisuals.register()`.

**Visual registration**: `com.zurrtum.create.client.AllBlockEntityRenders.register()` (read via
`javap`) registers the creative motor (`AllBlockEntityTypes.MOTOR`) with a plain
`BlockEntityRendererProvider` (`CreativeMotorRenderer::new`, the Flywheel-less fallback) *and* a
Flywheel visual factory built from `OrientedRotatingVisual.of(AllPartialModels.SHAFT_HALF)` — not
`SingleAxisRotatingVisual` as the ticket's Approach guessed; `OrientedRotatingVisual` is the one
that orients a partial from a fixed model-space direction (`south`) to the block's own `facing`
value, matching a `DirectionalKineticBlock`'s single `FACING` property. `SimpleBlockEntityVisualizer
.Builder.apply()` itself calls `VisualizerRegistry.setVisualizer`, so no separate registry call is
needed. Mirrored exactly: `metered_motor.client.visual.MotorVisuals.register()` calls
`net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(type,
MeteredMotorRenderer::new)` (vanilla's own registration, the same one Create Fly calls — not a
Fabric API wrapper) then `SimpleBlockEntityVisualizer.builder(type).factory(OrientedRotatingVisual
.of(AllPartialModels.SHAFT_HALF)).skipVanillaRender(be -> true).apply()`.

**What the model copy contains and how the shaft is drawn**: the creative motor's `block.json`
(12 elements) and `block_vertical.json` (11 elements) contain the casing geometry and four
decorative corner struts textured `create:block/axis` (a generic metal texture reuse, not a
rotating shaft) — no shaft cylinder is baked into either. The connecting half-shaft is drawn
entirely by the renderer/visual (`CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF,
state, facing)` in the BER fallback; `OrientedRotatingVisual` in the Flywheel path), so copying
the block models unchanged (only retargeting the `5`/`6`/`particle` texture keys to this mod's
namespace, tier by tier) already matches "no shaft in the block model" with no stripping needed.
`item.json` (13 elements) is different: it bakes its own static "Axis" shaft element for the
inventory icon, since item rendering has no live rotation or visual; only tier I's textures are
used for it, since the inventory shows one model regardless of roll.

**Recolour targets** (`tools/recolour.py`): sampled both 16x16 source textures — the magenta
band sits at HLS hue 0.675–0.854; the darker "shadow" pixels (hue ~0.667–0.70, some near-black
with spuriously high saturation) fall *outside* the specified 0.78–0.92 band and are correctly
left untouched, while the actual magenta highlights (hue 0.786–0.854, saturation 0.2–0.56) are
all inside it — confirming the band boundaries given in the ticket were well chosen against the
real texture data, no tuning needed. Recolour keeps each pixel's own lightness and takes hue and
saturation from the tier's target RGB (andesite `(132,136,130)`, brass `(196,148,72)`, gold
`(232,196,80)`) converted to HLS.

**Compile-time gotcha**: `net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState
.blockState`/`.lightCoords` are accessed directly by Create Fly's own `CreativeMotorRenderer`
bytecode, but `blockState` compiles as private from this mod's own source (this project depends
on Create Fly via plain `implementation`, not `modImplementation`, so Create Fly's own access
widener — which must be widening this field for its own build — never reaches our compile
classpath). Worked around by reading `blockEntity.getBlockState()` instead of `state.blockState`
in `MeteredMotorRenderer`; `lightCoords` compiled fine as used. Confirmed by an early
`./gradlew compileJava` checkpoint before building out the rest of the assets.

**Check results**: `just check` green — `./gradlew check -x test -x runGameTest` (lint) succeeds;
`just map-check` clean; `./gradlew test` and `tools/test_*.py` (4 tests) pass, including the new
`ModelAssetsTest`; `./gradlew runGameTest` reports "All 28 required tests passed :)" (25 prior +
3 new `TierStateGameTest` cases). `just map` regenerated `docs/map.md` and
`docs/map/root/metered_motor.client.visual.md`.

**Not done**: `tools/icon.py`'s comment forward-references MM-7 changing its default sprite to
the real item texture; not done here. The motor has no flat item texture — its inventory icon is
a full 3D model (casing + motor textures composited by `models/item/metered_motor.json`), not a
single sprite `icon.py` can scale and outline the way it does for a flat item like the compass;
pointing it at either 16x16 texture alone would misrepresent the icon. Left as a follow-up rather
than guessed at. Rendering, goggles and the tinted casing itself are Kevin's `just client` check
per the ticket (not run here).
