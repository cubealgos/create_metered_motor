---
schema_version: 1
id: 01M2YBXMH4F2NB4FBYQP1R2G4D
key: MM-18
type: bug
title: "The motor screen: player inventory slots misaligned with the frame, readout unstructured and the window still too tall"
created_by: kevin
created_at: 2026-09-20T02:56:57Z
---

## Scope

Second client check of the motor screen (Kevin, 2026-09-20, screenshot `mm18-screen.png` in the session scratchpad): the player inventory's slots are misaligned with the drawn inventory frame (items sit left of and below their cells, the hotbar row worst), and the readout is an unstructured column of nine lines on a panel that is still far too tall. Make the screen compact and structured (`UI-REQ-001`, `UI-REQ-003`, `UI-REQ-004`): the whole window no taller than about 200 px so it fits at every GUI scale; the readout as a two-column table of label and value in four rows plus a state line; the five slots directly under it; the player inventory frame and its slots drawn from one shared origin so every item sits in its cell.

## Approach

Alignment: `MeteredMotorMenu.addPlayerSlots(x, y)` and `MeteredMotorScreen.renderPlayerInventory(x, y)` must use the same x and y, with the frame's internal slot offset (Create's `PLAYER_INVENTORY` texture has a border; read how `AbstractSimiContainerScreen.renderPlayerInventory` positions the grid relative to the texture's corner and what offset Create's own menus pass to `addPlayerSlots`, e.g. `ToolboxMenu`/`ToolboxScreen`, and copy that pair exactly). Readout: labels on the left in the dim colour, values right-aligned on the right in the bright colour, rows: Speed / Capacity, Efficiency / Rate at full load, Load / Emeralds inside, Remaining; a first line "Tier III · Stopped" (tier and state together); every value through `StatsText`. Panel width 176 (the inventory's width, so the frame edges align) or the stock-keeper atlas's opaque 208 with the inventory centred: decide from the mock. Body strips: only as many as the table plus the slot row need (three or four). The Pillow mock (`tools/screen_mock.py`) must now also draw the player inventory frame and outline every real slot position from the menu's constants, so the misalignment is visible headless; view it and iterate until slots and cells coincide.

## Acceptance criteria

- [x] `LayoutTest`: every player-inventory slot position from the menu falls inside the corresponding cell of the drawn frame (the test knows both origins), and the window's total height is at most 200.
- [x] The readout is the two-column table described above, wrapped or truncated to its column; the state and tier share the first line.
- [x] The Pillow mock shows the final layout with slot outlines matching the cells; reviewed by Claude, then Kevin's `just client`.
- [x] Merged through a Forgejo pull request into `development`.

## Constraints and prior findings

MM-17's Findings: the stock-keeper atlas is opaque only from u=24 to u=231 (208 px); `STOCK_KEEPER_REQUEST_SLOT` at u=32, v=200 is the slot art; `Layout` holds the pure constants. The approved look is `create_brass_compass`'s screens; Create's own container screens (toolbox) show how the player inventory is placed.

## Findings

Branch `bugfix/mm-18-screen-compact` (not merged; pushed for review).

- **The alignment bug was X-only, not Y.** `player_inventory.png`'s own built-in inset from the
  frame's corner to the first slot's *icon* (not its cell border) is `(8, 18)`, confirmed two
  independent ways and cross-checked: (1) Create Fly's own `ToolboxMenu.addSlots()` calls
  `addPlayerSlots(8, 165)` while `ToolboxScreen.extractBackground` calls `renderPlayerInventory(
  graphics, leftPos, topPos + imageHeight - PLAYER.getHeight())`; with
  `imageHeight = BG.getHeight() + PLAYER.getHeight() - 24 = 171 + 108 - 24 = 255` (`BG`/`PLAYER`
  dimensions read off `AllGuiTextures`'s `<clinit>`: `TOOLBOX` is 188x171, `PLAYER_INVENTORY` is
  176x108) and `PLAYER.getHeight()=108`, that frame sits at `topPos + 147`, and row 0's slots (at
  `topPos + 165`) sit `(8, 165-147)=(8, 18)` past the frame's own corner. (2) Sampling
  `player_inventory.png` directly (`javap`'d the atlas dimensions off `AllGuiTextures`'s
  `<clinit>`, Pillow on the extracted PNG): the dark top/left border of the row-0 cell ends and
  the grey 16x16 interior — where the item icon actually draws — begins at pixel `(8, 18)` from
  the texture's own top-left corner; the hotbar cell's interior begins at `(8, 76)`, which is
  `18 + 58` — the same `y + 58` `MenuBase.addPlayerSlots` itself adds for the hotbar row. Both
  agree. The previous (MM-17) `Layout` passed `PLAYER_INV_X` (the frame's own local x, no `+8`)
  straight into `addPlayerSlots`, so every player slot's item rendered 8px left of its own cell —
  matching the screenshot. `MAIN_INV_Y = TOP_HEIGHT + GAP + 18` already carried the correct `+18`
  (apparently a lucky guess, no comment explained it), so vertical alignment was never actually
  broken; the "below their cells" read in the ticket's own description was very likely the same
  8px horizontal shift read diagonally against the grid, not a second, separate vertical bug —
  there is no plausible source of a Y error once the (8, 18) inset and `MenuBase`'s own `y+row*18`/
  `y+58` formulas are accounted for, and the LayoutTest below now checks both axes explicitly
  against the independently-sourced `(8, 18)` literal (not `Layout.MAIN_INV_X`/`Y` themselves) so
  a regression would be caught either way. Fix: `Layout.MAIN_INV_X = PLAYER_INV_X + 8` (new),
  `MeteredMotorMenu.addSlots()` now passes `MAIN_INV_X` instead of `PLAYER_INV_X`.
- **Decision: panel width stays 208** (the stock-keeper atlas's own opaque span, MM-17), not 176 —
  the ticket's other option would have meant re-deriving a narrower atlas slice from scratch with
  no established opaque span to reuse; 208 with the player inventory frame centred under it
  (`PLAYER_INV_X = (208-176)/2 = 16`) was already proven and cost nothing to keep.
  `RIGHT_VALUE_RIGHT_X = 200` is exactly the panel's own right text edge (`WIDTH - TEXT_LEFT`).
- **The ≤200px budget does not fit five text lines (a header plus four table rows, at the font's
  own 9px line height) and an 18px slot row without also trimming the atlas's fixed bands.** With
  `TITLE_H=18` and the old `BOTTOM_H=12` unchanged, even the tightest possible body (zero padding,
  zero inter-line gap, GAP=0 between the panel and the player inventory frame) needs `45 + 18 =
  63px` of pure content against only `200 - 18 - 12 - 108 = 62px` of budget — short by 1px before
  a single pixel of breathing room is added. `BOTTOM_H` was trimmed from the compass's 12px to 6px
  (`BOTTOM_V` moved from 148 to 154): sampled directly (Pillow), that band is a plain
  light-to-dark-to-black gradient with no fine detail a shorter crop would cut into, confirmed by
  viewing the crop before committing to it. `GAP` (panel to player inventory frame) dropped from
  4 to 0 — Create's own `ToolboxScreen` does not even leave a gap; it *overlaps* its own BG and
  the player inventory frame by 24px (`imageHeight = BG.height + PLAYER.height - 24`), so touching
  with zero gap is well inside precedent, not a new risk. `PANELS` no longer needs to divide
  `BODY_HEIGHT` evenly (MM-17's own guard): the tileable body strip is a low-period (4px) dither
  with no larger pattern a partial-height crop would break, so the body is now sized to its exact
  content and finished with one final partial strip (`Layout.FULL_PANELS`/`LAST_PANEL_H`) instead
  of rounding up to the next whole 20px strip and eating the difference as dead padding — the
  rounding waste was the other ~17px MM-17's tiling convention would have cost here. Final numbers:
  `BODY_HEIGHT=67`, `TOP_HEIGHT=91`, `WINDOW_HEIGHT=199` — one pixel under the budget.
- **Table layout**: read literally, "a two-column table of four rows" listing seven stats in pairs
  separated by `/` cannot mean a strict single label/value pair per row (four rows would only hold
  four stats, not seven); it reads as four rows, each holding two label/value pairs side by side
  (an effective four-column grid: label, value, label, value), exactly as the ticket's own grouped
  examples show. Implemented that way: `LEFT_LABEL_X=8`, `LEFT_VALUE_RIGHT_X=100`,
  `RIGHT_LABEL_X=108`, `RIGHT_VALUE_RIGHT_X=200`, each half 92px wide. The last row (`Remaining`)
  has no right pair.
- **Colours**: neither compass screen (`EditScreen`, `SwitchScreen`) defines a dim/bright pair for
  a table like this one — both only ever use one panel-text colour. `COLOUR_VALUE` keeps MM-17's
  proven `0xFFCDBCA8` (header line and every value column); `COLOUR_LABEL = 0xFF7F7468` is that
  colour at roughly 62% brightness, picked to read as clearly secondary without vanishing against
  the panel's brown — not reused from anywhere, since nothing to reuse exists.
- **Truncation, not wrapping** (`UI-REQ-004`): a table row has no spare line height to wrap a long
  value into inside the ≤200px window, unlike MM-17's old single-column lines. `MeteredMotorScreen
  .drawPair` measures the label, computes the value's available width against the column's own
  right edge, and truncates with `Font.plainSubstrByWidth` plus an ellipsis rather than wrapping.
- **Pillow mock**: `tools/screen_mock.py` now also draws the "Inventory" label and outlines every
  real slot position `Layout` computes — the five motor slots (red) and all 36 player slots (blue,
  27 main + 9 hotbar) — plus the table text in a monospace placeholder font. Composed to
  `/private/tmp/claude-501/-Users-kevin-Documents-git-personal-create-civilization/bb7bc244-01b6-4890-9ca3-da5d384dc21e/scratchpad/mm18-mock.png`
  (208x199 at 4x = 832x796): every blue outline sits exactly on its real grid cell border (visible
  proof of the Part 1 fix — compare against the pre-fix screenshot, where items sat 8px left of
  every cell), the five red motor-slot outlines sit inside their warm request-slot art unchanged
  from MM-17, and the panel reads as one compact block with no empty field. The monospace
  placeholder font is visibly wider per character than Minecraft's own proportional font, so the
  right column's values sit closer to their 92px column budget in the mock than they will in the
  real client; that is a placeholder-font artifact (documented in the script's own docstring), not
  a real overflow — confirmed by re-checking with debug guide lines that no text crosses the
  panel's true right edge even in the mock, and the truncation safety net (previous finding)
  covers the real client regardless.
- `just check`: **`All 43 required tests passed :)`** (gametest), plus `LayoutTest` 8/8 and the
  rest of `./gradlew test` green. `just map` run; `docs/map.md`,
  `docs/map/root/metered_motor.menu.md` and `docs/map/root/metered_motor.menu.test.md` updated.
- Commit `642d723`, pushed. Not merged, not ticked — Kevin's own `just client` check and the PR
  are still open per the acceptance criteria.

### Follow-up after the mock review (Kevin, 2026-09-20)

- **The value column's right edge was already at the panel's own margin** (`RIGHT_VALUE_RIGHT_X =
  WIDTH - TEXT_LEFT = 200`, the same 8px inset the left column uses) — re-derived from scratch
  (`javap`'d `Layout`'s compiled constants directly, sampled the mock's own rendered pixels for
  the rightmost lit text pixel, drew debug guide lines at x=200/208) and could not reproduce an
  actual overflow past the panel's true edge (x=208) in either the code or the mock; the panel
  margin is only 8px, though, so the value visibly sits close to the border at 4x zoom with the
  mock's wide monospace placeholder font, which likely read as "running past" on a quick look.
  Added the explicit `LayoutTest.bothColumnsRightEdgesSitAtOrInsideThePanelsOwnMargin` regardless,
  since the invariant is worth guarding explicitly and the ticket asked for it by name.
- **The label colour fix went through two attempts.** The first (`SwitchScreen.COLOUR_ROW`,
  0xFF656565, "the brighter of the two colours the compass uses for its row text", as asked)
  turned out to be a worse choice than the original: measured by luma (`0.299r+0.587g+0.114b`),
  this panel's own brown (the same `PANEL_V` atlas band `EditScreen` also uses) averages ~103,
  `EditScreen`'s only candidate secondary colour `COLOUR_NAME` is ~85 (an 18-point gap — the
  original "almost invisible" complaint), and `SwitchScreen.COLOUR_ROW` is ~101 — a 2-point gap,
  effectively invisible, because `SwitchScreen` draws that colour on its own lighter tan
  `STOCK_KEEPER_CATEGORY_ENTRY` row texture, not this darker brown; neither compass colour was
  ever tuned for this panel. Landed on `COLOUR_VALUE` blended 55% toward the panel's own brown
  (0xFFAE907F, ~48 luma points above the panel, versus `COLOUR_VALUE`'s own ~88) — same warm hue
  family as the bright value column, confirmed legible in a re-rendered mock.
- `just check` re-run after both fixes: **`All 43 required tests passed :)`**; `LayoutTest` 9/9
  (the new assertion included). `just map` run again (`docs/map/root/metered_motor.menu.test.md`
  updated for the new test method).
- Mock re-rendered to the same path:
  `/private/tmp/claude-501/-Users-kevin-Documents-git-personal-create-civilization/bb7bc244-01b6-4890-9ca3-da5d384dc21e/scratchpad/mm18-mock.png`.
- Commit `9950447`, pushed. Still not merged, not ticked.
