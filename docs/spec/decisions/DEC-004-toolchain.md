---
title: "create_metered_motor DEC-004 — Java 25, Gradle 9.5.1, Loom 1.17, Kotlin DSL, version catalog, one Gradle project"
type: "spec"
category: "create_metered_motor"
---

# `DEC-004` — Java 25, Gradle 9.5.1, Loom 1.17, Kotlin DSL, version catalog, one Gradle project

**Status:** decided by Kevin, 2026-09-19.

The same toolchain as `create_brass_compass` and `create_civilization`, verified in the vault's
Minecraft notes (`README.md` Verification 2): Java 25, Gradle 9.5.1, Loom 1.17, Kotlin DSL with a
version catalog. One Gradle project, because — as with the compass — there is no sim layer to keep
pure; `verifyPurePackage` (`operations/testing.md`) replaces the project split that
`create_civilization`'s `core`/`harness` boundary needs, checking the stats codec and roll
arithmetic stay free of Minecraft imports without a second module.

Alternative considered: splitting a pure module from the Minecraft-facing one, as `create_civilization`
does. Rejected: the pure surface here is small (a codec and some arithmetic), and a package check
gives the same guarantee at a fraction of the build complexity. Cost if wrong: the pure package
grows large enough that mixing it with Minecraft-facing code becomes awkward, in which case a
module split becomes a ticket, not a rewrite.
