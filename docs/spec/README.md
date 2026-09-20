---
title: "create_metered_motor spec — index"
type: "spec"
category: "create_metered_motor"
repo: "create_metered_motor"
---

# Create Fly: Metered Motor — specification

A small Create Fly add-on for Minecraft 26.2 on Fabric: a **metered motor**, a kinetic power source
bought from a toolsmith villager — three motors matching a small, a mid and a full steam set-up,
fixed rather than rolled (`decisions/DEC-009-fixed-tiers.md`) — which burns emeralds in proportion
to the stress its network actually draws. One block, one item, three tiers on a component, one
screen, no recipe. The second of the
small add-ons built to gain the experience `create_civilization` needs (its ENERGY domain assumes
exactly such a motor); it is nonetheless a distributed product with real users and is specified as
one (`decisions/DEC-001-classification.md`).

This spec is the distributed-product spec sheet in the chunked format. The sheet's sections map to
files as follows; a section marked *out of scope* says why in the file that would have held it.

| Sheet section | File |
|---|---|
| §1 Document control | this file: identifiers, state, decisions |
| §2 Executive summary and business context | `00-context.md` |
| §3 Product architecture and runtime topology | `04-architecture.md` |
| §4 Domain-driven functional specifications | `01-actors.md`, `02-journeys.md`, `03-glossary.md`, `domains/motor.md`, `domains/trade.md`, `domains/ui.md` |
| §5 Interface contracts and integration | `contracts/platform-matrix.md`, `contracts/public-surface.md`, `contracts/data-contract.md` |
| §6 Compliance, security and governance | `operations/compliance.md` |
| §7 Release engineering, distribution and support | `operations/release.md`, `operations/testing.md` |
| §8 Migration, compatibility and out of scope | `00-context.md` §What it will not do, `contracts/data-contract.md` |
| Appendix: technical blueprints | `04-architecture.md` §Shape, `contracts/data-contract.md` |

## Files and state

| File | Domain prefix | State |
|---|---|---|
| `00-context.md` | — | written |
| `01-actors.md` | `ACTORS` | written |
| `02-journeys.md` | `UC` | written |
| `03-glossary.md` | — | written |
| `04-architecture.md` | `ARCH` | written |
| `domains/motor.md` | `MOTOR` | written |
| `domains/trade.md` | `TRADE` | written |
| `domains/ui.md` | `UI` | written |
| `contracts/platform-matrix.md` | `PLATFORM` | written |
| `contracts/public-surface.md` | `SURFACE` | written |
| `contracts/data-contract.md` | `DATA` | written |
| `operations/compliance.md` | `COMP` | written |
| `operations/release.md` | `REL` | written |
| `operations/testing.md` | `TEST` | written |

## Identifiers

`<DOMAIN>-<KIND>-<NNN>`: `MOTOR-REQ-004`, `TRADE-UC-001`, `MOTOR-FAIL-001`, `ARCH-DEC-001`.
Permanent; a withdrawn item keeps its number.

## Verifications

| # | Claim | Verified | Where |
|---|---|---|---|
| 1 | Create Fly 26.2 coordinate, mod id `create`, declared version `6.0.9-1`, CC0 | 2026-09-19 | `vault/technical/minecraft/create-fly-26-2.md` (personal layer) |
| 2 | 26.2 toolchain: Java 25, Gradle 9.5.1, Loom 1.17, Loader 0.19.5, Fabric API 0.160.0 | 2026-09-18 | `vault/technical/minecraft/fabric-26-2-toolchain.md` |
| 3 | Create Fly's creative motor: `content.kinetics.motor.CreativeMotorBlock extends DirectionalKineticBlock implements IBE`, `CreativeMotorBlockEntity extends GeneratingKineticBlockEntity`; `KineticBlockEntity` exposes `getGeneratedSpeed()`, `calculateAddedStressCapacity()`, `getOrCreateNetwork()`, `hasNetwork()`; `KineticNetwork` holds `currentStress` and `currentCapacity` (read through the stress gauge's `getNetworkStress()`/`getNetworkCapacity()` pattern) | 2026-09-19 | the Create Fly jar, `javap` |
| 4 | Create Fly reaches block inventories through vanilla `Container`: `AllTransfer.getInventory(level, pos, state, be, side)`; funnels, arms and chutes go through `InvManipulationBehaviour.getInventory()`. A `WorldlyContainer` serves vanilla hoppers and Create's logistics alike | 2026-09-19 | the Create Fly jar |
| 5 | 26.2 villager trades are data: `data/<ns>/villager_trade/<path>.json` (`wants`, optional `additional_wants`, `gives` as an item template, `given_item_modifiers` as loot functions, `max_uses`, `reputation_discount`, `xp`); a level's set `data/minecraft/trade_set/toolsmith/level_4.json` draws `amount: 2` trades from the tag `#minecraft:toolsmith/level_4`, which a datapack extends with `replace: false` | 2026-09-19 | the 26.2 jar, `data/minecraft/villager_trade/toolsmith/**`, `trade_set`, `tags/villager_trade` |
| 6 | Loot functions are registered in `BuiltInRegistries.LOOT_FUNCTION_TYPE` as `MapCodec`s; `LootItemConditionalFunction.run(ItemStack, LootContext)` is the hook; the context carries the random source | 2026-09-19 | the 26.2 jar |
| 7 | Block entities move components to and from their item through `applyImplicitComponents(DataComponentGetter)` and `collectImplicitComponents(DataComponentMap.Builder)`; `WorldlyContainer` gates faces with `getSlotsForFace` and `canPlaceItemThroughFace` | 2026-09-19 | the 26.2 jar |
| 8 | Client visuals: `SingleAxisRotatingVisual.of(PartialModel)` and `SimpleBlockEntityVisualizer.builder(type)` from Create Fly's bundled Flywheel; the creative motor uses `CreativeMotorRenderer`; goggles read `client.api.goggles.IHaveGoggleInformation.addToGoggleTooltip` | 2026-09-19 | the Create Fly jar |
| 9 | Create Fly's screen framework (`MenuType` in `CreateRegistries.MENU_TYPE`, `MenuBase`, `AbstractSimiContainerScreen`, `AllGuiTextures`, tinted glyph blits) | 2026-09-19 | `create_brass_compass`, in production |
| 10 | Goggles text comes from a client-side `BlockEntityBehaviour`: the overlay reads `client.foundation.blockEntity.behaviour.tooltip.TooltipBehaviour.TYPE` from the hovered block entity (Create's own kinetic block entities implement no client interface); how an add-on attaches one to its block entity type is read at the first ticket. Create Fly's `BlockStressValues.register` sets generator speeds only; capacities are config (`CStress`); the largest boiler's figure, previously taken as an estimate, is now confirmed at 294,912 SU (Verification 11) | 2026-09-19, partly | the Create Fly jar; `MOTOR-REQ-012`, `DEC-009` |
| 11 | Create Fly's boiler heat-level ladder (`BoilerData.getEngineEfficiency`, `getMaxHeatLevelForBoilerSize`), the steam engine's capacity-per-rpm semantics (`PoweredShaftBlockEntity.calculateAddedStressCapacity()` returns `combinedCapacity / speedModifier`, which cancels against `getGeneratedSpeed()` in `KineticNetwork`'s product, confirmed by full bytecode disassembly), and the reference generator capacities (`CStress`: steam engine 1,024, creative motor 16,384) | 2026-09-20 | `vault/technical/minecraft/create-fly-steam-engines-26-2.md`, `javap` |

## Divergences from heimathafen standards

| Standard | Divergence | Recorded in |
|---|---|---|
| `default-license-apache-2-cla` | MIT, no CLA | `decisions/DEC-003-licence.md` |
| `naming-theme` | Descriptive English under the Create add-on convention | `decisions/DEC-002-name.md` |
| "no remote unless justified later" | Public on Forgejo under `cubealgos` from the bootstrap, mirrored to GitHub with the issue tracker there, as `create_brass_compass` ended up | `decisions/DEC-003-licence.md` |

## Decisions

| ID | Decision | State |
|---|---|---|
| `DEC-001` | Distributed product, full spec sheet | written |
| `DEC-002` | `create_metered_motor`, mod id `metered_motor`, "Create Fly: Metered Motor" | written |
| `DEC-003` | MIT; public under the cubealgos organisation from the first commit | written |
| `DEC-004` | Toolchain as `create_brass_compass`: Java 25, Gradle 9.5.1, Loom 1.17, Kotlin DSL, one project | written |
| `DEC-005` | No recipe: the toolsmith sells it, three tiers at Journeyman, Expert and Master, never more than one motor offer per villager | written |
| `DEC-006` | One block and one item; the tier travels between item and block on a component (amended by `DEC-009`: the component now carries only the tier) | written |
| `DEC-007` | Metered: emeralds burn in proportion to the stress the network draws | written |
| `DEC-008` | Edges: empty stops it, a redstone signal pauses it, five slots for emeralds and emerald blocks, automation inserts only | written |
| `DEC-009` | Fixed tiers as real steam-engine set-ups (16,384 / 65,536 / 294,912 SU), all at 64 rpm, divisor 92,160; withdraws rolled rpm/capacity/efficiency | written |
| `MOTOR-DEC-004` | The model is Create's creative motor, recoloured per tier (in `domains/motor.md`) | written |

## Open questions gathered

| Question | Where | Blocks |
|---|---|---|
| Whether the merchant predicate sees the villager's offers, which the one-motor-per-villager rule needs | `domains/trade.md` §7 | the trade ticket's approach, not the design |
| Whether 26.2's villager-trade `gives` item template accepts a `components` block for the fixed tier stats | `domains/trade.md` §7, `TRADE-REQ-002` | the ticket, marked "to verify at the ticket" there |

Closed: the full steam engine's stress figure in Create Fly, to pin tier III's ceiling — resolved
by Verification 11 and fixed as the ladder in `decisions/DEC-009-fixed-tiers.md`.
