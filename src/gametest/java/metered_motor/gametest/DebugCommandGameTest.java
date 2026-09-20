package metered_motor.gametest;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import metered_motor.MeteredMotor;
import metered_motor.block.MeteredMotorBlockEntity;
import metered_motor.block.MotorBlocks;
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

/**
 * MM-8: the development-only command runs on the dispatcher and writes a tier's fixed stats onto
 * the held or looked-at motor. MM-21 (`decisions/DEC-009-fixed-tiers.md`) dropped the
 * {@code [rpm] [capacity] [efficiency]} override arguments and the numeric {@code <tier>}
 * argument along with the roll they used to seed: {@code /metered_motor debug <I|II|III>} is the
 * whole surface now.
 */
public final class DebugCommandGameTest {
    @GameTest
    public void theCommandWritesTheTiersFixedStatsOntoTheHeldMotorOnADevelopmentServer(GameTestHelper helper) {
        helper.assertTrue(FabricLoader.getInstance().isDevelopmentEnvironment(), "game tests run in the development environment, where the command exists");
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(MotorBlocks.ITEM));

        execute(helper, player, "metered_motor debug II");

        Stats held = player.getItemInHand(InteractionHand.MAIN_HAND).get(MeteredMotor.STATS);
        helper.assertTrue(Stats.of(Tier.II).equals(held), "the held motor carries tier II's fixed stats: " + held);
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
    public void theCommandWritesTheTiersFixedStatsOntoAMotorThePlayerLooksAtOnADevelopmentServer(GameTestHelper helper) {
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

        execute(helper, player, "metered_motor debug III");

        MeteredMotorBlockEntity blockEntity = helper.getBlockEntity(motorPos, MeteredMotorBlockEntity.class);
        helper.assertTrue(Stats.of(Tier.III).equals(blockEntity.stats()), "the looked-at motor carries tier III's fixed stats: " + blockEntity.stats());
        helper.succeed();
    }

    @GameTest
    public void anUnknownTierLiteralIsRejectedByTheParser(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        boolean rejected;
        try {
            execute(helper, player, "metered_motor debug IV");
            rejected = false;
        } catch (AssertionError e) {
            rejected = true;
        }
        helper.assertTrue(rejected, "\"IV\" is not one of the three literal branches and is rejected");
        helper.succeed();
    }

    @GameTest
    public void aNumericTierIsNoLongerAcceptedByTheParser(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        boolean rejected;
        try {
            execute(helper, player, "metered_motor debug 2");
            rejected = false;
        } catch (AssertionError e) {
            rejected = true;
        }
        helper.assertTrue(rejected, "MM-21 dropped the numeric <tier> argument: only I, II and III are registered");
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
