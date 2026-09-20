---
title: "create_metered_motor spec — context: why, for whom, and what it will not do"
type: "spec"
category: "create_metered_motor"
---

# 00 — Context

## Why this exists

Create's power sources are free once built: a water wheel turns forever, a steam engine burns
whatever fuel the base already produces. The metered motor is a power source that costs money to
run. It comes from a villager, not a crafting table: three motors matching a small, a mid and a
full steam set-up, one per toolsmith level, each fixed to a real steam-engine configuration rather
than rolled (`decisions/DEC-009-fixed-tiers.md`), so a player who wants a stronger one levels a
toolsmith to Expert or Master rather than hunting for a lucky offer. Once placed, it turns a shaft
as any Create generator does and draws emeralds from its own small inventory in proportion to the
work its network demands.

It is the second small add-on on the way to `create_civilization`, whose ENERGY domain assumes a
metered motor that bills an account. This one bills a hopper-sized inventory, and in building it
the team learns what the civilization needs next: a kinetic block on Create Fly, a block entity
with an inventory that hoppers and Create's logistics can feed, data-driven villager trades with
fixed per-tier stats, a component that travels between item and block, and a screen fed from a
block.

## Who it is for

- Players on Minecraft 26.2 with Fabric and Create Fly, single player or on a server, who are past
  the water-wheel stage and trade with villagers.
- Server operators who install it alongside Create Fly and expect nothing to configure.
- Modpack authors who want an emerald sink and a power source that fits Create's progression and
  can be retuned through datapack trade files.

## Business context

No business model, no revenue, no telemetry. Published on Modrinth under MIT, source on the
cubealgos Forgejo with a GitHub mirror and tracker (`decisions/DEC-003-licence.md`). Support is a
public issue tracker and nothing more.

## What it will not do

- No new fuel: emeralds and emerald blocks only, no currency item, no bank.
- No crafting recipe at 1.0; the villager is the only source (`decisions/DEC-005-acquisition.md`).
- No stat editing after purchase: no upgrades, no repair, no merging two motors.
- No new villager profession and no changes to vanilla trades other than adding to the toolsmith's
  level tags.
- No fluid or item output; it turns a shaft and burns emeralds, nothing else.
- No configuration file at 1.0; tiers and prices are data files a datapack can override.
- No wireless or remote metering; the civilization's account binding is out of scope here.

## Success

Kevin finds a toolsmith offering a motor, buys it, places it against a shaft, drops emeralds in a
hopper above it, watches Create's stress gauge climb while the emerald count falls at a rate that
matches the load, levels a toolsmith to Master for the next tier, and the whole thing looks like it
belongs to Create.
