---
title: "create_metered_motor DEC-007 — Metered: emeralds burn in proportion to the network's load, read once a second"
type: "spec"
category: "create_metered_motor"
---

# `DEC-007` — Metered: emeralds burn in proportion to the network's load, read once a second

**Status:** decided by Kevin, 2026-09-19.

The motor's burn follows Create's network stress over capacity, read once a second as the stress
gauge reads it, and added to a fractional meter at `rate / 60 × min(1, stress / capacity)`
(`MOTOR-REQ-006`, `MOTOR-DEC-001`). An idle network — nothing drawing stress — costs nothing
(`MOTOR-FAIL-002`); a network at full load burns at the motor's full rate, fixed at
`capacity / 8192 / efficiency` emeralds per minute (`MOTOR-REQ-005`, `MOTOR-DEC-003`).

Alternative considered: a flat burn whenever the motor is turning, regardless of load, the way
a blaze burner burns fuel by time. Rejected: it would make an idling network as expensive as a
working one, defeating the point of a *metered* motor and giving a player no reason to build
efficiently. Cost if wrong: the load ratio is the network's, not the motor's. Two metered motors
on one network each burn their own rate times the same ratio, which splits the bill by capacity
and is fair; but a metered motor beside a free water wheel pays the ratio for load the wheel
covers too. Bounded by keeping metered motors on their own networks, which the goggles readout
makes visible (`MOTOR-DEC-001`).
