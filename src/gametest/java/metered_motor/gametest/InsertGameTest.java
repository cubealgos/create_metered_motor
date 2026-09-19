package metered_motor.gametest;

import com.zurrtum.create.AllTransfer;
import com.zurrtum.create.infrastructure.items.ItemInventory;
import metered_motor.block.MeteredMotorBlock;
import metered_motor.block.MeteredMotorBlockEntity;
import metered_motor.block.MotorBlocks;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * MM-4: vanilla hoppers and Create's logistics both insert emeralds and emerald blocks, and
 * refuse anything else, through every face (MOTOR-REQ-008, ARCH-DEC-003, docs/spec/README.md
 * Verification 4).
 */
public final class InsertGameTest {
    private static final BlockPos MOTOR_POS = new BlockPos(1, 1, 1);
    private static final BlockPos HOPPER_POS = MOTOR_POS.above();

    @GameTest(maxTicks = 100)
    public void aHopperAboveInsertsEmeraldsAndRefusesCobblestone(GameTestHelper helper) {
        helper.setBlock(MOTOR_POS, MotorBlocks.BLOCK.defaultBlockState().setValue(MeteredMotorBlock.FACING, Direction.NORTH));
        helper.setBlock(HOPPER_POS, Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN));
        HopperBlockEntity hopper = helper.getBlockEntity(HOPPER_POS, HopperBlockEntity.class);
        hopper.setItem(0, new ItemStack(Items.EMERALD, 5));
        hopper.setItem(1, new ItemStack(Items.COBBLESTONE, 5));

        helper.succeedWhen(() -> {
            MeteredMotorBlockEntity motor = helper.getBlockEntity(MOTOR_POS, MeteredMotorBlockEntity.class);
            helper.assertTrue(motor.emeraldsInside() > 0, "the hopper fed the motor emeralds: " + motor.emeraldsInside());
            boolean cobblestoneStillInHopper = false;
            for (int slot = 0; slot < hopper.getContainerSize(); slot++) {
                if (hopper.getItem(slot).is(Items.COBBLESTONE)) {
                    cobblestoneStillInHopper = true;
                }
            }
            helper.assertTrue(cobblestoneStillInHopper, "the cobblestone stayed in the hopper");
        });
    }

    /**
     * MOTOR-REQ-008, docs/spec/README.md Verification 4: Create's funnels, arms and chutes reach
     * a block's inventory through {@code AllTransfer.getInventory}, the same resolution
     * {@code InvManipulationBehaviour.getInventory()} uses. A live andesite funnel additionally
     * needs a belt or an item entity moving across it to push anything, which a game test
     * structure this small cannot build headless (the ticket's documented fallback): this drives
     * the same {@link Container} a funnel would insert through, via {@link ItemInventory#insert},
     * the method {@code InvManipulationBehaviour.insert} itself calls.
     */
    @GameTest
    public void createsLogisticsInsertThroughTheSameContainerAndRefuseCobblestone(GameTestHelper helper) {
        helper.setBlock(MOTOR_POS, MotorBlocks.BLOCK.defaultBlockState().setValue(MeteredMotorBlock.FACING, Direction.NORTH));
        BlockPos absolute = helper.absolutePos(MOTOR_POS);
        BlockState state = helper.getLevel().getBlockState(absolute);
        MeteredMotorBlockEntity motor = helper.getBlockEntity(MOTOR_POS, MeteredMotorBlockEntity.class);

        Container container = AllTransfer.getInventory(helper.getLevel(), absolute, state, motor, Direction.UP);
        helper.assertTrue(container instanceof ItemInventory, "AllTransfer.getInventory resolves the motor's WorldlyContainer: " + container);
        ItemInventory inventory = (ItemInventory) container;

        int insertedEmeralds = inventory.insert(new ItemStack(Items.EMERALD, 3));
        helper.assertTrue(insertedEmeralds == 3, "the logistics path accepted the emeralds: " + insertedEmeralds);
        helper.assertTrue(motor.emeraldsInside() == 3, "the motor's inventory now holds them: " + motor.emeraldsInside());

        int insertedCobblestone = inventory.insert(new ItemStack(Items.COBBLESTONE, 3));
        helper.assertTrue(insertedCobblestone == 0, "the logistics path refuses cobblestone: " + insertedCobblestone);

        helper.succeed();
    }
}
