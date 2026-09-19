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

- [ ] Game test: the five slots accept only emeralds and emerald blocks, including shift-click from the player inventory (`SlotRulesGameTest`).
- [ ] Game test: the synced readout fields (load, meter, state, emeralds inside) match the block entity's real values after a tick (`SyncGameTest`).
- [ ] Every screen string is a translation key with an `en_us` entry (`SourceSurfaceTest`, extended for this ticket's keys).
- [ ] `just client`: the screen opens, looks like Create's frame, and no line runs past the panel (Kevin's check, per `UI-REQ-004`).

## Constraints and prior findings

`UI-REQ-001..004`, `UI-REQ-007`, `UI-REQ-008`, `UI-DEC-001`, `UI-DEC-002`. Verified: Create Fly's screen framework (`MenuType` in `CreateRegistries.MENU_TYPE`, `MenuBase`, `AbstractSimiContainerScreen`, `AllGuiTextures`, tinted glyph blits) is in production use in `create_brass_compass` (spec `README.md` Verification 9). `create_brass_compass`'s BC-10 hotfix: an unwrapped string overflowed a 192px panel — wrap or truncate every readout line to the frame from the start. Blocked by MM-4 (the inventory and meter this screen exposes).
