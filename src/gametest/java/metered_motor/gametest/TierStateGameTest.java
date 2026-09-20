package metered_motor.gametest;

import metered_motor.MeteredMotor;
import metered_motor.block.MeteredMotorBlock;
import metered_motor.block.MotorBlocks;
import metered_motor.block.MotorTier;
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
 * MM-7: the {@code tier} block state property is set at placement from the item's stats, so the
 * blockstate JSON alone picks the model set (MOTOR-REQ-013, MOTOR-DEC-004).
 */
public final class TierStateGameTest {
    @GameTest
    public void placingATierIiiItemYieldsTierIiiState(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos support = new BlockPos(1, 1, 1);
        BlockPos motorPos = support.above();
        helper.setBlock(support, Blocks.STONE);

        Stats stats = new Stats(Stats.VERSION, Tier.III);
        ItemStack stack = new ItemStack(MotorBlocks.ITEM);
        stack.set(MeteredMotor.STATS, stats);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        helper.placeAt(player, stack, support, Direction.UP);

        MotorTier tier = helper.getBlockState(motorPos).getValue(MeteredMotorBlock.TIER);
        helper.assertTrue(tier == MotorTier.III, "a tier III item places tier=iii: " + tier);
        helper.succeed();
    }

    @GameTest
    public void anItemWithNoStatsPlacesTierI(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos support = new BlockPos(1, 1, 1);
        BlockPos motorPos = support.above();
        helper.setBlock(support, Blocks.STONE);

        ItemStack stack = new ItemStack(MotorBlocks.ITEM);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        helper.placeAt(player, stack, support, Direction.UP);

        MotorTier tier = helper.getBlockState(motorPos).getValue(MeteredMotorBlock.TIER);
        helper.assertTrue(tier == MotorTier.I, "an item with no component places tier=i: " + tier);
        helper.succeed();
    }

    @GameTest
    public void breakingAndReplacingKeepsTheTier(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos support = new BlockPos(1, 1, 1);
        BlockPos motorPos = support.above();
        helper.setBlock(support, Blocks.STONE);

        Stats stats = new Stats(Stats.VERSION, Tier.III);
        ItemStack stack = new ItemStack(MotorBlocks.ITEM);
        stack.set(MeteredMotor.STATS, stats);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        helper.placeAt(player, stack, support, Direction.UP);
        helper.assertTrue(helper.getBlockState(motorPos).getValue(MeteredMotorBlock.TIER) == MotorTier.III,
            "set up: placed at tier iii");

        helper.getLevel().destroyBlock(helper.absolutePos(motorPos), false);
        helper.assertTrue(helper.getBlockState(motorPos).is(Blocks.AIR), "set up: broken");

        ItemStack replacement = new ItemStack(MotorBlocks.ITEM);
        replacement.set(MeteredMotor.STATS, stats);
        player.setItemInHand(InteractionHand.MAIN_HAND, replacement);
        helper.placeAt(player, replacement, support, Direction.UP);

        MotorTier tier = helper.getBlockState(motorPos).getValue(MeteredMotorBlock.TIER);
        helper.assertTrue(tier == MotorTier.III, "re-placing the same stats keeps tier=iii: " + tier);
        helper.succeed();
    }
}
