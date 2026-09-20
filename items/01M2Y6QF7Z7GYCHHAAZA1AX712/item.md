---
schema_version: 1
id: 01M2Y6QF7Z7GYCHHAAZA1AX712
key: MM-16
type: bug
title: The placed motor's shaft half renders crooked instead of along the facing axis
created_by: kevin
created_at: 2026-09-20T01:26:12Z
---

## Scope

The placed motor's shaft half renders crooked (Kevin, 2026-09-20, client check, screenshot in the session scratchpad `mm16-crooked-shaft.png`): a brass motor facing the player shows the grey shaft half tilted at an angle inside the front face, not along the facing axis as Create's creative motor draws it. The shaft shall sit on the facing axis, centred, and turn about that axis, exactly as the creative motor beside it (`MOTOR-REQ-002`, `MOTOR-REQ-013`).

## Approach

Compare byte for byte with Create Fly: `javap -p -c` on `CreativeMotorRenderer` (the fallback renderer: `KineticBlockEntityRenderer.renderSafe` and how it picks the rendered block state / rotation for the shaft: `getRenderedBlockState`, `shaft(...)`, `rotateToFace`, `KineticBlockEntityRenderer.standardKineticRotationTransform`) and the visual `AllBlockEntityRenders`' `OrientedRotatingVisual.of(AllPartialModels.SHAFT_HALF)` (what `Direction`/rotation it derives from `facing`, whether it uses `getRotationAxis` from the block and a `rotateToFace(Direction)` on the partial). Read MM-7's `MeteredMotorRenderer` and `MotorVisuals` against those. Suspects: the fallback renderer rotating the shaft with the block's facing as a rotation axis instead of orienting the partial to the face; the visual passing `Direction.UP`-relative rotation where the creative motor passes the facing; both paths wrong for the horizontal facings only. Decide which path the client was on (Flywheel is bundled with Create Fly, so the visual path is the live one unless Flywheel is off) and fix both.

## Acceptance criteria

- [ ] `MeteredMotorRenderer` and `MotorVisuals` orient the shaft half the way Create's creative motor does for all six facings; the difference to Create's code is only the block entity type.
- [ ] A unit test on the orientation helper (the pure part: facing → rotation) if one can be separated; otherwise the ticket records why not.
- [ ] `just client`: the shaft sits on the facing axis and turns while running, for a horizontal and a vertical facing (Kevin's check).
- [ ] Merged through a Forgejo pull request into `development`.

## Constraints and prior findings

MM-7's Findings on `OrientedRotatingVisual` and `AllBlockEntityRenders.register()`; the block model itself contains no shaft geometry.

## Findings

**No code divergence found, after an exhaustive byte-for-byte comparison; no source change was made.**
Both suspects the Approach names are ruled out, and every other layer between a placed block's
`FACING` value and the drawn shaft was checked and matches Create Fly's own creative motor
exactly (source `javap -p -c` on both this mod's *compiled* classes and the Create Fly jar's, not
just prose comparison):

- **`MeteredMotorRenderer.extractRenderState`** (fallback, Flywheel-less path): compiled
  bytecode is logic-identical to `CreativeMotorRenderer.extractRenderState` — same
  `CachedBuffers.partialFacing(SHAFT_HALF, blockState, facing)` call (facing passed straight,
  never `.getOpposite()`), same `KineticBlockEntityRenderer.getRotateAngleWithoutBeOffset(Axis,
  KineticBlockEntity, BlockEntityRenderState, LevelAccessor)` overload for the spin. The only
  textual difference is reading `blockEntity.getBlockState()` instead of the inaccessible
  `state.blockState` field (documented in MM-7, unavoidable at this mod's compile classpath,
  and equivalent in value since `extractBase` populates `state.blockState` synchronously,
  same thread, same tick, immediately before).
- **`MotorVisuals.register()`**: byte-identical to `AllBlockEntityRenders`'s private `visual(type,
  rendererFactory, visualizerFactory, skipVanillaRender)` helper (confirmed by disassembling
  that helper too, not just its call site for `AllBlockEntityTypes.MOTOR`): `BlockEntityRenderers
  .register(type, rendererFactory)` then `SimpleBlockEntityVisualizer.builder(type)
  .factory(visualizerFactory).skipVanillaRender(pred).apply()`. Create's own registration for
  the creative motor calls `OrientedRotatingVisual.of(AllPartialModels.SHAFT_HALF)` — the exact
  static factory our code calls — with `skipVanillaRender` always `true`, matching `MotorVisuals`
  exactly. There is no `Direction.UP`-relative rotation anywhere in this path (the ticket's
  suspicion): `UP` only appears in `OrientedRotatingVisual`'s unrelated `gantryShaft` factory.
- **`OrientedRotatingVisual.of(...)`'s generated factory** (`lambda$of$0`, disassembled): reads
  `blockEntity.getBlockState().getValue(BlockStateProperties.FACING)` and constructs
  `new OrientedRotatingVisual(ctx, be, partialTick, Direction.SOUTH, facing, chunkPartial)`,
  whose constructor calls `rotatingModel.rotateToFace(SOUTH, facing).setup(blockEntity)` — this
  is Create's own class and method, called unmodified; there is no MM-specific code on this path
  at all beyond passing our own `BLOCK_ENTITY_TYPE`.
- **`RotatingInstance.setup`/`KineticBlockEntityVisual.rotationAxis`**: the per-frame spin reads
  the axis generically via `IRotate.getRotationAxis(state)` — `MeteredMotorBlock.getRotationAxis`
  and `CreativeMotorBlock.getRotationAxis` are byte-identical (`state.getValue(FACING).getAxis()`),
  and `DirectionalKineticBlock.FACING` (both blocks' shared superclass) is `BlockStateProperties
  .FACING` itself (confirmed from the class's static initializer), so there's no property-identity
  mismatch either.
- **Blockstate JSON**: `metered_motor.json`'s per-facing `x`/`y` values are the same as
  `creative_motor.json`'s, facing for facing.
- **Block model JSON**: `block_ii.json`'s `elements` (geometry/rotation) diff byte-equal against
  Create's `block.json` once the `textures` key (expected to differ, retargeted namespace) is
  excluded — the four "corner strut" elements MM-7 found aren't shaft geometry are not
  contributing anything crooked.
- **The rotation math itself**: wrote a standalone JOML 1.10.8 program reproducing
  `RotatingInstance.rotateToFace`'s `Quaternionf().rotateTo((0,0,1), facing.step())` for all six
  `Direction` values, including the antiparallel case (`SOUTH`→`NORTH`, the one case a naive
  "rotate shortest arc" implementation could degenerate on). All six produced clean, valid unit
  quaternions (no NaN, no near-identity substitute); `SOUTH`→`NORTH` cleanly resolves to a
  180° rotation about `Y`. No math bug at any facing.

`just check` is green on this unmodified worktree (`./gradlew check -x test -x runGameTest`
clean; `just map` writes no diff; **"All 37 required tests passed :)"**), so this isn't a build
regression hiding the symptom either.

**Why no unit test was added**: there is no separable MM-specific "facing → rotation" pure
helper to extract and test. The only facing-dependent logic on our side is
`MeteredMotorBlock.getRotationAxis`, already the minimal one-line `state.getValue(FACING)
.getAxis()` — vanilla `Direction.getAxis()` itself is the "pure helper," and it's already
identical to `CreativeMotorBlock`'s override. Everything downstream of that (`CachedBuffers
.partialFacing`, `OrientedRotatingVisual`, `RotatingInstance`) is Create Fly's own code, not
ours to unit-test in isolation, and (per MM-7's own finding and `docs/spec/operations/testing.md`)
this project's `test-java` suite doesn't bootstrap Minecraft's block/state registries the way
`runGameTest` does — there's no existing precedent here for a plain-JUnit test that constructs a
real `BlockState`.

**Conclusion and open question for Kevin**: given the source, compiled bytecode, assets and the
underlying rotation math are all a verified match to Create Fly's own creative motor, the crooked
shaft in `mm16-crooked-shaft.png` cannot be explained from this mod's code as it stands in this
branch (`bugfix/mm-16-shaft-orientation`, off `development` @ `4573473`, which already carries
MM-7's implementation unchanged). Recorded rather than guessed at (per the workflow's "a design
question the spec doesn't answer is asked, never decided inline" and "bound fix/review loops and
record an explicit ruling"): possible next steps are (1) re-take the screenshot against a freshly
built client from this exact commit, in case the original capture predates MM-7 landing or used a
stale resource pack cache, or (2) check whether Create Fly's own creative motor shows the same
crookedness at the same facing in the same world — if it does, this is an upstream Create Fly
6.0.9-1 issue this mod cannot "fix" while staying byte-identical to it, which is what this ticket's
own acceptance criteria ask for. No commit was pushed for this ticket since no code change was
warranted; this Findings entry is the deliverable.
