---
title: "create_metered_motor spec — glossary"
type: "spec"
category: "create_metered_motor"
---

# 03 — Glossary

| Term | Means |
|---|---|
| **metered motor** | This mod's block and item: a kinetic source that burns emeralds in proportion to the stress its network draws. |
| **tier** | I, II or III: which toolsmith level sells it and which bands its stats are rolled from. On the component; never changes. |
| **rpm** | The rotation speed the motor generates while running, Create's unit; rolled once. |
| **stress capacity** | The stress units (SU) the motor adds to its network while running; rolled once. |
| **efficiency** | A rolled factor from 0.75 to 1.25 that divides the burn; the "luck" of a motor beyond its size. |
| **rate** | Emeralds per minute at full load: stress capacity / 8,192 / efficiency. Fixed once rolled; at most about three a minute. |
| **load** | The network's current stress divided by its capacity, 0 to 1, as Create's stress gauge shows it. |
| **meter** | The motor's fractional emerald counter; when it reaches one, an emerald is taken. |
| **running / stopped / paused** | Running: has emeralds and no redstone signal, generates. Stopped: no emeralds. Paused: a redstone signal is present. |
| **offer** | A villager's trade instance; the roll happens when the offer is created. |
| **trade** | The data file that describes an offer; the tag that lists it for a toolsmith level. |
| **roll** | The loot function that writes tier, rpm, stress capacity and efficiency onto the offered item. |
| **network** | Create's kinetic network the motor is a source of. |
| **goggles** | Create's engineer's goggles; their overlay shows the motor's meter. |
