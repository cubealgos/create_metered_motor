---
schema_version: 1
id: 01M2Y6KFA9S4K7XPN25FRY183J
key: MM-15
type: bug
title: The item shows tier I's model for every tier; the item model must follow the rolled tier
created_by: kevin
created_at: 2026-09-20T01:24:01Z
---

## Scope

The item shows tier I's andesite model for every tier (Kevin, 2026-09-20, client check): MM-7 gave the block item one item model. The item in the inventory, in hand and in the trade screen shall show the casing colour of its rolled tier (andesite, brass, gold), from the stats component, so the offered motor in a trade screen already looks like its tier (`MOTOR-REQ-013`, `MOTOR-DEC-004`, `TRADE-REQ-005`). An unrolled item (no component) shows tier I.

## Approach

26.2 item models are data-driven (`assets/metered_motor/items/metered_motor.json`): verify in the merged jar whether `minecraft:select` accepts a mod-registered select property (`net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty`, its `Type` and the `SelectItemModelProperties` id mapper) so a `metered_motor:tier` property reads the stats component and selects among three `create:model/normal` item models (`item_i`, `item_ii`, `item_iii`, copies of the current item model with the tier's textures). If a custom select property cannot be registered from a mod without a mixin, fall back to `minecraft:custom_model_data` strings written next to the stats component by the roll and by the debug command, and say so. `tools/recolour.py` already writes the three texture sets.

## Acceptance criteria

- [x] Unit test `ModelAssetsTest` extended: the item model definition references three item models and each resolves to existing textures.
- [x] Game test or unit test: the select property yields `i`, `ii`, `iii` for stacks with the matching stats component and `i` for none.
- [ ] `just client`: the three debug items and a toolsmith's offer show their tier colour in the inventory, in hand and in the trade screen (Kevin's check).
- [x] Merged through a Forgejo pull request into `development`.

## Constraints and prior findings

MM-7's Findings: the creative motor's `item.json` bakes its own static shaft; `create:model/normal` is Create Fly's item model type. MM-12's `tools/icon.py` reads the tier I item model and substitutes tier II textures; keep it working (it may read `item_ii` directly afterwards).

## Findings

**The registration path, verified in the merged jar and the Create Fly jar**:
`net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty<T>` is a plain
interface (`get(ItemStack, ClientLevel, LivingEntity, int, ItemDisplayContext)`, `valueCodec()`,
`type()`); its `Type<P, T>` record is built with the static factory
`SelectItemModelProperty.Type.create(MapCodec<P> propertyCodec, Codec<T> valueCodec)` — copied
exactly from vanilla's own `DisplayContext` (a stateless property, `javap -c`'d to confirm its
static initialiser calls `SelectItemModelProperty.Type.create(MapCodec.unit(new
DisplayContext()), VALUE_CODEC)`). Where a mod-registered `Type` is looked up is
`SelectItemModelProperties`: a **private static final** `ExtraCodecs.LateBoundIdMapper<Identifier,
SelectItemModelProperty.Type<?, ?>> ID_MAPPER`, populated only by vanilla's own
`SelectItemModelProperties.bootstrap()`. `LateBoundIdMapper.put(I, V)` is public and the mapper is
otherwise mutable (no freeze/lock), but the field itself has no public accessor and is not a real
`Registry` — unlike `DataComponentType` or `BlockEntityType`, there is no `Registry.register`-style
door open to an add-on. `ItemModels` (which maps e.g. `create:model/normal` to its Java type) has
the exact same shape, confirming this is deliberate closed design, not an oversight specific to
select properties.

No Fabric API module (`fabric-rendering-v1`, `fabric-model-loading-api-v1`, or any other cached
`net.fabricmc.fabric-api` jar) mentions `SelectItemModelProperty` anywhere — grepped every cached
module jar's class list. **No registration is possible without a mixin.** Per the ticket's own
guidance ("prefer an accessor/invoker mixin over the fallback"), added the smallest possible reach:
`metered_motor.mixin.client.SelectItemModelPropertiesAccessor`, a `@Mixin(SelectItemModelProperties.class)`
interface with a single static `@Accessor("ID_MAPPER")` method (the standard Fabric pattern for a
private static field with no public door). `metered_motor.client.visual.TierProperty` (stateless,
mirroring `DisplayContext`'s shape) is `put` into the mapper under `metered_motor:tier` from
`MotorVisuals.register()`. This is the **first mixin in this codebase**: added
`src/main/resources/metered_motor.mixins.json` (client-only entry) and a `"mixins"` array in
`fabric.mod.json`. `org.spongepowered:mixin` (0.17.4+mixin.0.8.7, `JAVA_25` compatibility level
confirmed supported) is already on the compile classpath transitively through
`implementation(libs.fabricLoader)` — no `build.gradle.kts` change was needed. One gap found and
accepted: this Loom/toolchain setup (26.2's `minecraft-merged-deobf` jar, no intermediary layer)
configures **no Mixin annotation processor** (`./gradlew dependencies --configuration
annotationProcessor` is empty, `compileJava`'s `annotationProcessorPath` and `compilerArgs` are
both `[]`), so the accessor is not refmap-validated at compile time — Mixin resolves `ID_MAPPER`
directly against the loaded class's real (already-deobfuscated, unobfuscated-at-runtime-too) field
name instead. Confirmed working at runtime, not just compiling, two ways: `just gametest`'s "All 40
required tests passed" run loads the Mixin subsystem cleanly (Env=SERVER; client-only mixins are
correctly skipped, not applied, in that environment — expected, not a positive proof by itself);
`./gradlew runClient` was launched and watched to the item atlas stitch (`Created:
1024x512x0 minecraft:textures/atlas/items.png-atlas`, `flywheel` shaders loaded, no exception, no
Mixin apply-failure) before being stopped — item model baking (which would hard-crash on a bad
`minecraft:select` reference or an unregistered `metered_motor:tier` property) completed without
error. The tier-colour-in-inventory/hand/trade-screen visual remains Kevin's `just client` check
per the ticket.

**`create:model/normal`** is `com.zurrtum.create.client.infrastructure.model.NormalModel`
(implements `net.minecraft.client.renderer.item.ItemModel`; its `ID` field's string literal
`"model/normal"` found by grepping the jar's class bytes) — Create Fly's own client-side item
model type, registered into vanilla's `ItemModels.ID_MAPPER` by Create Fly itself. Confirmed by
example (`assets/create/items/creative_motor.json`, `mechanical_arm.json` in the Create Fly jar)
that it wraps a nested `"model"` id exactly like `minecraft:model` does. Nesting it inside a
vanilla `minecraft:select`'s `cases[].model` needed no code on this mod's side: model types are
resolved by id at data-load time, after Create Fly's own bootstrap has already run.

**JSON shape**, confirmed by `javap`-ing `SelectItemModel$Unbaked`, `$UnbakedSwitch` and
`$SwitchCase` and reading their field-name string constants directly out of the class bytes
(`type`, `property`, `cases`, `fallback`, `when`, `model`): `property` is a flat string id (no
wrapper object) when the `Type`'s property codec is `MapCodec.unit` (stateless, `TierProperty`'s
case) — matching vanilla's own `"property": "minecraft:display_context"` shape, not the
`"property": {"type": ..., ...}` shape a stateful property like `custom_model_data`'s `index`
would need.

**The item definition** (`assets/metered_motor/items/metered_motor.json`) is now:
```json
{
  "model": {
    "type": "minecraft:select",
    "property": "metered_motor:tier",
    "cases": [
      {"when": "i", "model": {"type": "create:model/normal", "model": "metered_motor:block/metered_motor/item_i"}},
      {"when": "ii", "model": {"type": "create:model/normal", "model": "metered_motor:block/metered_motor/item_ii"}},
      {"when": "iii", "model": {"type": "create:model/normal", "model": "metered_motor:block/metered_motor/item_iii"}}
    ],
    "fallback": {"type": "create:model/normal", "model": "metered_motor:block/metered_motor/item_i"}
  }
}
```
The old `models/item/metered_motor.json` was **removed**, not kept: the three per-tier model
files live at `models/block/metered_motor/item_i|ii|iii.json` (deep copies of the old file, only
the `5`/`6`/`particle` texture keys retargeted to each tier's `casing_<t>`/`motor_<t>`; `item_i`'s
content is byte-identical to the old file), grouped with the block models they share the recoloured
textures with, matching the resource ids the ticket's own Approach section named.

**`tools/icon.py`**: `MODEL_PATH` now reads `models/block/metered_motor/item_ii.json` directly;
`TIER_II_SUBSTITUTIONS` is gone along with its use in `load_texture`. `just icon` was re-run:
output is byte-identical to the pre-change icon (111,891 bytes, `cmp` clean, `git status` shows no
diff) — the tier II model's own textures already are what the substitution used to redirect to.

**Tests**: `ModelAssetsTest.everyItemDefinitionModelExists` (new) parses the item definition's
`"model"` references the same way `everyBlockstateModelExists` already parses the blockstate's,
and asserts all three tier cases are present; `everyModelsTextureReferenceResolves` covers the
three new per-tier item models for free since it already walks all of `models/` recursively. The
select property's mapping itself — `metered_motor.block.MotorTier.of(ItemStack)`, a new
server-safe overload `TierProperty.get` delegates to (tier I for no component or a
`Stats#readOnly()` component newer than this build, matching `MeteredMotorBlock`'s own placement
rule) — is exercised by a new `TierPropertyGameTest` (`src/gametest`, registered in
`metered_motor_gametest`'s `fabric.mod.json`) rather than a `src/test` unit test: no existing unit
test constructs an `ItemStack`, and this toolchain only bootstraps item/registry state for the
game-test environment, not for `src/test`.

**Check results**: `./gradlew check -x test -x runGameTest` (lint) and `verifyPurePackage` pass;
`python3 -m unittest discover -s tools` (4 tests, unaffected by this ticket) passes; `just map`
regenerated `docs/map.md` and five files under `docs/map/root/`, including a new
`metered_motor.mixin.client.md`. `just check` (`lint map-check test gametest`) is green end to
end: **"All 40 required tests passed :)"** (37 prior + 3 new `TierPropertyGameTest` cases).
