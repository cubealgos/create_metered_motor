# Modrinth listing (paste-ready)

## Project settings

| Field | Value |
|---|---|
| Name | Create Fly: Metered Motor |
| Slug | `metered-motor` |
| Summary | A kinetic source that costs money to run: buy one rolled from a toolsmith and feed it emeralds. |
| Categories | Utility, Technology (secondary: Equipment) |
| Licence | MIT |
| Client side | Required |
| Server side | Required |
| Loaders | Fabric |
| Game versions | 26.2 |
| Dependencies | Create Fly (required), Fabric API (required) |
| Icon | `icon.png` in this folder: the motor on the round blueprint badge Create add-ons share (`just icon` regenerates it) |
| Links | Source `https://git.cubealgos.de/cubealgos/create_metered_motor` · Issues `https://github.com/cubealgos/create_metered_motor/issues` · Mirror `https://github.com/cubealgos/create_metered_motor` |

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

- **Bought, not crafted.** A toolsmith at Journeyman, Expert or Master offers one, rolled the moment
  the offer is created: rpm, stress capacity and efficiency, each drawn independently within its
  tier's band. A toolsmith never offers more than one at a time; a better roll means breeding or
  levelling another villager.
- **Placed like Create's motor.** Face the shaft where you want it, right-click to place. It turns
  the network as any generator does, at its rolled rpm and stress capacity.
- **Burns emeralds only under load.** The rate is fixed for the block's life and follows the
  network's stress against its capacity, read the way Create's own gauge reads it: an idle network
  costs nothing, and a network at half its capacity burns at half the motor's rate.
- **Hopper-fed, insert-only.** Five slots take emeralds and emerald blocks from any face, from
  hoppers, players and Create's logistics; nothing can be pulled back out except by a player in the
  screen. Empty stops it; a redstone signal pauses it either way.
- **Read at a glance.** The item's tooltip and Create's goggles overlay both show tier, rpm, stress
  capacity, efficiency and rate; the overlay adds current load, emeralds inside and the remaining
  time at that load. The block's tinted band marks its tier on the model.

### The tiers

| Tier | Toolsmith level | Price (emeralds) | rpm band | Stress capacity band (SU) | Efficiency band |
|---|---|---|---|---|---|
| I | Journeyman (3) | 24 | 16 to 64 | 512 to 2,048 | 0.75 to 1.25 |
| II | Expert (4) | 40 | 32 to 128 | 2,048 to 8,192 | 0.75 to 1.25 |
| III | Master (5) | 64 | 64 to 256 | 8,192 to 18,432 | 0.75 to 1.25 |

Rate at full load follows from capacity and efficiency: tier I burns one emerald every 3 to 20
minutes, tier II one every 45 seconds to 5 minutes, tier III 0.8 to 3 a minute, at most one stack a
Minecraft day at the luckiest roll.

### Made for Create

The screen is built from Create Fly's own frames, textures and slot art, so it looks like the rest
of your workshop. It adds one block, its item, and nothing else: no new mechanics to learn.

### Privacy

Nothing leaves your machine. No telemetry, no update checks, no network calls of its own. The block
entity stores its emerald count, its meter and its rolled stats; nothing else.

### Requirements

Minecraft 26.2, Fabric, Fabric API, and Create Fly 6.0.9-1 (the build this version was tested with;
the mod declares exactly that version).

### Support

Through the issue tracker only (https://github.com/cubealgos/create_metered_motor/issues), as time
allows. Source on Forgejo, mirrored to GitHub. Include your Minecraft, Fabric and Create Fly
versions, the mod version from the jar name, and the steps that show the problem. MIT licensed.
