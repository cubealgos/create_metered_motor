package metered_motor.debug;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Locale;
import java.util.function.DoubleSupplier;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Development-only: {@code /metered_motor debug <tier> [rpm] [capacity] [efficiency]} writes stats
 * of the given tier — any omitted stat rolled within the tier's bands, any given one used as is —
 * onto the motor in the player's main hand (or a new one, given if none is held), and onto a
 * placed motor within 8 blocks of the player's look, filling its inventory with emerald blocks
 * when it exposes one. For screenshots and screen checks; never registered in a released jar
 * (MM-8).
 */
public final class DebugCommand {
    /** Bound so a placed motor's inventory (0 to 320 blocks, docs/spec/domains/motor.md §3) can be filled in one command. */
    private static final double MAX_LOOK_DISTANCE = 8.0;

    private DebugCommand() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> dispatcher.register(
            Commands.literal(MeteredMotor.MOD_ID).then(Commands.literal("debug")
                .then(Commands.argument("tier", IntegerArgumentType.integer(1, 3))
                    .executes(c -> run(c.getSource(), tierOf(c), null, null, null))
                    .then(Commands.argument("rpm", IntegerArgumentType.integer(1))
                        .executes(c -> run(c.getSource(), tierOf(c), IntegerArgumentType.getInteger(c, "rpm"), null, null))
                        .then(Commands.argument("capacity", IntegerArgumentType.integer(1))
                            .executes(c -> run(c.getSource(), tierOf(c), IntegerArgumentType.getInteger(c, "rpm"),
                                IntegerArgumentType.getInteger(c, "capacity"), null))
                            .then(Commands.argument("efficiency", DoubleArgumentType.doubleArg(0.1, 10.0))
                                .executes(c -> run(c.getSource(), tierOf(c), IntegerArgumentType.getInteger(c, "rpm"),
                                    IntegerArgumentType.getInteger(c, "capacity"), DoubleArgumentType.getDouble(c, "efficiency")))))))
                .then(Commands.literal("I").executes(c -> run(c.getSource(), Tier.I, null, null, null)))
                .then(Commands.literal("II").executes(c -> run(c.getSource(), Tier.II, null, null, null)))
                .then(Commands.literal("III").executes(c -> run(c.getSource(), Tier.III, null, null, null))))));
    }

    private static Tier tierOf(CommandContext<CommandSourceStack> context) {
        return Tier.ofNumber(IntegerArgumentType.getInteger(context, "tier"));
    }

    static int run(CommandSourceStack source, Tier tier, Integer rpm, Integer capacity, Double efficiency) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        RandomSource random = player.level().getRandom();
        Stats stats = stats(tier, rpm, capacity, efficiency, random::nextDouble);

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

        String efficiencyText = String.format(Locale.ROOT, "%.2f", stats.efficiency());
        source.sendSuccess(() -> Component.translatable(
            "command.metered_motor.debug.done", tier.number(), stats.rpm(), stats.capacity(), efficiencyText), false);
        return 1;
    }

    /**
     * Writes {@code stats} onto the block entity at {@code pos} when it is a metered motor, and,
     * once it exposes an inventory (MM-4), fills every slot with a stack of emerald blocks; a
     * plain {@code instanceof Container} check so this runs unchanged before and after MM-4 merges.
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
        // MeteredMotorBlockEntity is final and does not yet implement Container, so the check runs
        // against the raw block entity reference; once MM-4 adds the interface, this starts firing
        // without needing a recompile of this file.
        if (raw instanceof Container container) {
            ItemStack emeraldBlocks = new ItemStack(Items.EMERALD_BLOCK, container.getMaxStackSize());
            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                container.setItem(slot, emeraldBlocks.copy());
            }
            container.setChanged();
        }
    }

    /**
     * Builds a tier's stats: any of {@code rpm}, {@code capacity} or {@code efficiency} left
     * {@code null} is rolled within the tier's default band from {@code random} (drawing in
     * [0, 1)); a given one is used as is, validated only by {@link Stats}'s own bounds. Pure of the
     * command so the game test can call it directly.
     */
    public static Stats stats(Tier tier, Integer rpm, Integer capacity, Double efficiency, DoubleSupplier random) {
        int rolledRpm = rpm != null ? rpm : (int) Math.round(tier.rpmBand().roll(random));
        int rolledCapacity = capacity != null ? capacity : (int) Math.round(tier.capacityBand().roll(random));
        double rolledEfficiency = efficiency != null ? efficiency : tier.efficiencyBand().roll(random);
        return new Stats(Stats.VERSION, tier, rolledRpm, rolledCapacity, rolledEfficiency);
    }
}
