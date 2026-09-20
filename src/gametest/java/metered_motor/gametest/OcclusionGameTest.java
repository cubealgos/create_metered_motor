package metered_motor.gametest;

import com.zurrtum.create.AllBlocks;
import metered_motor.block.MotorBlocks;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * MM-16: neighbours of a placed motor were wrongly culling their touching face — the block had
 * no {@code getShape} override, so it fell back to {@code Block}'s full-cube default, and
 * Minecraft precomputes a block's per-facing *occlusion* shape from that same shape absent a
 * dynamic one. A neighbour then believed the motor's casing fully covered its face and skipped
 * rendering it, even though the rendered casing (copied byte-for-byte from Create's own
 * {@code block.json}) never filled that face — the gap showed whatever was beyond the neighbour
 * (MOTOR-REQ-002, MOTOR-REQ-013). Asserts our block's shape and occlusion behaviour match the
 * creative motor's exactly, for a horizontal and a vertical facing.
 */
public final class OcclusionGameTest {
    @GameTest
    public void theCasingShapeAndOcclusionMatchTheCreativeMotorHorizontally(GameTestHelper helper) {
        assertMatchesCreativeMotor(helper, Direction.NORTH);
    }

    @GameTest
    public void theCasingShapeAndOcclusionMatchTheCreativeMotorVertically(GameTestHelper helper) {
        assertMatchesCreativeMotor(helper, Direction.UP);
    }

    private static void assertMatchesCreativeMotor(GameTestHelper helper, Direction facing) {
        BlockPos pos = new BlockPos(1, 1, 1);
        BlockState ours = MotorBlocks.BLOCK.defaultBlockState().setValue(BlockStateProperties.FACING, facing);
        BlockState creative = AllBlocks.CREATIVE_MOTOR.defaultBlockState().setValue(BlockStateProperties.FACING, facing);
        helper.setBlock(pos, ours);

        helper.assertTrue(
            ours.canOcclude() == creative.canOcclude(),
            "canOcclude() matches the creative motor's (facing " + facing + "): ours=" + ours.canOcclude() + " creative=" + creative.canOcclude());

        VoxelShape oursShape = ours.getShape(helper.getLevel(), pos, CollisionContext.empty());
        VoxelShape creativeShape = creative.getShape(helper.getLevel(), pos, CollisionContext.empty());
        helper.assertTrue(
            oursShape == creativeShape,
            "the casing shape is Create's own AllShapes.MOTOR_BLOCK.get(facing), not a per-block copy (facing " + facing + ")");
        helper.assertTrue(
            !oursShape.bounds().equals(Shapes.block().bounds()),
            "the shape isn't a full cube — that was the bug (facing " + facing + "): " + oursShape.bounds());

        helper.succeed();
    }
}
