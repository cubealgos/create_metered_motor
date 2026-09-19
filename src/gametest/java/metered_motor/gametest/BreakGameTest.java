package metered_motor.gametest;

import java.util.List;
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
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

/** MM-3: breaking the motor drops exactly one item carrying its stats (MOTOR-REQ-003). */
public final class BreakGameTest {
    @GameTest
    public void breakingDropsExactlyOneItemWithTheSameStats(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos support = new BlockPos(1, 1, 1);
        BlockPos motorPos = support.above();
        helper.setBlock(support, Blocks.STONE);

        Stats stats = new Stats(Stats.VERSION, Tier.III, 200, 18_432, 0.75);
        ItemStack stack = new ItemStack(MotorBlocks.ITEM);
        stack.set(MeteredMotor.STATS, stats);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        helper.placeAt(player, stack, support, Direction.UP);
        MeteredMotorBlockEntity blockEntity = helper.getBlockEntity(motorPos, MeteredMotorBlockEntity.class);
        helper.assertTrue(stats.equals(blockEntity.stats()), "set up: the block entity carries the item's stats");

        helper.getLevel().destroyBlock(helper.absolutePos(motorPos), true);

        helper.succeedWhen(() -> {
            List<ItemEntity> drops = helper.getEntities(EntityTypes.ITEM, motorPos, 2.0);
            helper.assertTrue(drops.size() == 1, "exactly one item drops: " + drops.size());
            ItemStack dropped = drops.get(0).getItem();
            helper.assertTrue(dropped.is(MotorBlocks.ITEM), "the dropped item is the motor's item: " + dropped);
            helper.assertTrue(stats.equals(dropped.get(MeteredMotor.STATS)), "the dropped item carries the same stats: " + dropped.get(MeteredMotor.STATS));
        });
    }
}
