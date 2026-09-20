---
title: "create_metered_motor DEC-001 — Distributed product, full spec sheet"
type: "spec"
category: "create_metered_motor"
---

# `DEC-001` — Distributed product, full spec sheet

**Status:** decided by Kevin, 2026-09-19.

This is the second of the small add-ons built on the way to `create_civilization`, and like
`create_brass_compass` before it, it ships to real users on Modrinth, not only into the
civilization mod. Kevin follows the same process: the full sheet, not a compact one, so the
release path, the compliance table and the testing layers are decided once, up front, rather than
retrofitted when a player files an issue.

Alternative considered: treating it as internal tooling and skipping §5–§7 (interface contracts,
compliance, release engineering) since `create_civilization` is the real goal. Rejected for the
same reason as the compass: a motor with tiered stats and a real balance ladder invites bug
reports and balance requests from people who never touch the civilization mod. Cost if wrong: an
evening of spec for a one-block, one-screen mod.
