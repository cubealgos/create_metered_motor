package metered_motor.gametest;

import com.zurrtum.create.AllBlocks;
import metered_motor.block.MeteredMotorBlock;
import metered_motor.block.MeteredMotorBlockEntity;
import metered_motor.block.MotorBlocks;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * MM-4: taking an emerald from a slot that only holds an emerald block splits it into nine loose
 * emeralds, leaving eight behind after the one taken (MOTOR-REQ-006). {@link
 * metered_motor.model.Meter#holdAtOne()} is the ticket's own suggested hook to put the meter right
 * at a take without waiting real minutes for the fraction to get there on its own.
 */
public final class SplitGameTest {
    private static final BlockPos MOTOR_POS = new BlockPos(1, 1, 1);
    private static final BlockPos CONSUMER_POS = MOTOR_POS.above();

    @GameTest(maxTicks = 60)
    public void takingFromAnEmeraldBlockLeavesEightLooseEmeralds(GameTestHelper helper) {
        helper.setBlock(MOTOR_POS, MotorBlocks.BLOCK.defaultBlockState().setValue(MeteredMotorBlock.FACING, Direction.NORTH));
        MeteredMotorBlockEntity motor = helper.getBlockEntity(MOTOR_POS, MeteredMotorBlockEntity.class);
        motor.setItem(0, new ItemStack(Items.EMERALD_BLOCK, 1));
        motor.meter().holdAtOne();

        helper.succeedWhen(() -> {
            helper.assertTrue(motor.emeraldsInside() == 8, "one emerald was taken from the block, leaving eight: " + motor.emeraldsInside());
            boolean looseEmeraldsFound = false;
            for (int slot = 0; slot < motor.getContainerSize(); slot++) {
                ItemStack stack = motor.getItem(slot);
                if (stack.is(Items.EMERALD)) {
                    helper.assertTrue(stack.getCount() == 8, "the eight emeralds sit loose in one slot: " + stack.getCount());
                    looseEmeraldsFound = true;
                }
            }
            helper.assertTrue(looseEmeraldsFound, "the split emeralds are loose, not still a block");
        });
    }

    /**
     * Review 2026-09-19: a full inventory of emerald blocks (five slots, every one holding more
     * than one block, so a split has no loose-emerald slot and no empty slot to land its
     * remainder in) must still burn instead of holding the meter at one forever while the motor
     * keeps running for free. The eight it cannot place are credited (the "Prepaid" field).
     */
    @GameTest(maxTicks = 60)
    public void aFullInventoryOfBlocksStillBurns(GameTestHelper helper) {
        helper.setBlock(MOTOR_POS, MotorBlocks.BLOCK.defaultBlockState().setValue(MeteredMotorBlock.FACING, Direction.UP));
        helper.setBlock(CONSUMER_POS, AllBlocks.MILLSTONE.defaultBlockState());
        MeteredMotorBlockEntity motor = helper.getBlockEntity(MOTOR_POS, MeteredMotorBlockEntity.class);
        for (int slot = 0; slot < motor.getContainerSize(); slot++) {
            motor.setItem(slot, new ItemStack(Items.EMERALD_BLOCK, 64));
        }
        int before = motor.emeraldsInside();
        motor.meter().holdAtOne();

        helper.succeedWhen(() -> {
            int after = motor.emeraldsInside();
            helper.assertTrue(after == before - 1,
                "one emerald was taken and the eight it could not place were credited, not lost: expected " + (before - 1) + " got " + after);
            boolean oneBlockConsumed = false;
            for (int slot = 0; slot < motor.getContainerSize(); slot++) {
                ItemStack stack = motor.getItem(slot);
                if (stack.is(Items.EMERALD_BLOCK) && stack.getCount() == 63) {
                    oneBlockConsumed = true;
                }
            }
            helper.assertTrue(oneBlockConsumed, "one slot's stack of emerald blocks dropped by exactly one");
        });
    }
}
