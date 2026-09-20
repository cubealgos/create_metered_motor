---
title: "create_metered_motor spec — glossary"
type: "spec"
category: "create_metered_motor"
---

# 03 — Glossary

| Term | Means |
|---|---|
| **metered motor** | This mod's block and item: a kinetic source that burns emeralds in proportion to the stress its network draws. |
| **tier** | I, II or III: which toolsmith level sells it and which fixed capacity it carries, pinned to a real steam-engine set-up (`decisions/DEC-009-fixed-tiers.md`). The only field on the component; never changes. |
| **rpm** | The rotation speed the motor generates while running, Create's unit: fixed at 64 for every tier, the same active speed a steam engine reaches (`DEC-009`). Not stored; derived from being a metered motor. |
| **stress capacity** | The stress units (SU) the motor adds to its network while running; fixed per tier — 16,384 / 65,536 / 294,912 for I/II/III (`DEC-009`), not rolled. Create wants this expressed *per rpm*, so `calculateAddedStressCapacity()` returns the tier's capacity divided by 64 (`domains/motor.md` `MOTOR-REQ-004`). |
| **efficiency** | *Withdrawn* (`DEC-009`, 2026-09-20). There was a rolled 0.75–1.25 "luck" factor beyond a motor's tier; fixed tiers removed it — every motor of a tier is now identical. Kept in this glossary only so older text that names it is legible as withdrawn. |
| **rate** | Emeralds per minute at full load: stress capacity / 92,160. Constant per tier (0.18 / 0.71 / 3.2 a minute for I/II/III), not per-item (`DEC-009`). |
| **load** | The network's current stress divided by its capacity, 0 to 1, as Create's stress gauge shows it. |
| **meter** | The motor's fractional emerald counter; when it reaches one, an emerald is taken. |
| **running / stopped / paused** | Running: has emeralds and no redstone signal, generates. Stopped: no emeralds. Paused: a redstone signal is present. |
| **offer** | A villager's trade instance; the tier component is fixed on the trade's `gives` item template, nothing is rolled when the offer is created (`DEC-009`). |
| **trade** | The data file that describes an offer; the tag that lists it for a toolsmith level. |
| **network** | Create's kinetic network the motor is a source of. |
| **goggles** | Create's engineer's goggles; their overlay shows the motor's meter. |
