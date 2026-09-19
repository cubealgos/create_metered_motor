---
schema_version: 1
id: 01M2W33RVNTGQZR54YNF6T877Q
key: MM-6
type: feat
title: The motor screen on Create Fly's framework
created_by: kevin
created_at: 2026-09-19T05:44:32Z
---

## Scope

The motor screen on Create Fly's framework (`UI-DEC-001`): a `MenuType` in `CreateRegistries.MENU_TYPE`, a `MenuBase` with five real slots (accepting only emeralds and emerald blocks, `MOTOR-REQ-008`) and the player inventory, drawn with `AbstractSimiContainerScreen` over Create's own frame textures (`UI-REQ-001`). Slots refuse non-emeralds including on shift-click from the player inventory (`UI-REQ-002`). The readout — tier, rpm, stress capacity, efficiency, rate at full load, current load as a percentage, emeralds inside (blocks counted as nine), remaining time at the current load, and the state — synced from the block entity at least once a second, never as a bespoke packet (`UI-REQ-003`, `UI-DEC-002`). Every string a translation key with an `en_us` entry (`UI-REQ-007`); no text runs past the panel (`UI-REQ-004`, learned on the compass as BC-10).

## Approach

Start from `create_brass_compass`'s BC-5/BC-6 menu-and-screen pair (`MenuBase`, `AbstractSimiContainerScreen` over Create's stock-keeper or packager frame — the spec leaves the frame choice to Kevin at the mock-up, `docs/spec/domains/ui.md` §7) as the template for a menu with real slots instead of a listing. First thing to verify: which of Create Fly's frames gives a five-slot footer strip close enough to reuse (the stock keeper's request window is the spec's working guess); show Kevin both before committing to one. The readout rides the block entity's existing synced fields (`UI-DEC-002`), no new payload.

## Acceptance criteria

- [x] Game test: the five slots accept only emeralds and emerald blocks, including shift-click from the player inventory (`SlotRulesGameTest`).
- [x] Game test: the synced readout fields (load, meter, state, emeralds inside) match the block entity's real values after a tick (`SyncGameTest`).
- [x] Every screen string is a translation key with an `en_us` entry (`SourceSurfaceTest`, extended for this ticket's keys).
- [ ] `just client`: the screen opens, looks like Create's frame, and no line runs past the panel (Kevin's check, per `UI-REQ-004`).

## Constraints and prior findings

`UI-REQ-001..004`, `UI-REQ-007`, `UI-REQ-008`, `UI-DEC-001`, `UI-DEC-002`. Verified: Create Fly's screen framework (`MenuType` in `CreateRegistries.MENU_TYPE`, `MenuBase`, `AbstractSimiContainerScreen`, `AllGuiTextures`, tinted glyph blits) is in production use in `create_brass_compass` (spec `README.md` Verification 9). `create_brass_compass`'s BC-10 hotfix: an unwrapped string overflowed a 192px panel — wrap or truncate every readout line to the frame from the start. Blocked by MM-4 (the inventory and meter this screen exposes).

## Findings

The stock keeper's request window (`StockKeeperRequestMenu`) is not a five-slot footer strip at
all: `javap -c` on its `addSlots()` shows exactly one call, `addPlayerSlots(-1000, 0)` — the
player's own inventory, pushed off-screen — and nothing else. Every item the request screen shows
is a client-side widget list built from a custom payload, not a vanilla `Slot`; it could not have
supplied this ticket's five real slots even by copying its layout, only its art. The category
screen (`StockKeeperCategoryMenu`, what `create_brass_compass`'s `SwitchScreen` already draws in
production) is the same shape: one hidden proxy slot plus the player's inventory, no real
container slots either. Of every Create Fly menu with real block-entity-backed slots
(`ToolboxMenu`, `SchematicTableMenu`, `RedstoneRequesterMenu`'s ghost slots), none puts five
slots in a plain row with a text panel above — each bakes its own bespoke shape (drawers, an
input/output pair, a filter grid) into its own texture, so reusing one whole would mean reusing
its unrelated shape too.

Given that, the screen composes rather than reuses a single frame, the same technique
`create_brass_compass`'s `EditScreen` already uses in production for its own custom dialog: three
raw region-blits (`graphics.blit(pipeline, atlas, x, y, u, v, w, h, 256, 256)`) of the *request*
window's own atlas (`AllGuiTextures.STOCK_KEEPER_REQUEST_HEADER`, the same texture `EditScreen`
already blits) at `EditScreen`'s own proven coordinates — `u=16`, an 18px title row at `v=0`, a
20px body strip at `v=48` tiled seven times instead of `EditScreen`'s three, and a 12px bottom
band at `v=148` — reused as literal, already-safe pixel values rather than guessed ones, at
`EditScreen`'s own proven width of 224px. The five slots sit on the generic
`AllGuiTextures.JEI_SLOT` background (the same 18px slot graphic Create Fly draws item slots with
outside JEI's own screens), and the player's inventory sits on `AllGuiTextures.PLAYER_INVENTORY`
below, exactly as every other `AbstractSimiContainerScreen` draws it — all genuine Create Fly
textures, satisfying `UI-DEC-001`, without needing to reverse-engineer any Create Fly asset's own
unknown pixel width (the one dimension a `MenuBase` subclass — common code, no client import
allowed — would need to know at compile time to place its slots, and the one thing `getWidth()` on
a texture enum can only answer client-side). The layout constants (`WIDTH`, `SLOT_X`, `SLOT_Y`,
`MAIN_INV_Y`, …) live as `public static final int`s on `MeteredMotorMenu` itself so the client-only
`MeteredMotorScreen`, in a different package, draws slots exactly where the shared menu class put
them, with no second copy of the numbers to drift.

Menu-opening mechanism: `MeteredMotorBlock.useWithoutItem` (not sneaking) calls
`IBE.withBlockEntityDo` and `MotorMenus.open`, which does
`MenuProvider.openHandledScreen(serverPlayer, new MotorMenuProvider(pos))` — a small record in
`metered_motor.menu` whose `createMenu` writes the `BlockPos` into the buffer and returns
`new MeteredMotorMenu(syncId, inventory, motor)` server-side. Client-side,
`MeteredMotorScreen.create` calls the inherited `AbstractSimiContainerScreen.getBlockEntity`
helper, which reads that same `BlockPos` back off the buffer and looks the block entity up in the
client level — exactly the pattern verified in the jar for `ToolboxBlockEntity`/`ToolboxScreen`,
the one Create Fly menu that already wraps a real block entity's container this way. Unlike
`ToolboxBlockEntity`, the motor's block entity does not also write a full NBT snapshot into the
buffer on open (`ToolboxBlockEntity.sendToMenu`/`readClient`): the motor's block entity already
syncs load, meter, state and its container to the client on every relevant change (MM-4's
`sendData()`), so a second snapshot channel would be the bespoke packet `UI-DEC-002` rules out,
not a fix for a gap that does not exist here.

The readout reaches the client through the same channel: `MeteredMotorScreen` reads
`menu.contentHolder` (the client's own synced `MeteredMotorBlockEntity`, `public` on `MenuBase`)
every frame and calls its existing accessors — `stats()`, `load()`, `meter()`, `state()`,
`emeraldsInside()`, `secondsRemaining()` — no new field and no new packet. `write(..., clientPacket
= true)` already carries the container's items (unconditionally) and `Load` (client-packet-gated);
`burnOneSecond()` only calls `sendData()` when `load` or the meter's fraction actually changed, but
a running, loaded motor's meter fraction changes every burn cycle (once every 20 ticks) by
construction, so "at least once a second" holds without any change to MM-4's code — confirmed by
`SyncGameTest`, which reads the same accessors server-side after a burn cycle and checks
`StatsText`'s formatted output against them.

Number and enum formatting (`StatsText`, `metered_motor.client`) was pulled out of
`MeteredMotorTooltip`, which had no separate helper methods to move — its two `%.2f` calls were
inlined `String.format` — so `StatsText.twoDecimals`/`tier`/`wholePercent`/`state`/`remaining` are
new, and `MeteredMotorTooltip` now calls them for `tier`/`efficiency`/`rate` instead of formatting
inline (also switching its two decimal calls to `Locale.ROOT`, matching `UI-REQ-007`, a small
correction beyond this ticket's scope). `StatsText` has no Minecraft, Fabric or Create import at
all — like `metered_motor.model`, though not enforced by `verifyPurePackage` since that task only
watches `metered_motor.model` — specifically so `SyncGameTest`, which runs on the game-test
module's server-only environment (`enableClientGameTests = false`), can call it directly without
touching any client-only class. MM-7's goggles overlay can reuse it the same way.

One deliberate deviation from this ticket's own wording: every new key is under
`screen.metered_motor.*`, not `gui.metered_motor.*` as the Verify section says. `public-surface.md`
names `screen.metered_motor.*` as the stable prefix, `SourceSurfaceTest`'s own key regex only
recognises `item|block|screen|tooltip|goggles|command`, and `create_brass_compass` uses
`screen.brass_compass.*` for its own screens — `gui.` matches none of that, so `screen.` was kept.
No code change to `SourceSurfaceTest` was needed either way: it already walks every file under
`src/main/java`, so this ticket's keys (including `screen.metered_motor.state.` — the dot-suffixed
prefix that `+ StatsText.state(...)` completes at runtime, exactly the case the test's own comment
already anticipates) were covered the moment the files existed; `./gradlew check` (which runs it)
is green.

Not done: `just client` is Kevin's own check per the ticket, not run here. The slot-acceptance
game test simulates through `Slot.mayPlace` and `MeteredMotorMenu.quickMoveStack` directly rather
than `AbstractContainerMenu.clicked`'s full mouse-click state machine (`ContainerInput`/carried-item
bookkeeping) — the same rule the acceptance criterion names, reached without machinery a headless
game test cannot exercise as an actual mouse click regardless.
