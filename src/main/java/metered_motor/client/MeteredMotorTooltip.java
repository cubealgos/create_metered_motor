package metered_motor.client;

import metered_motor.MeteredMotor;
import metered_motor.block.MotorBlocks;
import metered_motor.model.Stats;
import metered_motor.model.Tier;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.network.chat.Component;

/**
 * The rolled-stats tooltip (TRADE-REQ-005, UI-REQ-006): shown on any item stack carrying
 * {@link MeteredMotor#STATS}, or "unrolled" for a motor item with no roll (MOTOR-FAIL-003). The
 * motor item itself does not exist in this ticket's branch (MM-3), so this listens on every stack
 * rather than the item's own {@code appendHoverText}; once the item lands, its tooltip may move
 * there and this callback stays a harmless no-op for stacks without the component.
 */
public final class MeteredMotorTooltip {
    private MeteredMotorTooltip() {
    }

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
            Stats stats = stack.get(MeteredMotor.STATS);
            if (stats == null) {
                if (stack.is(MotorBlocks.ITEM)) {
                    lines.add(Component.translatable("tooltip.metered_motor.tier", Tier.I.name()));
                    lines.add(Component.translatable("tooltip.metered_motor.unrolled"));
                }
                return;
            }
            lines.add(Component.translatable("tooltip.metered_motor.tier", stats.tier().name()));
            lines.add(Component.translatable("tooltip.metered_motor.rpm", stats.rpm()));
            lines.add(Component.translatable("tooltip.metered_motor.capacity", stats.capacity()));
            lines.add(Component.translatable("tooltip.metered_motor.efficiency", StatsText.decimal(stats.efficiency())));
            lines.add(Component.translatable("tooltip.metered_motor.rate", StatsText.decimal(stats.ratePerMinute())));
        });
    }
}
