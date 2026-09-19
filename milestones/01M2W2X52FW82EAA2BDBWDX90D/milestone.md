---
schema_version: 1
id: 01M2W2X52FW82EAA2BDBWDX90D
key: M0
title: Foundation
status: todo
created_at: 2026-09-19T05:40:55Z
---

## Goal

Everything the repository needs before feature work begins: a working Gradle/Loom build against Create Fly, licensing, routing, tooling and CI, and a proof the mod loads at all.

## Scope

In: the Gradle project, main and client entrypoints, `docs/spec/` synced from the vault, `LICENSE`/`NOTICE`, `CLAUDE.md` routing, `justfile` and `tools/`, CI wiring, a smoke game test. Out: any block, trade or screen code — those start at M1.

## Exit criteria

- `just check` is green, including the smoke game test.
- `just doctor` is clean: toolchain floors, spec copy, merge templates.
- `fabric.mod.json`'s contact block names the Forgejo repo, the GitHub issues tracker and the Modrinth slug.

## Tickets

- MM-1 — Bootstrap: Gradle with Loom and Create Fly, entrypoints, licence, notice, routing, tools, spec copy, smoke game test

## Depends on

Nothing.
