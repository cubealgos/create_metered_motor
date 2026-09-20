---
schema_version: 1
id: 01M2YGNJG7DNW2XNQVRFY60DAX
key: MM-20
type: bug
title: "The motor screen: padding between frame and content, no pointer triangle under the inventory"
created_by: kevin
created_at: 2026-09-20T04:19:56Z
---

## Scope

Two small screen changes from Kevin's third client check (2026-09-20, screenshot `mm20-screen.png` in the session scratchpad): (1) the panel content sits directly against the frame border; add an inner padding so the header line, the table's two columns and the slot strip all keep a clear margin from the frame on every side, and a gap between the table and the slot row; (2) the player inventory frame shows a pointer triangle sticking out at the bottom, part of Create's `PLAYER_INVENTORY` texture meant for screens that sit above the hotbar; draw the frame without it (`UI-REQ-001`, `UI-REQ-004`).

## Approach

Padding: raise `Layout`'s text inset from 8 to about 12 px on the left and right, the header's y by a few px below the title strip, and put one line's worth of gap between the last table row and the slot row; tile one more body strip if the content needs it, staying under the 200 px window height (check `LayoutTest`'s bound; loosen to 210 only if unavoidable and say so). Triangle: read the `PLAYER_INVENTORY` texture region (`AllGuiTextures.PLAYER_INVENTORY`'s u, v, width, height, viewed with Pillow from the atlas PNG) and find the rows that hold the triangle (bottom ~8 px, centred); blit the frame with a height that stops above them (a local `region` blit of the same atlas coordinates minus the triangle rows, or two blits if the frame's bottom border needs to be drawn from a row above the triangle), and shrink the window height accordingly. Update `tools/screen_mock.py` and the mock, and `LayoutTest`.

## Acceptance criteria

- [x] The mock shows a visible margin between the frame and every content element, and no triangle under the inventory; reviewed by Claude, then Kevin's `just client`.
- [x] `LayoutTest` asserts the insets and that no content x lies within the new padding.
- [x] Merged through a Forgejo pull request into `development`.

## Constraints and prior findings

MM-17 and MM-18's Findings: `Layout` constants, the opaque atlas span 24..231, the player inventory inset (8, 18), `WINDOW_HEIGHT` 199.

## Findings

Branch `bugfix/mm-20-screen-padding` (not merged; pushed for review), built on MM-18's compact
screen (development at `c9fb2a8`).

- **Padding: four `Layout` constants widened, each read literally off the ticket's own wording.**
  `TEXT_LEFT` 8 → 12 (the table's side margin). `LINE_TOP_PADDING` 1 → 3 (the header now sits
  visibly below the title strip's own bottom edge, not almost touching it). `SLOT_GAP` 2 → 9 — set
  to exactly `LINE_STRIDE`, "a full line's gap", rather than picking an arbitrary larger number.
  `BOTTOM_PADDING` 1 → 3 (the slot row and the bottom band no longer touch). `COLUMN_GAP` (8) and
  the atlas panel coordinates (MM-17's proven 208px span) were untouched — the ticket only asked
  for margins and gaps, not a wider panel.
- **Triangle: `player_inventory.png` (`AllGuiTextures.PLAYER_INVENTORY`'s own 176x108 file,
  `startX=startY=0`, confirmed by `javap`'d bytecode on the constructor chain and cross-checked by
  sampling the extracted PNG with Pillow) is clean only through row 96.** Every row from 97 down is
  already part of the triangle's own build-up, not a separate defect starting cleanly at row 100 as
  the ticket's own approach guessed: row 97 (the frame's shadow-grey tone, which would otherwise
  read as a finishing border) has a ~14px gap centred on columns 45..58 where the lighter fill
  colour continues instead — the triangle's own "collar"; row 98 repeats that gap and adds the
  triangle apex's first antialiasing bleed (columns 44-45, 58); row 99, the frame's actual solid
  black border line, is broken outright by the triangle at columns 44..59. The ticket's own
  fallback ("if the border is part of the triangle rows, blit it from the row just above") turned
  out to have no clean row left to fall back to — every darker/border-toned row already carries the
  collar. Rows 100-107 are the triangle itself (a narrowing wedge centred on column 51, transparent
  elsewhere). Settled on `Layout.PLAYER_INVENTORY_HEIGHT = 97` (rows 0..96): the tallest crop with
  zero triangle pixels of any kind, at the cost of a flat, undecorated bottom edge instead of a
  shadowed one (verified by rendering h=96/97/98 crops side by side and by the composed mock — h=98
  still showed a faint light nub at 4x zoom; h=97 was fully clean). `Layout.
  PLAYER_INVENTORY_TEXTURE_HEIGHT = 108` keeps the source file's own declared height for reference;
  nothing layout-facing reads it.
- **`MeteredMotorScreen` no longer calls `AbstractSimiContainerScreen.renderPlayerInventory`**
  (always blits the full, uncropped 108px texture) — a new private `playerInventoryFrame` blits the
  same atlas region that method's own `AllGuiTextures.render` would (u=0, v=0, a 256x256 texture)
  at the cropped height, then draws the inherited `playerInventoryTitle` field itself at the same
  `(x+8, y+6)` and the same colour that method uses (`javap`'d the constant int `-12566464` =
  `0xFF404040`; the mock's earlier `(65,65,65)` guess was close but not exact), so the "Inventory"
  label is pixel-for-pixel what Create draws, per the ticket's own instruction to keep it as Create
  draws it.
- **The 10px+ saved by cropping the frame paid for all four padding increases and then some**:
  `WINDOW_HEIGHT` moved from 199 to 199 again — the four padding increases cost 11px
  (`BODY_HEIGHT` 78 vs MM-18's 67) and the frame crop saved 11px (108 → 97), landing the window
  exactly where MM-18 left it, one pixel under the 200px budget. `LayoutTest`'s `WINDOW_BUDGET`
  (200) did not need loosening to 210.
- **`MeteredMotorScreen`'s constructor now reads `MeteredMotorMenu.WINDOW_HEIGHT`** (newly
  re-exported from `Layout.WINDOW_HEIGHT`) instead of recomputing
  `TOP_HEIGHT + GAP + PLAYER_INVENTORY.getHeight()` — that expression used Create's own
  `AllGuiTextures.getHeight()` (108, the full uncropped texture), which would have silently
  reintroduced 11px of dead space (and made the window taller than the frame actually drawn) once
  the frame itself started blitting a shorter crop.
- **`LayoutTest`**: added `thePlayerInventoryFrameIsCroppedAboveItsOwnPointerTriangle` (the crop is
  actually shorter than the source texture, not just documented as such),
  `theFourMM20PaddingInsetsAreAsWide` (each of the four insets, checked against the ticket's own
  wording rather than re-deriving `Layout`'s formulas) and `noContentXLiesWithinTheNewSideMargins`
  (the acceptance criterion's own wording: no content x inside the new padding) — 12/12 green.
- **Pillow mock**: `tools/screen_mock.py` now also crops the player inventory paste to
  `PLAYER_INVENTORY_HEIGHT` (97) and draws the "Inventory" label in Create's own measured colour.
  Composed to
  `/private/tmp/claude-501/-Users-kevin-Documents-git-personal-create-civilization/bb7bc244-01b6-4890-9ca3-da5d384dc21e/scratchpad/mm20-mock.png`
  (208x199 at 4x = 832x796): a clear margin on every side, a full blank line between the table and
  the slot row, a visible gap between the slot row and the bottom band, and no pointer triangle
  under the inventory frame.
- `just check`: **`All 43 required tests passed :)`** (gametest), plus `LayoutTest` 12/12 and the
  rest of `./gradlew test` green (`lint`/`check -x test -x runGameTest` also green). `just map` run;
  `docs/map.md`, `docs/map/root/metered_motor.menu.md` and
  `docs/map/root/metered_motor.menu.test.md` updated.
- Commit `bf42c3d`, pushed. Not merged, not ticked — Kevin's own `just client` check and the PR are
  still open per the acceptance criteria.
