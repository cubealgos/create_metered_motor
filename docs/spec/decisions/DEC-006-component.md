---
title: "create_metered_motor DEC-006 — One block and one item; tier, rpm, capacity and efficiency on a component that travels between item and block"
type: "spec"
category: "create_metered_motor"
---

# `DEC-006` — One block and one item; tier, rpm, capacity and efficiency on a component that travels between item and block

**Status:** decided by Kevin, 2026-09-19.

One block id and one item id, `metered_motor:metered_motor`, for all three tiers. Tier, rpm,
stress capacity and efficiency live on a single versioned component, `metered_motor:stats`
(`contracts/data-contract.md`), which the roll writes onto the item and which moves to the block
entity on placement and back on breaking via `applyImplicitComponents`/`collectImplicitComponents`
(`ARCH-DEC-005`, `DATA-REQ-004`). The tier only changes which trade sold it and which bands it was
rolled from; it does not change the block or item id.

Alternative considered: three items and three blocks, one pair per tier. Rejected: it would triple
the registry entries, the models, the textures and the loot-function wiring for no behavioural
gain, since every tier already varies continuously within its rolled bands — a tier boundary on
the id would be a second, redundant tier axis alongside the component's own `tier` field. Cost if
wrong: three items and blocks would mean three times the assets to draw, model and maintain, for a
distinction the component already carries.
