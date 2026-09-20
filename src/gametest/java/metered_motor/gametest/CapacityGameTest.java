package metered_motor.gametest;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.api.stress.BlockStressValues;
import metered_motor.MeteredMotor;
import metered_motor.block.MeteredMotorBlock;
import metered_motor.block.MeteredMotorBlockEntity;
import metered_motor.block.MotorBlocks;
import metered_motor.block.MotorState;
import metered_motor.model.Stats;
import metered_motor.model.Tier;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * MM-21: the bug `decisions/DEC-009-fixed-tiers.md` fixes. Create's {@code KineticNetwork} sums a
 * source's contribution as {@code calculateAddedStressCapacity() * abs(getGeneratedSpeed())}
 * (confirmed by full bytecode disassembly of {@code KineticNetwork.getActualCapacityOf} at this
 * ticket, and by `vault/technical/minecraft/create-fly-steam-engines-26-2.md` §B) — stress
 * capacity is per-rpm, not a total — so a running motor's {@code calculateAddedStressCapacity()}
 * must return the tier's capacity divided by 64, not the tier's whole capacity, or the network
 * would see 64x the tier's true capacity (MOTOR-REQ-004). This checks the product actually lands
 * on the tier's fixed capacity for every tier, and that a millstone load reads the expected
 * fraction of it.
 */
public final class CapacityGameTest {
    private static final BlockPos MOTOR_POS = new BlockPos(1, 1, 1);
    private static final BlockPos CONSUMER_POS = MOTOR_POS.above();

    @GameTest(maxTicks = 60)
    public void aRunningTierIMotorContributesItsFixedCapacityAt64Rpm(GameTestHelper helper) {
        assertContributesExactCapacity(helper, Tier.I);
    }

    @GameTest(maxTicks = 60)
    public void aRunningTierIiMotorContributesItsFixedCapacityAt64Rpm(GameTestHelper helper) {
        assertContributesExactCapacity(helper, Tier.II);
    }

    @GameTest(maxTicks = 60)
    public void aRunningTierIiiMotorContributesItsFixedCapacityAt64Rpm(GameTestHelper helper) {
        assertContributesExactCapacity(helper, Tier.III);
    }

    private void assertContributesExactCapacity(GameTestHelper helper, Tier tier) {
        helper.setBlock(MOTOR_POS, MotorBlocks.BLOCK.defaultBlockState().setValue(MeteredMotorBlock.FACING, Direction.UP));
        MeteredMotorBlockEntity motor = helper.getBlockEntity(MOTOR_POS, MeteredMotorBlockEntity.class);
        motor.applyComponentsFromItemStack(stackOf(tier));
        motor.setItem(0, new ItemStack(Items.EMERALD, 64));

        helper.succeedWhen(() -> {
            helper.assertTrue(motor.state() == MotorState.RUNNING, "set up: the motor runs: " + motor.state());
            helper.assertTrue(motor.getGeneratedSpeed() == 64f, "generated speed is 64 rpm for every tier: " + motor.getGeneratedSpeed());

            float expected = tier.capacity();
            float perRpm = motor.calculateAddedStressCapacity();
            float networkTotal = perRpm * Math.abs(motor.getGeneratedSpeed());
            helper.assertTrue(
                Math.abs(networkTotal - expected) < 1e-3,
                "calculateAddedStressCapacity() * |generatedSpeed| equals the tier's fixed capacity: expected " + expected + " got "
                    + networkTotal);

            helper.assertTrue(motor.networkCapacity() > 0f, "waiting for the network to sync its capacity");
            helper.assertTrue(
                Math.abs(motor.networkCapacity() - expected) < 1.0f,
                "the network's own synced capacity equals the tier's fixed capacity within rounding: expected " + expected + " got "
                    + motor.networkCapacity());
        });
    }

    @GameTest(maxTicks = 60)
    public void aTierIiMotorUnderAMillstoneLoadReadsTheExpectedFraction(GameTestHelper helper) {
        helper.setBlock(MOTOR_POS, MotorBlocks.BLOCK.defaultBlockState().setValue(MeteredMotorBlock.FACING, Direction.UP));
        MeteredMotorBlockEntity motor = helper.getBlockEntity(MOTOR_POS, MeteredMotorBlockEntity.class);
        motor.applyComponentsFromItemStack(stackOf(Tier.II));
        motor.setItem(0, new ItemStack(Items.EMERALD, 64));
        helper.setBlock(CONSUMER_POS, AllBlocks.MILLSTONE.defaultBlockState());
        double impact = BlockStressValues.getImpact(AllBlocks.MILLSTONE);

        helper.succeedWhen(() -> {
            helper.assertTrue(motor.state() == MotorState.RUNNING, "set up: the motor runs: " + motor.state());
            helper.assertTrue(motor.networkCapacity() > 0f, "waiting for the network's capacity to sync");

            // The millstone's raw impact is consumed into the network's actual stress scaled by
            // the network's shared generated speed (64 rpm), the same way KineticNetwork.
            // getActualStressOf scales it, and the fixed capacity here is already unscaled
            // (MOTOR-REQ-004, `DEC-009`): the ×64 does not cancel the way it would have before
            // this ticket's fix (see MeterGameTest's own note).
            double expectedLoad = Math.min(1.0, impact * Tier.II.rpm() / Tier.II.capacity());
            helper.assertTrue(expectedLoad > 0.0, "set up: the millstone draws a nonzero load: " + impact);
            double tolerance = Math.max(1e-6, expectedLoad * 1e-3);
            helper.assertTrue(
                Math.abs(motor.load() - expectedLoad) < tolerance,
                "the load reads (impact * 64) / the tier's fixed capacity: expected " + expectedLoad + " got " + motor.load());
        });
    }

    private static ItemStack stackOf(Tier tier) {
        ItemStack stack = new ItemStack(MotorBlocks.ITEM);
        stack.set(MeteredMotor.STATS, Stats.of(tier));
        return stack;
    }
}
