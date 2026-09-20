---
title: "create_metered_motor DEC-009 — Fixed tiers as steam-engine set-ups, all at 64 rpm, divisor 92,160"
type: "spec"
category: "create_metered_motor"
---

# `DEC-009` — Fixed tiers as steam-engine set-ups, all at 64 rpm, divisor 92,160

**Status:** decided by Kevin, 2026-09-20, on
`vault/technical/minecraft/create-fly-steam-engines-26-2.md`.

The rolled rpm/capacity/efficiency design (`DEC-006`, `TRADE-DEC-002`, `MOTOR-DEC-003`) is
withdrawn in favour of three fixed tiers. Kevin's rulings, verbatim:

1. "when we have to get into this anyway let's make it not random: each tier should be as
   powerful as a specific fitting steam engine + consume appropriate emeralds."
2. "also make them all output the same rpm as a steam engine does; a user there needs to change
   speed manually too."
3. "fixed rpm also makes multi motor setups easier which fits the QoL vibe better." Claude's note,
   kept here: two Create generators at different speeds on one shaft conflict; identical rpm lets
   motors stack like engines do, and their capacities add.

## The ladder

Kevin picked the ladder from the research note's boiler table (`§A`), which reads the heat-level
math straight out of the 26.2 jar:

| Tier | Real set-up | rpm | Capacity (SU) | Price (emeralds) | Rate at full load (SU / 92,160) | Per Minecraft day (×20 min) |
|---|---|---:|---:|---:|---:|---:|
| I | 1 engine, level-1 boiler | 64 | 16,384 | 24 | 0.18/min | 11 |
| II | 4 engines, level-4 boiler | 64 | 65,536 | 40 | 0.71/min | 43 |
| III | 18 engines, level-18 boiler (full) | 64 | 294,912 | 64 | 3.2/min | 64 |

All three tiers run at 64 rpm — the steam engine's own top ("active") speed — because ruling 2
ties the motor to what a steam engine does, and a player who wants it slower turns it down
manually as they would an engine. The top tier's rate lands exactly on the pre-existing
one-stack-a-day ceiling (`MOTOR-REQ-005`'s prior wording), and because the divisor is uniform
across tiers, cost per SU is identical at every tier — a player is not punished or rewarded by
tier for the price of power, only for the up-front emerald cost and the toolsmith level needed.

## Why 92,160 and not 8,192

`MOTOR-DEC-003`'s original divisor (8,192) was set from an estimate of the full-boiler case that
predates this bytecode read. The research note's bytecode read of `BoilerData`/`PoweredShaftBlockEntity`
gives the full level-18, 18-engine boiler as 294,912 SU, not the ~24,576 SU the estimate assumed —
off by a factor of exactly 12. Solving `294912 / D × 20 = 64` (the one-stack-a-day bound) for `D`
gives `D = 92,160`, which is the divisor this decision sets uniformly across all three tiers
(`MOTOR-DEC-003`, amended; `domains/motor.md` `MOTOR-REQ-005`).

**Alternative considered:** keep 8,192 and shrink tier III's capacity to a smaller real boiler
(e.g. 8 engines on a level-8 boiler) instead of raising the divisor. Rejected: ruling 1 asks for
each tier to be "a specific fitting steam engine" at recognisable set-up sizes (one engine, a
quarter of the max, the full boiler), and the research note's own open question left this exact
choice to Kevin; the ladder above is that choice, made once, uniformly.

## The bug this rework fixes

The same research note found that `KineticNetwork` sums a generator's contribution as
`calculateAddedStressCapacity() × |getGeneratedSpeed()|` — Create's stress capacity is **per rpm**,
not a total. A block entity that returns a tier's total capacity (e.g. 294,912) from
`calculateAddedStressCapacity()` while generating 64 rpm would hand the network
`294912 × 64 = 18,874,368` SU, not 294,912; the research note observed this exact shape of bug in
a client check ("the goggles showed '1,488,384 su'" for a mis-scaled roll — 153× the intended
capacity, the same class of error). The correct return is the tier's capacity **divided by 64**
(`domains/motor.md` `MOTOR-REQ-004`, citing the research note's §B bytecode). The same check found
the goggles overlay showing capacity twice — once on Create's own "Generator Stats" line (which
reads the same `calculateAddedStressCapacity()`), once on this mod's row — so the mod's overlay
now drops its capacity row when Create's line is present (`domains/motor.md` `MOTOR-REQ-012`,
`domains/ui.md` `UI-REQ-005`).

## Consequences

- `DEC-006`'s component (`tier`, `rpm`, `capacity`, `efficiency`) is superseded: the component now
  holds only `tier`; rpm, capacity and rate are derived from it in code
  (`contracts/data-contract.md` version 2).
- `TRADE-REQ-002` (the `metered_motor:roll` loot function) is withdrawn: a trade's `gives` item
  template carries the tier directly as a component, nothing is rolled at offer creation
  (`domains/trade.md`).
- `efficiency` is withdrawn everywhere it appeared as a stat, a band or a readout row
  (`03-glossary.md`, `domains/motor.md`, `domains/ui.md`).
- The toolsmith still sells three tiers at three levels (`DEC-005` stands); what changes is that
  every copy of a given tier is identical, so "breeding a better roll" is no longer the loop —
  levelling a toolsmith to Expert or Master to *unlock* a tier still is.

**Cost if wrong:** the ladder and the divisor are both in code; a rebalance is a release, not a
datapack (`ARCH-DEC-004`), same as `MOTOR-DEC-003` already was. Prices stay in the trade files and
remain datapack-retunable (`TRADE-REQ-004`).
