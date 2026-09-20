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

/**
 * MM-3: placing from an item copies its stats into the block entity, and an item with no
 * component places as a plain tier I motor (MOTOR-REQ-003, MOTOR-FAIL-003). MM-9: an item whose
 * stats component is newer than this build refuses placement outright (MOTOR-REQ-014).
 */
public final class PlacementGameTest {
    @GameTest
    public void placingFromAnItemCopiesItsStats(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos support = new BlockPos(1, 1, 1);
        BlockPos motorPos = support.above();
        helper.setBlock(support, Blocks.STONE);

        Stats stats = new Stats(Stats.VERSION, Tier.II);
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
    public void anItemWithNoStatsPlacesAsTierI(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos support = new BlockPos(1, 1, 1);
        BlockPos motorPos = support.above();
        helper.setBlock(support, Blocks.STONE);

        ItemStack stack = new ItemStack(MotorBlocks.ITEM);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        helper.placeAt(player, stack, support, Direction.UP);

        MeteredMotorBlockEntity blockEntity = helper.getBlockEntity(motorPos, MeteredMotorBlockEntity.class);
        Stats expected = Stats.of(Tier.I);
        helper.assertTrue(expected.equals(blockEntity.stats()), "an item with no component places as a plain tier I motor: " + blockEntity.stats());
        helper.succeed();
    }

    @GameTest
    public void aNewerStatsVersionRefusesPlacement(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos support = new BlockPos(1, 1, 1);
        BlockPos motorPos = support.above();
        helper.setBlock(support, Blocks.STONE);
        helper.setBlock(motorPos, Blocks.AIR);

        Stats future = new Stats(Stats.VERSION + 1, Tier.I);
        ItemStack stack = new ItemStack(MotorBlocks.ITEM);
        stack.set(MeteredMotor.STATS, future);
        helper.assertTrue(future.readOnly(), "set up: a version newer than this build's is read-only");
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        helper.placeAt(player, stack, support, Direction.UP);

        helper.assertTrue(
            !helper.getBlockState(motorPos).is(MotorBlocks.BLOCK), "a read-only item's placement is refused: " + helper.getBlockState(motorPos));
        helper.succeed();
    }
}
