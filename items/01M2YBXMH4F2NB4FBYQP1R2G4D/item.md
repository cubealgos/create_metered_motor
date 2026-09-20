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

- [ ] `LayoutTest`: every player-inventory slot position from the menu falls inside the corresponding cell of the drawn frame (the test knows both origins), and the window's total height is at most 200.
- [ ] The readout is the two-column table described above, wrapped or truncated to its column; the state and tier share the first line.
- [ ] The Pillow mock shows the final layout with slot outlines matching the cells; reviewed by Claude, then Kevin's `just client`.
- [ ] Merged through a Forgejo pull request into `development`.

## Constraints and prior findings

MM-17's Findings: the stock-keeper atlas is opaque only from u=24 to u=231 (208 px); `STOCK_KEEPER_REQUEST_SLOT` at u=32, v=200 is the slot art; `Layout` holds the pure constants. The approved look is `create_brass_compass`'s screens; Create's own container screens (toolbox) show how the player inventory is placed.
