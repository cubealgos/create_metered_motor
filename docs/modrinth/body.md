# Modrinth listing (paste-ready)

## Project settings

| Field | Value |
|---|---|
| Name | Create: Metered Motor |
| Slug | `metered-motor` |
| Summary | A kinetic source that costs money to run: buy a fixed tier from a toolsmith and feed it emeralds. |
| Categories | Equipment, Technology, Utility (secondary: Adventure, Game-Mechanics, Management, Optimization) |
| Licence | MIT |
| Client side | Required |
| Server side | Required |
| Loaders | Fabric |
| Game versions | 26.2 |
| Dependencies | Create Fly (required), Fabric API (required) |
| Icon | `icon.png` in this folder: the mod's own tier II brass motor block model on the cubealgos navy badge (`just icon` regenerates it) |
| Links | Source `https://github.com/cubealgos/create_metered_motor` · Issues `https://github.com/cubealgos/create_metered_motor/issues` · Origin `https://git.cubealgos.de/cubealgos/create_metered_motor` |

## Version settings

| Field | Value |
|---|---|
| Version number | `1.0.0+26.2` |
| Version title | Metered Motor 1.0.0 for Minecraft 26.2 |
| Channel | Release |
| File | `dist/create_metered_motor-1.0.0+26.2.jar` |
| Changelog | paste `dist/release-notes-1.0.0+26.2.md` |

## Body

Create's power sources are free once built. A metered motor is not: it comes from a toolsmith, not
a crafting table, and it burns emeralds in proportion to the work its network actually draws.

### What it does

- **Bought, not crafted.** A toolsmith at Journeyman, Expert or Master offers a fixed tier: every
  motor of a tier is identical, pinned to a real steam-engine set-up — tier I matches a small
  one-engine set-up, tier II a mid four-engine set-up, tier III a full boiler. A toolsmith never
  offers more than one at a time; a higher tier means levelling another villager.
- **Placed like Create's motor.** Face the shaft where you want it, right-click to place. It turns
  the network as any generator does, at 64 rpm — the steam engine's own active speed — and its
  tier's fixed stress capacity. Motors stack like engines: several at the same rpm add their
  capacities together on one shaft.
- **Burns emeralds only under load.** The rate is fixed per tier and follows the network's stress
  against its capacity, read the way Create's own gauge reads it: an idle network costs nothing,
  and a network at half its capacity burns at half the motor's rate.
- **Hopper-fed, insert-only.** Five slots take emeralds and emerald blocks from any face, from
  hoppers, players and Create's logistics; nothing can be pulled back out except by a player in the
  screen. Empty stops it; a redstone signal pauses it either way.
- **Read at a glance.** The item's tooltip and Create's goggles overlay both show tier, 64 rpm,
  stress capacity and rate; the overlay adds current load, emeralds inside and the remaining time
  at that load, and omits its own capacity row where Create's own "Generator Stats" line already
  shows the same number. The block's tinted band marks its tier on the model.

### The tiers

Fixed, not rolled: every motor of a tier is identical, all three at the steam engine's own 64 rpm.

| Tier | Toolsmith level | Price (emeralds) | Stress capacity (SU) | Rate at full load | Real set-up |
|---|---|---|---|---|---|
| I | Journeyman (3) | 24 | 16,384 | 0.18/min (11/day) | 1 engine, level-1 boiler |
| II | Expert (4) | 40 | 65,536 | 0.71/min (43/day) | 4 engines, level-4 boiler |
| III | Master (5) | 64 | 294,912 | 3.2/min (64/day) | 18 engines, level-18 boiler (full) |

Rate follows directly from capacity — cost per SU is identical across tiers, so a tier only changes
the up-front price and the toolsmith level needed, not the running cost per unit of power. Tier III
at full load lands exactly on the one-stack-a-day ceiling.

### Made for Create

The screen is built from Create Fly's own frames, textures and slot art, so it looks like the rest
of your workshop. It adds one block, its item, and nothing else: no new mechanics to learn.

### Privacy

Nothing leaves your machine. No telemetry, no update checks, no network calls of its own. The block
entity stores its emerald count, its meter and its tier; nothing else.

### Requirements

Minecraft 26.2, Fabric, Fabric API, and Create Fly 6.0.9-1 (the build this version was tested with;
the mod declares exactly that version).

### Support

Through the issue tracker only (https://github.com/cubealgos/create_metered_motor/issues), as time
allows. Source on GitHub, mirrored from the cubealgos Forgejo. Include your Minecraft, Fabric and
Create Fly versions, the mod version from the jar name, and the steps that show the problem. MIT
licensed.
