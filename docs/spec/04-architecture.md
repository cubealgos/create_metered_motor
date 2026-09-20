---
title: "create_metered_motor spec — architecture: one kinetic block, one component, one screen, data trades"
type: "spec"
category: "create_metered_motor"
---

# 04 — Architecture

Sheet §3. Small on purpose: everything below is what a one-block kinetic mod needs and nothing it
does not.

## Shape

```
 data                              server                                    client
 ┌─────────────────────┐   offer   ┌─────────────────────────────────┐        ┌───────────────────────┐
 │ villager_trade/*.json│──────────►│ MeteredMotorItem (BlockItem)    │        │ MotorScreen (Create   │
 │  gives: fixed tier   │  created  │   tooltip from the component    │  menu  │  AbstractSimiContainer│
 │ tags/villager_trade/ │           │ MeteredMotorBlock               │◄──────►│  Screen): 5 slots,    │
 │   toolsmith/level_N  │           │   (DirectionalKineticBlock, IBE)│        │  meter readout        │
 └─────────────────────┘           │ MeteredMotorBlockEntity         │        │ Goggles overlay       │
                                   │   (GeneratingKineticBlockEntity,│  sync  │ MotorVisual (Flywheel │
                                   │    WorldlyContainer): stats,    │───────►│  SingleAxisRotating)  │
                                   │    inventory, meter, running    │        │                       │
                                   │   tick: load → meter → emeralds │        └───────────────────────┘
                                   │   → 64 rpm and the tier's       │
                                   │     capacity per rpm             │
                                   └─────────────────────────────────┘
```

**The block entity is the truth while placed; the item component is the truth in between.** The
`metered_motor:stats` component holds only the tier (`contracts/data-contract.md` version 2). No
loot function writes it: the trade's `gives` item template carries the tier fixed at authoring
time (`decisions/DEC-009-fixed-tiers.md`, `TRADE-REQ-002` withdrawn); placement copies the
component into the block entity; breaking copies it back. rpm (64, fixed) and stress capacity
(the tier's, expressed per rpm for Create) are derived from the tier in code, never stored. The
inventory and the meter live in the block entity only and drop as items when broken.

## `ARCH-DEC-001` — a Fabric mod on Create Fly, one jar, Java 25

Same toolchain and layout as `create_brass_compass` (`decisions/DEC-004-toolchain.md`): Loom 1.17,
Gradle 9.5.1, Kotlin DSL with a version catalog, one Gradle project, `fabric.mod.json` depending
on `fabricloader`, `minecraft ~26.2`, `java >= 25`, `create` pinned to the tested Create Fly
build, and `fabric-api`. **Cost if wrong:** ports track Create Fly's release cadence.

## `ARCH-DEC-002` — a Create kinetic source, not a mixin into Create

The block extends `DirectionalKineticBlock` and implements `IBE`; the block entity extends
`GeneratingKineticBlockEntity` and overrides `getGeneratedSpeed()` (64 while running, else 0) and
`calculateAddedStressCapacity()` (the tier's fixed capacity divided by 64 while running, else 0 —
Create wants stress capacity *per rpm*, confirmed by full bytecode disassembly of
`PoweredShaftBlockEntity` and `KineticNetwork` in `vault/technical/minecraft/create-fly-steam-engines-26-2.md`
§B; `domains/motor.md` `MOTOR-REQ-004`). Create's network, stress gauge, goggles and shaft
rendering then work unchanged, as they do for the creative motor. Load is read from the network
the way the stress gauge reads it.

**Alternatives:** a plain block that mixins into Create's network (rejected: fragile, and the base
classes exist for this) · a Create "motor" reskin through Create's own registrate (rejected: Create
Fly's internal registration is not an API for add-ons).

**Cost if wrong:** the kinetic base classes are Create Fly's internals; a release can move them.
Bounded by keeping every Create touch in the block, the block entity and the visual.

## `ARCH-DEC-003` — the inventory is a vanilla `WorldlyContainer`

Five slots exposed through `WorldlyContainer`, so vanilla hoppers and Create's funnels, chutes and
arms insert through `AllTransfer.getInventory`, with `canPlaceItemThroughFace` allowing only
emeralds and emerald blocks and `canTakeItemThroughFace` always false. No Fabric Transfer API
storage of the mod's own: Create Fly reaches containers, and a second path would have to agree
with the first. **Cost if wrong:** a mod that only speaks the Transfer API cannot feed it; Fabric
API's own container bridge covers vanilla containers, to be confirmed at the ticket.

## `ARCH-DEC-004` — trades are data, the tier's stats are code

The trade files and the tag entries are datapack files the mod ships; a pack author retunes price,
level and tag membership without Java (`TRADE-REQ-004`). **Amended (Kevin, 2026-09-20, `DEC-009`):**
originally the only code in the trade path was one loot function type, `metered_motor:roll`,
whose parameters (tier, rpm band, capacity band, efficiency band) lived in the trade file; that
function is withdrawn (`TRADE-REQ-002`). A trade's `gives` item template now carries the tier
directly as a fixed component, so there is no code at all in the trade path any more — the tier's
rpm, capacity and burn rate are looked up from the tier in the block entity and the burn formula,
both code, not data. A rebalance of the ladder itself is still a release, not a datapack.

## `ARCH-DEC-005` — the component is versioned from the first commit

`stats` carries a `version` field; a newer version read by an older build keeps the data untouched
and shows the item as unknown (`contracts/data-contract.md`).

## Runtime topology (sheet §3.1)

The mod runs inside the Minecraft client and server processes; no process, daemon or file of its
own. Block entities live in chunk data, items in inventories, both in the world save.

## Local state and on-disk layout (sheet §3.5)

Nothing of the mod's own beyond the world save. A resource pack may replace models and textures;
a datapack may replace the trade files and tag entries.

## Failure modes with no single owner (sheet §3.6)

| ID | Failure | Response |
|---|---|---|
| `ARCH-FAIL-001` | Create Fly missing or another version | Fabric Loader refuses to start with its dependency message; the mod adds nothing. |
| `ARCH-FAIL-002` | The chunk with the motor unloads while its network runs | Create handles unloaded sources as it does for any generator; the meter does not run while unloaded (no emeralds burn, no power is made). |
| `ARCH-FAIL-003` | A datapack removes the trade files but not the tag entries | The tag references a missing trade; vanilla logs and skips it; the villager draws from the rest. |
| `ARCH-FAIL-004` | A screen is open while the block is broken | Vanilla closes container menus whose block is gone; the emeralds drop. |
