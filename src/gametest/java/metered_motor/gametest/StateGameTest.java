package metered_motor.gametest;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.base.RotatedPillarKineticBlock;
import metered_motor.block.MeteredMotorBlock;
import metered_motor.block.MeteredMotorBlockEntity;
import metered_motor.block.MotorBlocks;
import metered_motor.block.MotorState;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/**
 * MM-3/MM-4: the state machine driven from redstone and the inventory (MM-4's fuel gate) turns
 * Create's network on and off (MOTOR-REQ-004, MOTOR-REQ-010, docs/spec/domains/motor.md §3).
 */
public final class StateGameTest {
    private static final BlockPos MOTOR_POS = new BlockPos(1, 1, 1);
    private static final BlockPos SHAFT_POS = MOTOR_POS.relative(Direction.NORTH);
    private static final BlockPos REDSTONE_POS = MOTOR_POS.relative(Direction.EAST);

    // Four chained runAfterDelay(10) phases need more than the default 20-tick budget.
    @GameTest(maxTicks = 200)
    public void fuelAndRedstoneDriveTheStateMachineAndTheNetwork(GameTestHelper helper) {
        helper.setBlock(MOTOR_POS, MotorBlocks.BLOCK.defaultBlockState().setValue(MeteredMotorBlock.FACING, Direction.NORTH));
        helper.setBlock(SHAFT_POS, AllBlocks.SHAFT.defaultBlockState().setValue(RotatedPillarKineticBlock.AXIS, Direction.Axis.Z));
        MeteredMotorBlockEntity motor = helper.getBlockEntity(MOTOR_POS, MeteredMotorBlockEntity.class);
        motor.setItem(0, new ItemStack(Items.EMERALD, 64));

        helper.runAfterDelay(10, () -> {
            assertRunning(helper, motor);

            helper.setBlock(REDSTONE_POS, Blocks.REDSTONE_BLOCK);
            helper.runAfterDelay(10, () -> {
                assertPausedOrStopped(helper, motor, MotorState.PAUSED);

                helper.setBlock(REDSTONE_POS, Blocks.AIR);
                helper.runAfterDelay(10, () -> {
                    assertRunning(helper, motor);

                    motor.clearContent();
                    helper.runAfterDelay(10, () -> {
                        assertPausedOrStopped(helper, motor, MotorState.STOPPED);
                        helper.succeed();
                    });
                });
            });
        });
    }

    private void assertRunning(GameTestHelper helper, MeteredMotorBlockEntity motor) {
        helper.assertTrue(motor.state() == MotorState.RUNNING, "the motor runs: " + motor.state());
        helper.assertTrue(motor.getGeneratedSpeed() == motor.stats().rpm(), "generated speed is the rolled rpm: " + motor.getGeneratedSpeed());
        helper.assertTrue(motor.calculateAddedStressCapacity() == motor.stats().capacity(), "added capacity is the rolled capacity: " + motor.calculateAddedStressCapacity());
        KineticBlockEntity shaft = helper.getBlockEntity(SHAFT_POS, KineticBlockEntity.class);
        helper.assertTrue(shaft.getSpeed() != 0, "the attached shaft turns: " + shaft.getSpeed());
    }

    private void assertPausedOrStopped(GameTestHelper helper, MeteredMotorBlockEntity motor, MotorState expected) {
        helper.assertTrue(motor.state() == expected, "the motor is " + expected + ": " + motor.state());
        helper.assertTrue(motor.getGeneratedSpeed() == 0, "generated speed is zero: " + motor.getGeneratedSpeed());
        helper.assertTrue(motor.calculateAddedStressCapacity() == 0, "added capacity is zero: " + motor.calculateAddedStressCapacity());
        KineticBlockEntity shaft = helper.getBlockEntity(SHAFT_POS, KineticBlockEntity.class);
        helper.assertTrue(shaft.getSpeed() == 0, "the shaft stops: " + shaft.getSpeed());
    }
}
