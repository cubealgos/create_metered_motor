package metered_motor.gametest;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import metered_motor.MeteredMotor;
import metered_motor.block.MeteredMotorBlockEntity;
import metered_motor.block.MotorBlocks;
import metered_motor.debug.DebugCommand;
import metered_motor.model.Stats;
import metered_motor.model.Tier;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/** MM-8: the pure stats builder rolls within bands and honours overrides, and the development-only command runs on the dispatcher and rejects a bad tier. */
public final class DebugCommandGameTest {
    @GameTest
    public void theBuilderRollsWithinTierBandsAndHonoursOverrides(GameTestHelper helper) {
        for (Tier tier : Tier.values()) {
            Stats atZero = DebugCommand.stats(tier, null, null, null, () -> 0.0);
            helper.assertTrue(atZero.rpm() == (int) Math.round(tier.rpmBand().min()),
                "rolled rpm at random 0.0 is the tier's band minimum for " + tier + ": " + atZero.rpm());
            helper.assertTrue(atZero.capacity() == (int) Math.round(tier.capacityBand().min()),
                "rolled capacity at random 0.0 is the tier's band minimum for " + tier + ": " + atZero.capacity());
            helper.assertTrue(atZero.efficiency() == tier.efficiencyBand().min(),
                "rolled efficiency at random 0.0 is the tier's band minimum for " + tier + ": " + atZero.efficiency());

            Stats nearOne = DebugCommand.stats(tier, null, null, null, () -> 0.999999);
            helper.assertTrue(nearOne.rpm() <= tier.rpmBand().max() && nearOne.rpm() >= tier.rpmBand().min(),
                "rolled rpm stays within the band for " + tier + ": " + nearOne.rpm());
            helper.assertTrue(nearOne.capacity() <= tier.capacityBand().max() && nearOne.capacity() >= tier.capacityBand().min(),
                "rolled capacity stays within the band for " + tier + ": " + nearOne.capacity());
        }

        Stats overridden = DebugCommand.stats(Tier.III, 90, 9_000, 2.0,
            () -> {
                throw new AssertionError("random must not be drawn when every stat is given");
            });
        helper.assertTrue(overridden.equals(new Stats(Stats.VERSION, Tier.III, 90, 9_000, 2.0)),
            "every given stat is used as is: " + overridden);
        helper.succeed();
    }

    @GameTest
    public void theCommandWritesChosenStatsOntoTheHeldMotorOnADevelopmentServer(GameTestHelper helper) {
        helper.assertTrue(FabricLoader.getInstance().isDevelopmentEnvironment(), "game tests run in the development environment, where the command exists");
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(MotorBlocks.ITEM));

        execute(helper, player, "metered_motor debug 2 80 4096 1.5");

        Stats held = player.getItemInHand(InteractionHand.MAIN_HAND).get(MeteredMotor.STATS);
        Stats expected = new Stats(Stats.VERSION, Tier.II, 80, 4_096, 1.5);
        helper.assertTrue(expected.equals(held), "the held motor carries exactly the given stats: " + held);
        helper.succeed();
    }

    @GameTest
    public void theCommandGivesANewMotorWhenNoneIsHeldOnADevelopmentServer(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

        execute(helper, player, "metered_motor debug I");

        boolean carriesTier1 = player.getInventory().contains(stack -> stack.is(MotorBlocks.ITEM) && stack.get(MeteredMotor.STATS) != null
            && stack.get(MeteredMotor.STATS).tier() == Tier.I);
        helper.assertTrue(carriesTier1, "an empty-handed player is given a new tier I motor");
        helper.succeed();
    }

    @GameTest
    public void theCommandWritesStatsOntoAMotorThePlayerLooksAtOnADevelopmentServer(GameTestHelper helper) {
        BlockPos support = new BlockPos(1, 1, 1);
        BlockPos motorPos = support.above();
        helper.setBlock(support, Blocks.STONE);
        helper.setBlock(motorPos, MotorBlocks.BLOCK.defaultBlockState());

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos motorAbs = helper.absolutePos(motorPos);
        player.setPos(motorAbs.getX() + 0.5, motorAbs.getY(), motorAbs.getZ() + 3.5);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(motorAbs));
        // player.pick(distance, 0, false), as the command calls it, reads the previous tick's eye
        // position and rotation at a partial tick of 0; sync them so the freshly aimed mock player
        // is actually picked from where it now stands and looks.
        player.setOldPosAndRot();
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

        execute(helper, player, "metered_motor debug 3 200 12000 0.8");

        MeteredMotorBlockEntity blockEntity = helper.getBlockEntity(motorPos, MeteredMotorBlockEntity.class);
        Stats expected = new Stats(Stats.VERSION, Tier.III, 200, 12_000, 0.8);
        helper.assertTrue(expected.equals(blockEntity.stats()), "the looked-at motor carries the given stats: " + blockEntity.stats());
        helper.succeed();
    }

    @GameTest
    public void anUnknownTierIsRejectedByTheParser(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        boolean rejected;
        try {
            execute(helper, player, "metered_motor debug 4");
            rejected = false;
        } catch (AssertionError e) {
            rejected = true;
        }
        helper.assertTrue(rejected, "tier 4 is outside the integer(1, 3) argument and is rejected");
        helper.succeed();
    }

    private static void execute(GameTestHelper helper, ServerPlayer player, String command) {
        var dispatcher = helper.getLevel().getServer().getCommands().getDispatcher();
        try {
            dispatcher.execute(dispatcher.parse(command, player.createCommandSourceStack().withSuppressedOutput()));
        } catch (CommandSyntaxException e) {
            throw new AssertionError("the command did not run: " + e.getMessage(), e);
        }
    }
}
