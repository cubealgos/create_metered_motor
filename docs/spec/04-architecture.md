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
 │ villager_trade/*.json│──────────►│ RollFunction (loot function)    │        │ MotorScreen (Create   │
 │ tags/villager_trade/ │  created  │   writes MotorStats component   │        │  AbstractSimiContainer│
 │   toolsmith/level_N  │           │ MeteredMotorItem (BlockItem)    │  menu  │  Screen): 5 slots,    │
 └─────────────────────┘           │   tooltip from the component    │◄──────►│  meter readout        │
                                   │ MeteredMotorBlock               │        │ Goggles overlay       │
                                   │   (DirectionalKineticBlock, IBE)│        │ MotorVisual (Flywheel │
                                   │ MeteredMotorBlockEntity         │  sync  │  SingleAxisRotating)  │
                                   │   (GeneratingKineticBlockEntity,│───────►│                       │
                                   │    WorldlyContainer): stats,    │        └───────────────────────┘
                                   │    inventory, meter, running    │
                                   │   tick: load → meter → emeralds │
                                   │   → speed and capacity          │
                                   └─────────────────────────────────┘
```

**The block entity is the truth while placed; the item component is the truth in between.** The
`metered_motor:stats` component holds tier, rpm, stress capacity and efficiency. The roll function
writes it onto the offered item; placement copies it into the block entity; breaking copies it
back. The inventory and the meter live in the block entity only and drop as items when broken.

## `ARCH-DEC-001` — a Fabric mod on Create Fly, one jar, Java 25

Same toolchain and layout as `create_brass_compass` (`decisions/DEC-004-toolchain.md`): Loom 1.17,
Gradle 9.5.1, Kotlin DSL with a version catalog, one Gradle project, `fabric.mod.json` depending
on `fabricloader`, `minecraft ~26.2`, `java >= 25`, `create` pinned to the tested Create Fly
build, and `fabric-api`. **Cost if wrong:** ports track Create Fly's release cadence.

## `ARCH-DEC-002` — a Create kinetic source, not a mixin into Create

The block extends `DirectionalKineticBlock` and implements `IBE`; the block entity extends
`GeneratingKineticBlockEntity` and overrides `getGeneratedSpeed()` (the rolled rpm while running,
else 0) and `calculateAddedStressCapacity()` (the rolled capacity while running, else 0). Create's
network, stress gauge, goggles and shaft rendering then work unchanged, as they do for the creative
motor. Load is read from the network the way the stress gauge reads it.

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

## `ARCH-DEC-004` — trades are data, the roll is code

The trade files and the tag entries are datapack files the mod ships; the only code in the trade
path is one loot function type, `metered_motor:roll`, whose parameters (tier, rpm band, capacity
band, efficiency band) are in the trade file. A pack author retunes numbers without Java; the
burn formula stays in the block entity.

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
