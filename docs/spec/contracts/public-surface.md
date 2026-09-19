---
title: "create_metered_motor spec — public surface: what a datapack, resource pack or add-on may rely on"
type: "spec"
category: "create_metered_motor"
---

# Public surface (`SURFACE`)

| Surface | Stable from | What it is |
|---|---|---|
| Block and item id `metered_motor:metered_motor` | 1.0 | The block and its block item |
| Component `metered_motor:stats` | 1.0, versioned | `contracts/data-contract.md` |
| Loot function `metered_motor:roll` and its parameters `tier`, `rpm` band, `capacity` band, `efficiency` band (each `{min, max}`) | 1.0 | Overridable by a datapack trade file (`ARCH-DEC-004`) |
| Trade files `metered_motor:toolsmith/3/emerald_metered_motor_i`, `metered_motor:toolsmith/4/emerald_metered_motor_ii`, `metered_motor:toolsmith/5/emerald_metered_motor_iii` | 1.0 | Overridable by datapack |
| The tag entries appending these trades to `data/minecraft/tags/villager_trade/toolsmith/level_<n>.json` | 1.0 | Overridable by datapack |
| Block model and textures | 1.0 | Replaceable by resource pack |
| Translation keys `block.metered_motor.metered_motor`, `screen.metered_motor.*`, `tooltip.metered_motor.*`, `goggles.metered_motor.*` | 1.0 | Language packs |

Not public: the menu type, the screen classes, the block entity's internals, the burn divisor
(`MOTOR-DEC-003`), the merchant predicate's internals. Versioned by SemVer over the surface above (`operations/release.md`).

`SURFACE-REQ-001`: a change to a stable surface is a major version.
