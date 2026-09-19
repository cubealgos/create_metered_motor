package metered_motor.gametest;

import metered_motor.MeteredMotor;
import metered_motor.block.MeteredMotorBlockEntity;
import metered_motor.block.MotorBlocks;
import metered_motor.model.Stats;
import metered_motor.model.Tier;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

/** MM-3: placing from an item copies its stats into the block entity, and an unrolled item places rolled at tier I's middle (MOTOR-REQ-003, MOTOR-FAIL-003). */
public final class PlacementGameTest {
    @GameTest
    public void placingFromAnItemCopiesItsStats(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos support = new BlockPos(1, 1, 1);
        BlockPos motorPos = support.above();
        helper.setBlock(support, Blocks.STONE);

        Stats stats = new Stats(Stats.VERSION, Tier.II, 100, 4_096, 1.0);
        ItemStack stack = new ItemStack(MotorBlocks.ITEM);
        stack.set(MeteredMotor.STATS, stats);
        // helper.placeAt places whatever the player holds, not its own stack argument.
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        helper.placeAt(player, stack, support, Direction.UP);

        MeteredMotorBlockEntity blockEntity = helper.getBlockEntity(motorPos, MeteredMotorBlockEntity.class);
        helper.assertTrue(stats.equals(blockEntity.stats()), "the block entity carries the item's stats: " + blockEntity.stats());
        helper.succeed();
    }

    @GameTest
    public void anItemWithNoStatsPlacesRolledAtTheMiddleOfTierI(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos support = new BlockPos(1, 1, 1);
        BlockPos motorPos = support.above();
        helper.setBlock(support, Blocks.STONE);

        ItemStack stack = new ItemStack(MotorBlocks.ITEM);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        helper.placeAt(player, stack, support, Direction.UP);

        MeteredMotorBlockEntity blockEntity = helper.getBlockEntity(motorPos, MeteredMotorBlockEntity.class);
        Stats expected = Stats.middleOf(Tier.I);
        helper.assertTrue(expected.equals(blockEntity.stats()), "an unrolled item rolls at tier I's middle: " + blockEntity.stats());
        helper.succeed();
    }
}
