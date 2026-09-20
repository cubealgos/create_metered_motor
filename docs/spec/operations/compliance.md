---
title: "create_metered_motor spec — compliance, security and governance"
type: "spec"
category: "create_metered_motor"
---

# Compliance, security and governance (`COMP`, sheet §6)

A distributed product carries the same obligations, landing in different places
(`workflows/session/start-new-project.md`).

| Area | Position |
|---|---|
| GDPR: what leaves the user's machine | Nothing. No telemetry, no update check, no outbound network call of any kind (`COMP-REQ-001`). No player names in the mod's data; the block entity stores emerald counts, a meter fraction and its fixed tier stats only. |
| Hosted parts we run | None. Modrinth hosts the file and its page. |
| Impressumspflicht | Attaches to a public web presence; there is none beyond the platform pages. Revisit if a site exists. |
| Licence and notices | MIT (`decisions/DEC-003-licence.md`); `NOTICE` credits Create Fly (CC0), Create (MIT), Fabric (Apache-2.0). Minecraft's emerald textures are not copied; the mod uses vanilla's items directly. |
| Supply chain and release integrity | Builds from a tagged commit with pinned dependencies; the release checksum is in the release notes; no signing at 1.0. |
| Vulnerability disclosure | The public issue tracker only, on the GitHub mirror (`https://github.com/cubealgos/create_metered_motor/issues`); no private channel, no e-mail address published. Forgejo stays the source of truth for code. |
| Server trust boundary | The client never sends stats or emerald counts; every change goes through vanilla container menus and the block entity's sync. Offer creation embeds the fixed tier server-side, from the trade file's `gives` template; nothing is rolled (`decisions/DEC-009-fixed-tiers.md`). |
| AI Act, GoBD, sector regulation | Not applicable: no AI component, no financial records, no regulated sector. |

`COMP-REQ-001`: the mod shall make no network call of its own; a source-scan test asserts it
(`SourceSurfaceTest`, as `create_brass_compass`).

Open: release signing (minisign) before 1.0 or after.
