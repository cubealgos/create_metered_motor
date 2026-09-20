---
title: "create_metered_motor DEC-005 — No recipe; the toolsmith sells it at Journeyman, Expert and Master, stats rolled per offer"
type: "spec"
category: "create_metered_motor"
---

# `DEC-005` — No recipe; the toolsmith sells it at Journeyman, Expert and Master, stats rolled per offer

**Status:** decided by Kevin, 2026-09-19.

The metered motor has no crafting recipe at 1.0. It is sold by the toolsmith villager at three
levels — Journeyman (tier I), Expert (tier II), Master (tier III) — with a trade file per tier
appended to the matching vanilla `toolsmith/level_<n>` tag (`domains/trade.md` §3,
`TRADE-REQ-001`). Each offer's rpm, stress capacity and efficiency are rolled by the
`metered_motor:roll` loot function when the offer is created, not when it is bought
(`TRADE-DEC-003`), so buying twice from one offer yields twins but a new villager or the next
level yields a new roll — the breeding loop `01-actors.md` `FINDING-2` describes. A villager
never holds more than one motor offer (`TRADE-DEC-004`).

Alternative considered: a crafting recipe, either instead of or alongside the trade. Rejected: a
recipe would let a player mass-produce motors, undercutting the reason to level toolsmiths to
Expert and Master, which is the point of the design (`00-context.md`, "Why this exists"). Cost if
wrong: a server without villagers — a villager-free world — has no way to get a motor at all; a
datapack can add a recipe, since the trade stays data-driven either way (`ARCH-DEC-004`).

**Amended (Kevin, 2026-09-20, `decisions/DEC-009-fixed-tiers.md`):** rpm, stress capacity and
efficiency are no longer rolled by a `metered_motor:roll` loot function — that function is
withdrawn (`TRADE-REQ-002`). Each tier is now a fixed set of stats, embedded directly in the
trade's `gives` item template. Buying the same offer twice, or a different offer of the same tier,
always yields twins; the "breeding loop" `01-actors.md` `FINDING-2` describes no longer applies —
what a player still breeds and levels toolsmiths for is reaching the Expert and Master levels that
unlock tiers II and III, not for a better roll within a tier.
