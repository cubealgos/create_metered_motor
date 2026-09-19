---
schema_version: 1
id: 01M2W33BZQ76VMFRNWQ1TGDT4B
key: MM-1
type: chore
title: "Bootstrap: Gradle with Loom and Create Fly, entrypoints, licence, notice, routing, tools, spec copy, smoke game test"
created_by: kevin
created_at: 2026-09-19T05:44:19Z
---

## Scope

The repository as `docs/spec/04-architecture.md` `ARCH-DEC-001` and `docs/spec/decisions/DEC-004-toolchain.md` describe it: one Gradle project on Loom 1.17 with Create Fly `26.2-rc-2-6.0.9-1` pinned (`fabric.mod.json` declaring `6.0.9-1`, `contracts/platform-matrix.md`) and Fabric API 0.160.0, Java 25, Gradle 9.5.1 wrapper, Kotlin DSL and version catalog copied from `create_brass_compass`: `build.gradle.kts`, `settings.gradle.kts`, `gradle/`, `gradlew`, `gradle.properties`, `justfile`, `tools/` (doctor, map, release notes and their tests), `.ci/`, `.woodpecker.yml`, `.gitea/default_merge_message/*`, `CLAUDE.md` routing adapted to this repo, `.gitignore`. MIT `LICENSE` (`decisions/DEC-003-licence.md`), `NOTICE` crediting Create Fly (CC0), Create (MIT) and Fabric (Apache-2.0) (`operations/compliance.md`), `README.md`, `SUPPORT.md`, `CHANGELOG.md` skeleton, `docs/spec/` as a copy of the vault spec via `just spec-sync` pointed at the cubealgos heimathafen sibling. Main and client entrypoints for mod id `metered_motor`; `verifyPurePackage` gating the `metered_motor.model` package; a smoke game test proving the mod loads beside Create Fly; `SourceSurfaceTest` asserting no networking type is referenced by the mod (`COMP-REQ-001`), established here so later tickets extend it for their own translation-key coverage.

## Approach

Copy the verified toolchain from `create_brass_compass` (wrapper, Loom plugin id, Modrinth repository with `exclusiveContent`, the `justfile` recipes, `tools/doctor.py`, `tools/map.py`, `tools/release_notes.py` and their tests) into one Gradle project for `create_metered_motor`; adapt `CLAUDE.md`'s routing table to this repo's domains (motor, trade, ui). Point `just spec-sync`'s vault path at `../heimathafen/vault/projects/create_metered_motor/spec` (the cubealgos sibling, per `contracts/platform-matrix.md`'s doctor check). First thing to verify: `just spec-sync` produces a byte-identical `docs/spec/` against the vault source read for this ticket.

## Acceptance criteria

- [x] `just check` is green, including a smoke game test (`SmokeGameTest`) proving the mod loads on a dedicated server with Create Fly present (reviewer's own run 2026-09-19: all 2 required tests passed; Woodpecker pipeline 2 on the branch: success).
- [x] `just doctor` is clean after the merge: toolchain floors met, `docs/spec/` identical to the vault, merge templates on the default branch.
- [x] `fabric.mod.json`'s `contact` block links the Forgejo repo, the GitHub issues tracker and the Modrinth slug `metered-motor` (reviewed).
- [x] `verifyPurePackage` runs against `metered_motor.model` and passes; `just client` is Kevin's check at the first screen ticket, as with the compass.
- [x] `SourceSurfaceTest.noNetworkingTypeIsReferencedByTheMod` passes (`COMP-REQ-001`).

## Constraints and prior findings

Review 2026-09-19: the CI installer had to grow apt-installs for curl, git and python3 and fetch just as a tarball (the pipeline image lacks all four tools and bash); found through three Woodpecker runs on the compass and folded into this commit.

`docs/spec/contracts/platform-matrix.md` (`PLATFORM-REQ-001`), `docs/spec/decisions/DEC-004-toolchain.md`, `docs/spec/decisions/DEC-003-licence.md`, `docs/spec/operations/compliance.md` (`COMP-REQ-001`). Create Fly coordinate, mod id `create`, declared version `6.0.9-1`, CC0, and the 26.2 toolchain floors (Java 25, Gradle 9.5.1, Loom 1.17, Loader 0.19.5, Fabric API 0.160.0) are verified against the vault (spec `README.md` Verifications #1, #2). `create_brass_compass`'s BC-1 is the model for this ticket's shape and its `just doctor`/`just check` acceptance bar; its own `SourceSurfaceTest` (network scan and later, translation-key scan) is the direct template.
