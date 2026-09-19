package metered_motor.gametest;

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

/**
 * MM-4: taking the last emerald stops the motor, and the next emerald resumes it (MOTOR-FAIL-004,
 * docs/spec/domains/motor.md §3). Seeding exactly one emerald and forcing the meter to one with
 * {@link metered_motor.model.Meter#holdAtOne()} makes the very first burn cycle both reach one and
 * successfully take the only emerald there is: that is MOTOR-FAIL-004 (a take that empties the
 * inventory), not MOTOR-REQ-007 (nothing left to take when the meter reaches one) — the meter ends
 * this tick at its ordinary post-take remainder, not held at one. MOTOR-REQ-007's own branch stays
 * unreached by any game test here: nothing in MM-4's scope can empty the inventory except the
 * meter's own take (extraction is fully blocked, and there is no screen yet), so the two can only
 * come apart once MM-6 lets a player empty the screen mid-cycle.
 */
public final class EmptyGameTest {
    private static final BlockPos MOTOR_POS = new BlockPos(1, 1, 1);

    @GameTest(maxTicks = 100)
    public void theLastEmeraldStopsTheMotorAndTheNextResumesIt(GameTestHelper helper) {
        helper.setBlock(MOTOR_POS, MotorBlocks.BLOCK.defaultBlockState().setValue(MeteredMotorBlock.FACING, Direction.NORTH));
        MeteredMotorBlockEntity motor = helper.getBlockEntity(MOTOR_POS, MeteredMotorBlockEntity.class);
        motor.setItem(0, new ItemStack(Items.EMERALD, 1));
        motor.meter().holdAtOne();

        // The burn cycle that takes the last emerald lands somewhere in the first 20 ticks (once
        // a second, phased against the world's absolute game time); 25 is a safe margin.
        helper.runAfterDelay(25, () -> {
            helper.assertTrue(motor.state() == MotorState.STOPPED, "the last emerald taken stops the motor: " + motor.state());
            helper.assertTrue(motor.emeraldsInside() == 0, "the inventory is empty: " + motor.emeraldsInside());
            helper.assertTrue(motor.meter().fraction() < 1.0, "the meter is at its ordinary post-take remainder, not held: " + motor.meter().fraction());

            motor.setItem(0, new ItemStack(Items.EMERALD, 1));
            helper.runAfterDelay(40, () -> {
                helper.assertTrue(motor.state() == MotorState.RUNNING, "an emerald arriving resumes the motor within two seconds: " + motor.state());
                helper.succeed();
            });
        });
    }
}
