---
title: "create_metered_motor spec — release engineering and distribution"
type: "spec"
category: "create_metered_motor"
---

# Release engineering, distribution and support (`REL`, sheet §7)

| Item | Position |
|---|---|
| Version scheme | `<mod>+<mc>`, SemVer on the mod part over `contracts/public-surface.md`: `1.0.0+26.2` |
| Branches | gitkontor's: `development`, `production`; releases are tags on `production` |
| Channels | Modrinth only; CurseForge deferred, as `create_brass_compass` |
| CI | `just check` on every merge: lint, unit tests, game tests, by the Woodpecker file, live from the first push since the repo is public on Forgejo from the bootstrap (`https://git.cubealgos.de/cubealgos/create_metered_motor`, GitHub mirror `https://github.com/cubealgos/create_metered_motor`, which carries the public issue tracker) |
| Always a playable build | `just client` boots with Create Fly at every merge |
| Support | Issue tracker only; no SLA; a `SUPPORT.md` says so |
| Ports | A new Minecraft version is a new `+<mc>` build from a port branch; the component version does not change with the game version |

`REL-REQ-001`: every release jar is built by `just release` from a clean checkout at a tag.
`REL-REQ-002`: the release notes list the component version and the Create Fly version tested.
`REL-REQ-003`: the release notes state the tier table in force (`domains/trade.md` §3).
