package metered_motor.gametest;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.api.stress.BlockStressValues;
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
 * MM-4: the meter advances once a second in proportion to the network's actual load, read the way
 * Create's stress gauge reads it, and not at all with nothing on the network to draw it
 * (MOTOR-REQ-006, MOTOR-DEC-001, MOTOR-FAIL-002).
 *
 * <p>{@code Stats.ratePerMinute()} puts a whole emerald minutes to hours away even at the
 * smallest legal roll (`MOTOR-DEC-003`: sustainable burn is the point), so rather than waiting for
 * a take, this asserts the meter's fractional advance directly, the fallback the ticket itself
 * documents.
 */
public final class MeterGameTest {
    private static final BlockPos MOTOR_POS = new BlockPos(1, 1, 1);
    private static final BlockPos CONSUMER_POS = MOTOR_POS.above();

    @GameTest(maxTicks = 60)
    public void noConsumerReadsZeroLoadAndDoesNotAdvanceTheMeter(GameTestHelper helper) {
        MeteredMotorBlockEntity motor = placeFueledMotor(helper);

        // The meter advances once every 20 ticks; 50 covers more than two cycles with margin,
        // and a network alone with its own generator (MOTOR-FAIL-002) reads zero load regardless
        // of how many cycles land, so this has no timing dependency to get wrong.
        helper.runAfterDelay(50, () -> {
            helper.assertTrue(motor.state() == MotorState.RUNNING, "set up: the motor runs: " + motor.state());
            helper.assertTrue(motor.load() == 0.0, "a motor alone on its network reads zero load: " + motor.load());
            helper.assertTrue(motor.meter().fraction() == 0.0, "the meter does not advance with nothing to burn: " + motor.meter().fraction());
            helper.succeed();
        });
    }

    @GameTest(maxTicks = 60)
    public void aKnownConsumerAdvancesTheMeterByTheComputedFraction(GameTestHelper helper) {
        MeteredMotorBlockEntity motor = placeFueledMotor(helper);
        helper.setBlock(CONSUMER_POS, AllBlocks.MILLSTONE.defaultBlockState());
        double impact = BlockStressValues.getImpact(AllBlocks.MILLSTONE);

        // succeedWhen polls every tick: the first tick the fraction leaves zero is exactly the
        // tick the first (and, within this short window, only) burn cycle ran, sidestepping any
        // need to know which absolute game-time tick the once-a-second boundary falls on.
        helper.succeedWhen(() -> {
            helper.assertTrue(motor.state() == MotorState.RUNNING, "set up: the motor runs: " + motor.state());
            helper.assertTrue(motor.meter().fraction() != 0.0, "waiting for the first burn cycle");

            double expectedLoad = Math.min(1.0, impact / motor.stats().capacity());
            helper.assertTrue(expectedLoad > 0.0, "set up: the millstone draws a nonzero load: " + impact);
            assertApproximately(helper, motor.load(), expectedLoad, "the load is min(1, network stress / network capacity)");

            double expectedFraction = motor.stats().ratePerMinute() / 60.0 * expectedLoad;
            assertApproximately(helper, motor.meter().fraction(), expectedFraction, "the meter advanced by rate/60*load");
        });
    }

    private static void assertApproximately(GameTestHelper helper, double actual, double expected, String message) {
        double tolerance = Math.max(1e-9, Math.abs(expected) * 1e-3);
        helper.assertTrue(Math.abs(actual - expected) < tolerance, message + ": expected " + expected + " got " + actual);
    }

    private MeteredMotorBlockEntity placeFueledMotor(GameTestHelper helper) {
        helper.setBlock(MOTOR_POS, MotorBlocks.BLOCK.defaultBlockState().setValue(MeteredMotorBlock.FACING, Direction.UP));
        MeteredMotorBlockEntity motor = helper.getBlockEntity(MOTOR_POS, MeteredMotorBlockEntity.class);
        motor.setItem(0, new ItemStack(Items.EMERALD, 64));
        return motor;
    }
}
