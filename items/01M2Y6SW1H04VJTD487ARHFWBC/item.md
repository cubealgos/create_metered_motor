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
