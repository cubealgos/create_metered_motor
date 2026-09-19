package metered_motor.gametest;

import com.zurrtum.create.AllBlocks;
import metered_motor.block.MeteredMotorBlock;
import metered_motor.block.MeteredMotorBlockEntity;
import metered_motor.block.MotorBlocks;
import metered_motor.block.MotorState;
import metered_motor.client.StatsText;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * MM-6: the readout the screen would show is exactly the block entity's own synced fields, read
 * the same way the screen reads them — through {@link StatsText}'s pure formatting, so no menu or
 * render context is needed on the dedicated server this game test runs on
 * (docs/spec/domains/ui.md `UI-REQ-003`, `UI-DEC-002`).
 */
public final class SyncGameTest {
    private static final BlockPos MOTOR_POS = new BlockPos(1, 1, 1);
    private static final BlockPos CONSUMER_POS = MOTOR_POS.above();

    @GameTest(maxTicks = 60)
    public void theReadoutFieldsMatchTheBlockEntityAfterABurnCycle(GameTestHelper helper) {
        helper.setBlock(MOTOR_POS, MotorBlocks.BLOCK.defaultBlockState().setValue(MeteredMotorBlock.FACING, Direction.UP));
        MeteredMotorBlockEntity motor = helper.getBlockEntity(MOTOR_POS, MeteredMotorBlockEntity.class);
        motor.setItem(0, new ItemStack(Items.EMERALD, 64));
        helper.setBlock(CONSUMER_POS, AllBlocks.MILLSTONE.defaultBlockState());

        // succeedWhen polls every tick, as MeterGameTest does: the burn runs once every 20 ticks
        // on the world's own game-time modulus, not 20 ticks after this test started, so a fixed
        // delay could sample either side of the first cycle depending on when the test began.
        helper.succeedWhen(() -> {
            helper.assertTrue(motor.state() == MotorState.RUNNING, "set up: the motor runs: " + motor.state());
            helper.assertTrue(motor.meter().fraction() != 0.0 || motor.load() > 0.0, "waiting for the first burn cycle");

            // UI-REQ-003: load as a whole-number percentage.
            String loadText = StatsText.wholePercent(motor.load());
            double expectedPercent = Math.round(motor.load() * 100.0);
            helper.assertTrue(
                loadText.contains(String.valueOf((long) expectedPercent)), "the formatted load contains " + expectedPercent + ": " + loadText);
            helper.assertTrue(motor.load() > 0.0, "set up: the millstone draws a nonzero load: " + motor.load());

            // The meter has advanced by the same fraction burnOneSecond() computed (MOTOR-REQ-006).
            helper.assertTrue(motor.meter().fraction() >= 0.0 && motor.meter().fraction() < 1.0, "the meter stays within its bounds: " + motor.meter().fraction());

            // UI-REQ-003: emeralds inside, blocks counted as nine (MOTOR-REQ-006).
            int emeralds = motor.emeraldsInside();
            helper.assertTrue(emeralds >= 63 && emeralds <= 64, "64 emeralds fed in, at most one taken by the first burn cycle: " + emeralds);

            // UI-REQ-003, MOTOR-DEC-001: the state line the screen would show.
            helper.assertTrue(
                StatsText.state(motor.state()).equals("running"), "the formatted state matches the block entity: " + StatsText.state(motor.state()));

            // UI-REQ-003: minutes and seconds remaining at the current load, formatted from the
            // same accessor the screen calls.
            String remaining = StatsText.remaining(motor.secondsRemaining());
            helper.assertTrue(remaining != null, "a running, loaded motor reports a remaining time, not idle");
            helper.succeed();
        });
    }

    @GameTest(maxTicks = 20)
    public void anIdleMotorFormatsAsIdleNotAsATime(GameTestHelper helper) {
        helper.setBlock(MOTOR_POS, MotorBlocks.BLOCK.defaultBlockState().setValue(MeteredMotorBlock.FACING, Direction.UP));
        MeteredMotorBlockEntity motor = helper.getBlockEntity(MOTOR_POS, MeteredMotorBlockEntity.class);
        // No fuel: MOTOR-REQ-007 keeps it stopped, MOTOR-FAIL-002/UI-FAIL-002 read as idle.

        helper.assertTrue(motor.state() == MotorState.STOPPED, "an unfuelled motor is stopped: " + motor.state());
        helper.assertTrue(StatsText.remaining(motor.secondsRemaining()) == null, "an idle motor's remaining time formats as idle, not a duration");
        helper.assertTrue(StatsText.wholePercent(motor.load()).equals("0"), "an idle motor reads zero load: " + StatsText.wholePercent(motor.load()));
        helper.succeed();
    }
}
