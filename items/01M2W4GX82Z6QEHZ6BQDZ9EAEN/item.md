---
schema_version: 1
id: 01M2W4GX82Z6QEHZ6BQDZ9EAEN
key: MM-11
type: docs
title: "Modrinth listing assets ahead of the release: badge icon, paste-ready body and settings, gallery shot list"
created_by: kevin
created_at: 2026-09-19T06:09:11Z
---

## Scope

Kevin (2026-09-19): everything for the Modrinth page that can be produced before the code exists, so the release ticket only uploads. `docs/modrinth/body.md` (project settings: name, slug `metered-motor`, summary, categories, licence, sides, loader, game version, dependencies Create Fly and Fabric API; version settings; the markdown body from the spec's context and domains), `docs/modrinth/gallery.md` (five shots with captions, taken later on the release build), `tools/icon.py` adapted from the compass (the Create blueprint badge; the subject is the motor's item sprite, which does not exist before MM-7, so a placeholder 16x16 motor sprite under `docs/modrinth/placeholder-motor.png` is drawn now and the recipe re-renders from the real sprite when it exists), `docs/modrinth/icon.png` rendered from the placeholder, `just icon` working.

## Approach

Copy `create_brass_compass/docs/modrinth/body.md`, `gallery.md` and `tools/icon.py`; rewrite the text from `docs/spec/00-context.md`, `domains/motor.md` and `domains/trade.md`; draw the placeholder with Pillow in the flat pixel style of Create's icons (a brass-framed block with a green core and a shaft); parameterise `tools/icon.py` on a sprite path with the placeholder as default until MM-7 changes the default.

## Acceptance criteria

- [ ] `docs/modrinth/body.md`, `docs/modrinth/gallery.md`, `docs/modrinth/icon.png` (512x512, under 256 KiB) and `tools/icon.py` exist; `just icon` regenerates the icon; the body cites no number the spec does not (tiers, prices, burn); merged through a Forgejo pull request.

## Constraints and prior findings

Create add-on icon theme and palette: personal vault `vault/technical/minecraft/create-fly-26-2.md` § "Create add-on icon theme". Modrinth icon cap 256 KiB, gallery 5 MiB.
