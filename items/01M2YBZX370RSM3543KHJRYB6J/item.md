---
schema_version: 1
id: 01M2YBZX370RSM3543KHJRYB6J
key: MM-19
type: bug
title: The goggles readout is unstructured and Create's goggles icon hides its first line
created_by: kevin
created_at: 2026-09-20T02:58:11Z
---

## Scope

The goggles readout is an unstructured list and Create's goggles icon covers its first line (Kevin, 2026-09-20, screenshot `mm19-goggles.png` in the session scratchpad: "Tier I" hidden under the icon, then eight plain lines, "Idle" above "Stopped"). Create's own goggle text starts with an indented header line so the icon never covers it, then label-and-value rows in Create's colours (label grey, value coloured, unit dim). The motor's readout shall follow that shape exactly (`MOTOR-REQ-012`, `UI-REQ-005`, `UI-REQ-007`): a header "Metered Motor" (or "Meter") built with Create's `forGoggles` indentation, then rows Tier, Speed, Capacity, Efficiency, Rate at full load, Load, Emeralds inside, Remaining ("no load" or the time), State, each `Label: value unit` styled as Create styles its kinetic stats; appended after Create's own kinetic lines when those exist, and standing on its own when the motor is stopped (Create adds no kinetic lines at zero speed, which is why the icon hit our first line).

## Approach

Read from the Create Fly jar how Create Fly builds goggle lines: `com.zurrtum.create.client.foundation.utility.CreateLang` (or the catnip `LangBuilder`: `translate(...)`, `text`, `style(ChatFormatting)`, `add`, `forGoggles(List<Component>)`, `forGoggles(list, indents)`), the header pattern in `GeneratingKineticTooltipBehaviour.addToGoggleTooltip` (`gui.goggles.generator_stats`, `gui.goggles.kinetic_stats`, `tooltip.capacityProvided`, `tooltip.speedRequirement` and their colour styles) and `StressGaugeBlockEntity`'s overlay. Then rewrite `MotorTooltipBehaviour`'s lines with the same builders and keys `goggles.metered_motor.*`. Numbers through `StatsText`.

## Acceptance criteria

- [ ] The readout's first line is an indented header built the way Create's own goggle headers are, so the goggles icon covers no text; the rows are label-and-value in Create's styles; no "Idle".
- [ ] A game test (or unit test if the line builder is pure) asserts the line keys and order, the header first, for a running and a stopped motor.
- [ ] `just client`: goggles on a stopped and on a running motor (Kevin's check).
- [ ] Merged through a Forgejo pull request into `development`.

## Constraints and prior findings

MM-7's Findings: `BlockEntityBehaviour.addClient` + `GeneratingKineticTooltipBehaviour`; `MotorTooltipBehaviour` calls `super` first. MM-18 (running in parallel) owns the screen and `StatsText`; add to `StatsText` only if unavoidable and say so.
