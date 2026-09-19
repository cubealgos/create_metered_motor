---
title: "create_metered_motor spec — actors: who touches the motor and what each may do"
type: "spec"
category: "create_metered_motor"
---

# 01 — Actors

| ID | Actor | May | May not |
|---|---|---|---|
| `ACTORS-001` | **Player (buyer, owner in practice)** | Buy a motor from a toolsmith; place it; feed it emeralds by hand, hopper or Create logistics; open its screen; take emeralds back out; break it and keep its stats | Change its stats; make it run without emeralds; take emeralds out by automation |
| `ACTORS-002` | **Another player** | Everything the owner may, on any motor they can reach: there is no ownership. Steal the emeralds by breaking the block or through the screen | Nothing distinct; the block knows no owner |
| `ACTORS-003` | **Toolsmith villager** | Offer a motor at Journeyman, Expert or Master level when the game draws its trades; restock it | Offer it at other levels; change a rolled offer's stats |
| `ACTORS-004` | **Server** | Roll the stats when an offer is created; own the block's inventory and meter; read the network's load; stop and start the rotation; refuse any client request that does not come through a menu it opened | Trust a client with stats or emerald counts |
| `ACTORS-005` | **Create Fly** (dependency) | Supply the kinetic base classes, the stress network, the goggles, the GUI framework, the logistics that insert into containers | Be replaced by upstream Create: the mod targets Create Fly's ids and classes |
| `ACTORS-006` | **Datapack or resource pack author** | Change the trades (prices, levels, which tag they join), the tier bands the roll function reads from the trade file, textures and models | Change the burn formula or the component format |
| `ACTORS-007` | **Server operator** | Install and remove the mod; nothing to configure | Read or edit motor stats from the outside except through the world save |
| `ACTORS-008` | **Contributor** | Build, test and change the mod under MIT | Add telemetry or network calls (`operations/compliance.md`) |

## Findings from writing this

- **`FINDING-1`** There is no owner. Stats belong to the item and then to the block; emeralds belong
  to whoever reaches the block. Protection is a server's business (claims mods), not this mod's.
- **`FINDING-2`** The villager is an actor with a random hand: the roll happens when the *offer* is
  created, which is when the villager levels up or restocks, not when the player buys. Two buys of
  the same offer yield the same stats; a different villager, or the same one after the next level,
  yields a new roll. That is the breeding loop.
- **`FINDING-3`** The datapack author gets the tier bands as data, because the roll function reads
  its ranges from the trade file that invokes it. The burn formula stays code so a pack cannot make
  a motor free by accident.
- **`FINDING-4`** Create's logistics see the motor as a container, so an arm or a funnel can feed it
  emeralds; the same path must not let anything take emeralds out, or a funnel would drain it.
