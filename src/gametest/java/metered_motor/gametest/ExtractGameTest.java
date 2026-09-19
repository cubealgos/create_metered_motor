package metered_motor.gametest;

import metered_motor.block.MeteredMotorBlock;
import metered_motor.block.MeteredMotorBlockEntity;
import metered_motor.block.MotorBlocks;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

/**
 * MM-4: no face extracts from the motor; a hopper below it, which always sucks from the container
 * directly above regardless of its own facing, takes nothing (MOTOR-REQ-009).
 */
public final class ExtractGameTest {
    private static final BlockPos MOTOR_POS = new BlockPos(1, 1, 1);
    private static final BlockPos HOPPER_POS = MOTOR_POS.below();

    @GameTest(maxTicks = 140)
    public void aHopperBelowExtractsNothing(GameTestHelper helper) {
        helper.setBlock(MOTOR_POS, MotorBlocks.BLOCK.defaultBlockState().setValue(MeteredMotorBlock.FACING, Direction.NORTH));
        helper.setBlock(HOPPER_POS, Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN));
        MeteredMotorBlockEntity motor = helper.getBlockEntity(MOTOR_POS, MeteredMotorBlockEntity.class);
        motor.setItem(0, new ItemStack(Items.EMERALD, 10));

        // A hopper's suck cooldown is 8 ticks; 120 ticks is many attempts, generous but bounded.
        helper.runAfterDelay(120, () -> {
            helper.assertTrue(motor.emeraldsInside() == 10, "the motor kept every emerald: " + motor.emeraldsInside());
            HopperBlockEntity hopper = helper.getBlockEntity(HOPPER_POS, HopperBlockEntity.class);
            helper.assertTrue(hopper.isEmpty(), "the hopper below the motor took nothing");
            helper.succeed();
        });
    }
}
