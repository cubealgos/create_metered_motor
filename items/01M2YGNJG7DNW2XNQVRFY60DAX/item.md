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

- [ ] The mock shows a visible margin between the frame and every content element, and no triangle under the inventory; reviewed by Claude, then Kevin's `just client`.
- [ ] `LayoutTest` asserts the insets and that no content x lies within the new padding.
- [ ] Merged through a Forgejo pull request into `development`.

## Constraints and prior findings

MM-17 and MM-18's Findings: `Layout` constants, the opaque atlas span 24..231, the player inventory inset (8, 18), `WINDOW_HEIGHT` 199.
