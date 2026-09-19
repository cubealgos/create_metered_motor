# create_metered_motor

A Create Fly add-on for Minecraft 26.2 on Fabric: a metered motor, a kinetic power source bought
from a toolsmith villager, whose rpm, stress capacity and efficiency are rolled per offer and
which burns emeralds in proportion to the stress its network draws.

**This file routes. It does not hold content.** The specification is `docs/spec/`.

## Read this before you do that

| about to… | read first |
|---|---|
| anything at all | `docs/spec/README.md`, then the one domain file you need |
| find where something lives | `docs/map.md`; generated, never edited |
| touch `metered_motor.model` | it has no Minecraft imports; the build's `verifyPurePackage` enforces it |
| touch the motor block or block entity | `docs/spec/domains/motor.md`, `docs/spec/04-architecture.md` `ARCH-DEC-002` |
| touch the villager trade or the roll | `docs/spec/domains/trade.md`, `docs/spec/04-architecture.md` `ARCH-DEC-004` |
| add a screen | `docs/spec/domains/ui.md` |
| touch the component | `docs/spec/contracts/data-contract.md`: versioned, forward-only migrations |
| add a dependency | `docs/spec/decisions/DEC-003-licence.md` (MIT) and heimathafen's dependency policy |
| commit | scope `metered_motor`, the ticket key (`MM-N`) in the subject |

## Working here

```
kontor claim MM-N
kontor branch new MM-N <slug>
just check
```

`just --list` shows the task surface; `just spec-sync` refreshes `docs/spec/` from the vault; `just map` regenerates the map.

## Standing rules

- The spec is authoritative; `docs/spec/` is a copy of heimathafen's vault.
- A design question the spec does not answer is asked, never decided inline.
- Nothing leaves the player's machine: no telemetry, no network calls (`docs/spec/operations/compliance.md`).
- Always keep a playable build: `just client` boots with Create Fly at every merge.
