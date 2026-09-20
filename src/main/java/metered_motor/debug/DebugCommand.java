package metered_motor.debug;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import metered_motor.MeteredMotor;
import metered_motor.block.MeteredMotorBlockEntity;
import metered_motor.block.MotorBlocks;
import metered_motor.model.Stats;
import metered_motor.model.Tier;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Development-only: {@code /metered_motor debug <I|II|III>} writes the chosen tier's fixed stats
 * (`decisions/DEC-009-fixed-tiers.md`: nothing is rolled, every motor of a tier identical) onto
 * the motor in the player's main hand (or a new one, given if none is held), and onto a placed
 * motor within 8 blocks of the player's look, filling its inventory with emerald blocks when it
 * exposes one. For screenshots and screen checks; never registered in a released jar (MM-8;
 * MM-21 dropped the {@code [rpm] [capacity] [efficiency]} override arguments along with the roll
 * they used to seed — there is nothing left to override once a tier's stats are fixed).
 */
public final class DebugCommand {
    /** Bound so a placed motor's inventory (0 to 320 blocks, docs/spec/domains/motor.md §3) can be filled in one command. */
    private static final double MAX_LOOK_DISTANCE = 8.0;

    private DebugCommand() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> dispatcher.register(
            Commands.literal(MeteredMotor.MOD_ID).then(Commands.literal("debug")
                .then(Commands.literal("I").executes(c -> run(c.getSource(), Tier.I)))
                .then(Commands.literal("II").executes(c -> run(c.getSource(), Tier.II)))
                .then(Commands.literal("III").executes(c -> run(c.getSource(), Tier.III))))));
    }

    static int run(CommandSourceStack source, Tier tier) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Stats stats = Stats.of(tier);

        ItemStack held = player.getMainHandItem();
        ItemStack target = held.is(MotorBlocks.ITEM) ? held : new ItemStack(MotorBlocks.ITEM);
        target.set(MeteredMotor.STATS, stats);
        if (target != held && !player.getInventory().add(target)) {
            player.drop(target, false);
        }

        HitResult hit = player.pick(MAX_LOOK_DISTANCE, 0, false);
        if (hit instanceof BlockHitResult blockHit && blockHit.getType() == HitResult.Type.BLOCK) {
            fillLookedAtMotor(player.level(), blockHit.getBlockPos(), stats);
        }

        source.sendSuccess(() -> Component.translatable("command.metered_motor.debug.done", tier.number()), false);
        return 1;
    }

    /**
     * Writes {@code stats} onto the block entity at {@code pos} when it is a metered motor, and
     * fills every slot of its inventory with a stack of emerald blocks.
     */
    private static void fillLookedAtMotor(Level level, BlockPos pos, Stats stats) {
        var raw = level.getBlockEntity(pos);
        if (!(raw instanceof MeteredMotorBlockEntity blockEntity)) {
            return;
        }
        ItemStack carrier = new ItemStack(MotorBlocks.ITEM);
        carrier.set(MeteredMotor.STATS, stats);
        blockEntity.applyComponentsFromItemStack(carrier);
        blockEntity.setChanged();
        if (raw instanceof Container container) {
            ItemStack emeraldBlocks = new ItemStack(Items.EMERALD_BLOCK, container.getMaxStackSize());
            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                container.setItem(slot, emeraldBlocks.copy());
            }
            container.setChanged();
        }
    }
}
