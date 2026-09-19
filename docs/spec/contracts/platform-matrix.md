---
title: "create_metered_motor spec — platform matrix"
type: "spec"
category: "create_metered_motor"
---

# Platform matrix (`PLATFORM`)

| Row | Value | How it is checked |
|---|---|---|
| Minecraft | 26.2 (`~26.2` in `fabric.mod.json`) | `just doctor`, game tests on a dedicated server |
| Fabric Loader | ≥ 0.19.5 | `fabric.mod.json` |
| Fabric API | ≥ 0.160.0 (the command API for the development command, the game-test entrypoint; block entity sync, registries and loot functions are vanilla's) | `fabric.mod.json` |
| Create Fly | `26.2-rc-2-6.0.9-1`, mod id `create`, declared version `6.0.9-1`, `implementation` coordinate `maven.modrinth:create-fly`, pinned | `fabric.mod.json` depends `create`; `NOTICE` |
| Java | 25 | `just doctor` |
| Gradle / Loom | 9.5.1 wrapper / 1.17 | wrapper properties |
| Operating systems | macOS, Linux, Windows: the JVM's | not tested separately; nothing native |
| Client and server | Both; the screen and goggles overlay client-side, the roll, the meter and the network state server-side | game tests (server), `just client` (client) |
| Villager trades | 26.2's data-driven trade registry: `data/minecraft/villager_trade/`, `data/minecraft/trade_set/`, tags under `data/minecraft/tags/villager_trade/` | game test |

`PLATFORM-REQ-001`: **If** any row moves, **then** `just doctor` fails naming the row.
