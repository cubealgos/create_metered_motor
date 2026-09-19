package metered_motor.client;

import metered_motor.MeteredMotor;
import metered_motor.model.Stats;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.network.chat.Component;

/**
 * The rolled-stats tooltip (TRADE-REQ-005, UI-REQ-006): shown on any item stack carrying
 * {@link MeteredMotor#STATS}. The motor item itself does not exist in this ticket's branch (MM-3),
 * so this listens on every stack rather than the item's own {@code appendHoverText}; once the item
 * lands, its tooltip may move there and this callback stays a harmless no-op for stacks without the
 * component.
 *
 * <p>Efficiency and rate go through {@link StatsText}, the same formatting the motor screen (MM-6)
 * and, later, the goggles overlay use, so the two decimal places and the {@code Locale.ROOT}
 * formatting (`UI-REQ-007`) are written once.
 */
public final class MeteredMotorTooltip {
    private MeteredMotorTooltip() {
    }

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
            Stats stats = stack.get(MeteredMotor.STATS);
            if (stats == null) {
                return;
            }
            lines.add(Component.translatable("tooltip.metered_motor.tier", StatsText.tier(stats.tier())));
            lines.add(Component.translatable("tooltip.metered_motor.rpm", stats.rpm()));
            lines.add(Component.translatable("tooltip.metered_motor.capacity", stats.capacity()));
            lines.add(Component.translatable("tooltip.metered_motor.efficiency", StatsText.twoDecimals(stats.efficiency())));
            lines.add(Component.translatable("tooltip.metered_motor.rate", StatsText.twoDecimals(stats.ratePerMinute())));
        });
    }
}
