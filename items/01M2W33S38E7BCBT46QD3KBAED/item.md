---
schema_version: 1
id: 01M2W33S38E7BCBT46QD3KBAED
key: MM-10
type: chore
title: "Release preparation and cut: 1.0.0+26.2"
created_by: kevin
created_at: 2026-09-19T05:44:32Z
---

## Scope

Release preparation and the cut for version 1.0.0+26.2 (`operations/release.md`): the version in one place, `CHANGELOG.md`, `just release` building from a clean checkout at a tag with a checksum in the notes (`REL-REQ-001`), the release notes stating the component version and the Create Fly version tested (`REL-REQ-002`) and the tier table in force (`REL-REQ-003`, `docs/spec/domains/trade.md` §3). Modrinth listing assets — an icon on the Create blueprint badge via `tools/icon.py` as the compass, the body text, a gallery shot list. A recorded security pass (dependency audit, secrets scan, new surface, supply-chain diff) with a verdict and Kevin's approval. The cut itself: a release branch, a production merge through a Forgejo pull request, the tag, and the dist handed to Kevin for the manual Modrinth upload.

## Approach

Follow `create_brass_compass`'s BC-8/BC-9/BC-11/BC-12 split: release preparation (version, changelog, `just release`, listing text) lands on `development` first; the cut (branch, security pass, tag, `just release` at the tag, production merge, dist handed over) happens on its own release branch per `workflows/gitkontor/repo-workflow/07-releases.md`. `tools/icon.py` reuses the compass's palette sampling (outer band, ring, blueprint disc, white grid) against this mod's own sprite.

## Acceptance criteria

- [ ] Version `1.0.0+26.2` in one place, `CHANGELOG.md` complete, `just release` from a clean checkout at tag `v1.0.0+26.2` writes `dist/` with a reproducible SHA-256 across two runs (`REL-REQ-001`).
- [ ] Release notes state the component version and the tested Create Fly version (`REL-REQ-002`) and the tier table in force (`REL-REQ-003`).
- [ ] `docs/modrinth/icon.png` (the Create blueprint badge via `tools/icon.py`, `just icon`), `docs/modrinth/body.md` and `docs/modrinth/gallery.md` (shot list) exist.
- [ ] A security pass is recorded in this ticket with a verdict, and Kevin's approval of that verdict.
- [ ] Merged into `production` through a Forgejo pull request, tagged, `dist/` handed to Kevin for the Modrinth upload.

## Constraints and prior findings

`REL-REQ-001..003`, `operations/compliance.md`. Modelled directly on `create_brass_compass`'s BC-8 (release preparation), BC-9 (the cut, including its recorded security-pass table shape), BC-11 (listing assets) and BC-12 (the shared Create add-on icon theme — sixteen Modrinth icons compared, a blueprint-blue disc with a white grid, a sampled palette). `kontor release prepare` was broken in gitkontor 1.0.1 for the compass (`ModuleNotFoundError: No module named 'release'`); checksums came from `just release` with `shasum` instead — check whether this is still the case before relying on `kontor release prepare`. Blocked by MM-9 (the test sweep and client checklist this release certifies).
