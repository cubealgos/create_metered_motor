---
schema_version: 1
id: 01M2Y6SW1H04VJTD487ARHFWBC
key: MM-17
type: bug
title: "The motor screen: cut-off title, oversized empty panel, floating grey slots, a seam, a contradictory Idle line"
created_by: kevin
created_at: 2026-09-20T01:27:31Z
---

## Scope

The motor screen's layout does not read as Create's (Kevin, 2026-09-20, client check; screenshot in the session scratchpad `mm17-screen.png`): the title bar is cut off at the top of the window; the brown panel is far taller than its nine readout lines, leaving a large empty field; the five slots float in the lower half on plain grey slot backgrounds that match no Create screen; the tiled body strips show a visible seam column at the left edge; the readout's remaining-time line reads "Idle" directly above "Running", which contradicts itself; the light text on the brown panel is low-contrast. Fix the screen so it looks like `create_brass_compass`'s screens (approved by Kevin): the panel sized to its content, the title fully visible, the slot strip on a Create slot graphic directly under the readout, no seam, the compass screens' text colours, and the remaining line reading "Remaining: no load" (or the time) (`UI-REQ-001`, `UI-REQ-003`, `UI-REQ-004`).

## Approach

Measure: nine lines at the font's line height plus one gap, one 18 px slot row plus padding; tile as many 20 px body strips as that needs (three or four, not seven). Title: check `AbstractSimiContainerScreen`'s `setWindowOffset`/`imageHeight` and vanilla's centring so the whole window fits at GUI scale 2 to 4 on a 1080p client; the compass's `EditScreen` centres its window with the same superclass, copy its arithmetic. Seam: the body strip's left column of the atlas slice is the frame's own border; blit the strip at the exact atlas columns the compass uses (compare `EditScreen`'s `ATLAS_U`/width) and confirm by viewing a rendered mock (a Pillow composite of the atlas regions at the intended coordinates is enough to see seams headless; do it and include the PNG path in the report). Slots: pick a Create slot graphic used with real slots (`AllGuiTextures.TOOLBOX_SLOT`, `SCHEMATIC_SLOT`, the filter screen's slot cells, or the stock keeper's `STOCK_KEEPER_REQUEST_SLOT`-like entries; list what `AllGuiTextures` offers and choose the one that sits on this panel). Colours: the compass `EditScreen`/`SwitchScreen` text colours. Keep every existing test (`SyncGameTest` reads `StatsText`, untouched) green.

## Acceptance criteria

- [ ] The window's total height fits on a 1080p client at GUI scale 4 and the title is fully visible (a unit test on the layout constants: `TOP_HEIGHT + GAP + inventory height ≤ 270`, and the title's y within the title strip).
- [ ] The panel holds exactly the readout lines and the slot strip with no empty field below them; the slot backgrounds are a Create slot graphic; no seam (Pillow mock reviewed by Claude, then Kevin's `just client`).
- [ ] The remaining-time line never reads "Idle"; it reads a time or "Remaining: no load"; the state line stays.
- [ ] `just client`: Kevin's check of the screen.
- [ ] Merged through a Forgejo pull request into `development`.

## Constraints and prior findings

MM-6's Findings: neither of Create's candidate frames carries real slots; the compass's stock-keeper atlas slices at 224 px width are the proven panel. `create_brass_compass` `EditScreen`/`SwitchScreen` are the approved look.

## Findings

Branch `bugfix/mm-17-screen-layout` (not merged; pushed for review).

- **The seam was a real 8 px hole, not a rendering artifact.** Sampling `stock_keeper.png`
  directly (`javap`'d `AllGuiTextures`'s `<clinit>` for the atlas coordinates, then Pillow on the
  extracted PNG) shows the title band is opaque from u=16, but the panel band (`PANEL_V=48`) and
  the bottom band (`BOTTOM_V=148`) are fully transparent (alpha 0) at u=16..23 and u=232..239 —
  opaque only at u=24..231 (208 px). `create_brass_compass`'s own `EditScreen` blits those two
  bands at the wider u=16, width=224 too, so it very likely carries the same 8 px transparent gap
  at each edge of its panel/bottom bands; it just doesn't read as a seam there because its own
  backdrop is Minecraft's dark inventory blur, not an outdoor scene. This ticket's fix (u=24,
  width=208 for every band, `Layout.ATLAS_U`/`Layout.WIDTH`) does not touch the compass — worth a
  look there if a similarly bright backdrop ever exposes it.
- **The 224 px width from MM-6's finding above is the atlas slice `EditScreen` blits, not its
  opaque span.** The two aren't the same; 208 px (u=24..231) is what's actually opaque.
- **Slot graphic chosen: `AllGuiTextures.STOCK_KEEPER_REQUEST_SLOT`'s own atlas region**, read
  directly off `stock_keeper.png` at u=32, v=200, 18x18 (not through the enum, since only its
  `Identifier` is reused — the same atlas the panel already comes from, so no second texture
  load). It's a warm brown-and-tan bordered square, not the flat grey `AllGuiTextures.JEI_SLOT`
  the screen drew before. `TOOLBOX_SLOT` does not exist in this Create Fly version's
  `AllGuiTextures` (the ticket's own example name); `SCHEMATIC_SLOT` (u=54, v=0 in `widgets.png`,
  16x16) was the other real candidate but is a different, smaller atlas file and size.
- **The title cut-off and the oversized panel were the same bug**: seven tiled 20 px body strips
  (140 px) against nine lines that need 90 px left ~22 px of dead panel, pushing the slot row down
  and the whole window to 282 px (170 + 4 + 108) — past a 1080p client's ≤270 px budget at GUI
  scale 4, which is what pushed `topPos` (and the title drawn at it) up off the visible screen
  when Minecraft centred the window. Six 20 px strips (120 px) fit the nine lines, the slot gap
  and the slot row exactly, with only the 4 px bottom padding left over; the window is now 262 px.
- **The "low-contrast text" turned out to already match the compass exactly** —
  `COLOUR_TITLE=0xFF4A2D31` and the panel text colour `0xFFCDBCA8` are byte-for-byte
  `EditScreen`'s `COLOUR_TITLE`/`COLOUR_ON_PANEL`, and both draw without a shadow, same as the
  compass. No colour or shadow change was made; the mock (below) reads clearly once the panel no
  longer surrounds the text with a large empty field, which was likely what read as "low
  contrast" against the mostly-empty brown.
- **Pillow mock**: `tools/screen_mock.py` (Pillow only, not wired into `just check`), run as
  `python3 tools/screen_mock.py --jar <create-fly jar> --out <path>`. Composed to
  `/private/tmp/claude-501/-Users-kevin-Documents-git-personal-create-civilization/bb7bc244-01b6-4890-9ca3-da5d384dc21e/scratchpad/mm17-mock.png`
  (208x262 at 4x = 832x1048): no seam, title inside its strip, panel snug around the nine lines
  and the slot row, slots on the warm request-slot art. The player inventory frame's own
  downward-pointing notch at the bottom is `player_inventory.png`'s real, unmodified art (every
  `AbstractSimiContainerScreen` renders it the same way) — not something this ticket touched.
- `just check`: `All 37 required tests passed :)` (gametest), plus `LayoutTest` (5/5) and
  `SourceSurfaceTest` (2/2) green under `./gradlew test`. `just map` run; `docs/map.md` and
  `docs/map/root/metered_motor.menu.md` updated, `metered_motor.menu.test.md` added.
- Commit `41fae8a`. Not merged, not ticked — `just client` (Kevin's own check) and the PR are
  still open per the acceptance criteria.
