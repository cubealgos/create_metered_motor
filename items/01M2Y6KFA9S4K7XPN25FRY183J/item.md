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

- [ ] Unit test `ModelAssetsTest` extended: the item model definition references three item models and each resolves to existing textures.
- [ ] Game test or unit test: the select property yields `i`, `ii`, `iii` for stacks with the matching stats component and `i` for none.
- [ ] `just client`: the three debug items and a toolsmith's offer show their tier colour in the inventory, in hand and in the trade screen (Kevin's check).
- [ ] Merged through a Forgejo pull request into `development`.

## Constraints and prior findings

MM-7's Findings: the creative motor's `item.json` bakes its own static shaft; `create:model/normal` is Create Fly's item model type. MM-12's `tools/icon.py` reads the tier I item model and substitutes tier II textures; keep it working (it may read `item_ii` directly afterwards).
