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

- [x] The readout's first line is an indented header built the way Create's own goggle headers are, so the goggles icon covers no text; the rows are label-and-value in Create's styles; no "Idle".
- [x] A game test (or unit test if the line builder is pure) asserts the line keys and order, the header first, for a running and a stopped motor.
- [ ] `just client`: goggles on a stopped and on a running motor (Kevin's check).
- [x] Merged through a Forgejo pull request into `development`.

## Constraints and prior findings

MM-7's Findings: `BlockEntityBehaviour.addClient` + `GeneratingKineticTooltipBehaviour`; `MotorTooltipBehaviour` calls `super` first. MM-18 (running in parallel) owns the screen and `StatsText`; add to `StatsText` only if unavoidable and say so.

## Findings

**The builders, read from the Create Fly jar with `javap -p -c` (and `-v` for the `translate`
concat recipe):** `com.zurrtum.create.client.catnip.lang.LangBuilder` wraps one accumulating
`MutableComponent`. `text(String)`/`add(Component)`/`add(MutableComponent)` append; `add(LangBuilder)`
appends the other builder's *whole* accumulated component as one sibling (so a piece must be fully
built and styled before it is folded into a larger builder — Create relies on this for its own
two-part rows); `style(ChatFormatting)`/`color(...)` restyle everything accumulated *so far*, not
what is added later; `translate(String, Object...)` resolves `Component.translatable(namespace +
"." + key, args)` — the `"."` confirmed from the invokedynamic's `StringConcatFactory` recipe
(`.`) — so it only works for a caller whose own keys want that namespace prefix
(`CreateLang.builder()`/`CreateLang.translate(...)` hard-code namespace `"create"`); since this
mod's own `goggles.metered_motor.*` keys are already full, unprefixed literals (matching every
other tooltip in this codebase), the rewrite uses `new LangBuilder("metered_motor")` and
`.add(Component.translatable(key, args))` instead of `.translate(...)`, sidestepping the prefix
entirely rather than fighting it. `forGoggles(List<? super MutableComponent>)` is exactly
`forGoggles(list, 0)`; `forGoggles(list, int indent)` builds a *new* `LangBuilder` whose text is
`Strings.repeat(' ', getIndents(font, 4 + indent))` (`font` from `Minecraft.getInstance().font`,
so this call is client-only and cannot run headless), adds `this` builder's component after that
padding, and adds the result to the tooltip list — a terminal, non-chainable call. `getIndents`
measures a literal space's pixel width against `4 * DEFAULT_SPACE_WIDTH` scaled by `4 + indent`,
i.e. indent 0 and 1 differ by one space-equivalent, not a fixed 4/5-space jump.

**The header and row patterns, read from `GeneratingKineticTooltipBehaviour.addToGoggleTooltip`,
`KineticTooltipBehaviour.addToGoggleTooltip`/`addStressImpactStats`, and
`StressGaugeTooltipBehaviour.addToGoggleTooltip`:** a header (`gui.goggles.generator_stats`,
`gui.goggles.kinetic_stats`, `gui.stressometer.title`) is `CreateLang.translate(key)` straight
into `forGoggles(tooltip)` — **no `.style()` call at all**, so Create's own headers render in the
default (white) colour, not grey; both `GeneratingKineticTooltipBehaviour` and
`KineticTooltipBehaviour` return `false` from `addToGoggleTooltip` before adding anything when the
relevant magnitude (`calculateAddedStressCapacity()` / `calculateStressApplied()`) is exactly
zero — confirming the ticket's read that Create adds no kinetic lines on a stopped source, which
is why the icon covered our own first line before this fix. Create's own rows are **two lines per
stat**, not one: a label line at indent 0 (`tooltip.capacityProvided`, `tooltip.stressImpact`,
`gui.stressometer.capacity`, each `CreateLang.translate(key).style(GRAY).forGoggles(tooltip)`),
then a value line at indent 1 combining a `CreateLang.number(d)` (default colour, *no* style)
immediately concatenated with the unit's own translated string (`generic.unit.stress` = `"su"`,
no leading space — Create's real goggle text is genuinely `"1024su"`, not `"1024 su"`), *then*
`.style(AQUA)` applied to that merged number+unit, then an unstyled `.space()`, then a
`DARK_GRAY`-styled annotation builder (`"at current speed"` / `" / 1024su"`) folded in with
`.add(...)`, the whole thing through `forGoggles(tooltip, 1)`. `StressGaugeBlockEntity`'s overlay
(`StressGaugeTooltipBehaviour`) is the percentage example: same shape, the value line's colour
comes from `IRotate$StressImpact.getRelativeColor()` (green/yellow/red by load) rather than a
fixed `AQUA`.

**Deliberate deviation from Create's own two-line-per-stat shape:** the ticket's own line list
(header, then Tier/Speed/Capacity/Efficiency/Rate/Load/Inside/Remaining/State) already specifies
one `Label: value unit` line per row, not Create's label-line-then-value-line pair — nine stats at
two lines each would run twice as long as any of Create's own two- or three-line blocks. The
rewrite keeps Create's *colours* to the letter (label `GRAY`, value `AQUA`, a dim annotation
`DARK_GRAY` — here the unit rather than an "at current speed" aside) and Create's *indentation*
mechanism (header through `forGoggles(tooltip)`, unstyled; every row through
`forGoggles(tooltip, 1)`) but collapses label and value onto one row each, both because the ticket
asked for it directly and because it is the intended reading here (the file-turned-multi-line
reading of nine 2-line stats would very likely run off a 1080p client at the goggles overlay's own
font scale — not verified against a running client in this ticket, since goggles are Kevin's
`just client` check per the acceptance criteria, but worth noting for that check).

**Testability, and why the split:** `LangBuilder.forGoggles` needs a live `Minecraft.getInstance()
.font`, so nothing that calls it can run in a plain JVM unit test or the headless game-test server
(`docs/spec/operations/testing.md`). `MotorTooltipBehaviour.rows(Stats, MotorState, double, int,
double)` is a new package-private static method — pure Java, no Create or Minecraft client
classes, returning a package-private `record Row(String labelKey, String value, boolean
valueIsKey, String unitKey, int indent)` — that reproduces exactly the *content and order* Create's
own builders will render (header first, nine rows after, each row's label/unit key and formatted
value) without touching `LangBuilder`; `addRow(List<Component>, Row)` is the thin, untested
remainder that turns one `Row` into the real styled, indented `Component` through
`LangBuilder`/`forGoggles`. `MotorTooltipBehaviourTest`
(`src/test/java/metered_motor/client/visual/MotorTooltipBehaviourTest.java`) asserts the header
is first and at indent 0, the nine row keys appear in the ticket's declared order all at indent 1,
and the "no load" vs. formatted-countdown branch of the Remaining row picks correctly for a
stopped motor, a running motor at zero load, and a running motor with a positive countdown
(`4m 12s` at 252 seconds, matching `StatsText.remaining`'s existing rounding).

**`StatsText` untouched:** every value already had a formatter (`tier`, `twoDecimals`,
`wholePercent`, `state`, `remaining`); no new one was needed, so MM-18's ownership of `StatsText`
was not touched, per this ticket's constraint.

**`en_us.json`:** the old flat `goggles.metered_motor.{tier,rpm,capacity,efficiency,rate,load,
emeralds,remaining,idle}` keys (each a single `%s`-templated line) are replaced by
`goggles.metered_motor.title`, nine `goggles.metered_motor.row.*` label keys (each ending in a
literal colon, e.g. `"Tier:"`), six `goggles.metered_motor.unit.*` keys (`rpm`, `su`, `rate`,
`emeralds` each carrying their own leading space so the space is part of the localized unit text
rather than a code-level decision; `efficiency` = `"x"` and `percent` = `"%"` carry none, matching
the tight suffix the original screenshot already used) and `goggles.metered_motor.no_load` =
`"no load"`; `goggles.metered_motor.idle` is gone (the ticket's "no Idle" acceptance criterion).
The three `goggles.metered_motor.state.*` keys are unchanged. `screen.metered_motor.*` (MM-18's)
was not touched.

**Check results:** `./gradlew compileJava compileTestJava` clean; `./gradlew test` — all 5 tests
in `MotorTooltipBehaviourTest` and `SourceSurfaceTest` (both the networking check and the
translation-key check, confirming every new key resolves) green; `just map` regenerated
`docs/map.md` and `docs/map/root/metered_motor.client.visual.md` (the new `rows`/`Row` surface,
listed as `(test)` since both are package-private, used only by the ticket's own test). `just
check` (lint, map-check, test, test-tools, gametest) green end to end: gametest reports "All 43
required tests passed :)" (no game test added or needed here — the line assembly is the pure unit
test above, per the ticket's "or unit test if the line builder is pure").

**Not done, left to Kevin's `just client` check per the ticket:** the live goggles render (font
metrics, actual pixel indentation clearing the icon, and whether nine single-line rows plus
Create's own kinetic lines fit the tooltip's on-screen height at goggles scale) — none of that is
game-testable headless.
