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
| `ACTORS-003` | **Toolsmith villager** | Offer a motor at Journeyman, Expert or Master level when the game draws its trades; restock it | Offer it at other levels; offer a tier other than the one its trade file fixes |
| `ACTORS-004` | **Server** | Create the offer from the trade's fixed `gives` template (nothing rolled, `decisions/DEC-009-fixed-tiers.md`); own the block's inventory and meter; read the network's load; stop and start the rotation; refuse any client request that does not come through a menu it opened | Trust a client with stats or emerald counts |
| `ACTORS-005` | **Create Fly** (dependency) | Supply the kinetic base classes, the stress network, the goggles, the GUI framework, the logistics that insert into containers | Be replaced by upstream Create: the mod targets Create Fly's ids and classes |
| `ACTORS-006` | **Datapack or resource pack author** | Change the trades (prices, levels, which tag they join), textures and models | Change a tier's fixed rpm, capacity or rate, or the burn formula or the component format (`DEC-009`) |
| `ACTORS-007` | **Server operator** | Install and remove the mod; nothing to configure | Read or edit motor stats from the outside except through the world save |
| `ACTORS-008` | **Contributor** | Build, test and change the mod under MIT | Add telemetry or network calls (`operations/compliance.md`) |

## Findings from writing this

- **`FINDING-1`** There is no owner. Stats belong to the item and then to the block; emeralds belong
  to whoever reaches the block. Protection is a server's business (claims mods), not this mod's.
- **`FINDING-2`** *(Amended, `decisions/DEC-009-fixed-tiers.md`, 2026-09-20.)* Originally: the
  villager was an actor with a random hand, rolling stats when the offer was created; a different
  villager or the next level yielded a new roll, driving a breeding loop. That is withdrawn: every
  offer of a tier now carries the same fixed stats, whoever offers it and whenever it is created.
  The only reason left to level a toolsmith is to reach the level that unlocks a higher tier.
- **`FINDING-3`** *(Amended, `DEC-009`.)* Originally: the datapack author got the tier bands as
  data, because a roll function read its ranges from the trade file. That function is withdrawn;
  a datapack author now retunes only price, level and tag membership through the trade file — a
  tier's rpm, capacity and rate are fixed in code. The burn formula stays code either way, so a
  pack cannot make a motor free by accident.
- **`FINDING-4`** Create's logistics see the motor as a container, so an arm or a funnel can feed it
  emeralds; the same path must not let anything take emeralds out, or a funnel would drain it.
