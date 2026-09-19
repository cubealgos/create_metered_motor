package metered_motor.client;

import java.util.ArrayList;
import java.util.List;
import metered_motor.MeteredMotor;
import metered_motor.block.MeteredMotorBlock;
import metered_motor.block.MotorBlocks;
import metered_motor.model.Stats;
import metered_motor.model.Tier;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * The rolled-stats tooltip (TRADE-REQ-005, UI-REQ-006): shown on any item stack carrying
 * {@link MeteredMotor#STATS}, or "unrolled" for a motor item with no roll (MOTOR-FAIL-003). A
 * component newer than this build reads back intact and {@link Stats#readOnly()}
 * (MOTOR-REQ-014, DATA-REQ-003); the tooltip then shows "Unknown motor (newer version)" and no
 * stat lines, since printing a read-only component's numbers as if they were rolled by this
 * build would misreport them. {@link MeteredMotorBlock#getStateForPlacement} already refuses to
 * place such an item, so the block, the screen and the goggles overlay never see one — this
 * tooltip is the only place a read-only component is ever shown to a player. The motor item
 * itself does not exist in this ticket's branch (MM-3), so this listens on every stack rather
 * than the item's own {@code appendHoverText}; once the item lands, its tooltip may move there
 * and this callback stays a harmless no-op for stacks without the component.
 *
 * <p>Every number and enum name goes through {@link StatsText}, the same formatting the motor
 * screen (MM-6) and the goggles overlay (MM-7) use, so the two decimal places and the
 * {@code Locale.ROOT} formatting (`UI-REQ-007`) are written once.
 */
public final class MeteredMotorTooltip {
    private MeteredMotorTooltip() {
    }

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, flag, tooltipLines) -> tooltipLines.addAll(lines(stack)));
    }

    /**
     * The tooltip lines for a stack, pure of the Fabric callback so a game test can call it
     * directly without registering the client-only event (`UI-REQ-006`, `MOTOR-REQ-014`,
     * `DATA-REQ-003`): the rolled stats; "Unknown motor (newer version)" and no stat lines for a
     * read-only component; "Unrolled" for a motor item with no component at all
     * (MOTOR-FAIL-003); empty for any other item.
     */
    public static List<Component> lines(ItemStack stack) {
        List<Component> result = new ArrayList<>();
        Stats stats = stack.get(MeteredMotor.STATS);
        if (stats == null) {
            if (stack.is(MotorBlocks.ITEM)) {
                result.add(Component.translatable("tooltip.metered_motor.tier", StatsText.tier(Tier.I)));
                result.add(Component.translatable("tooltip.metered_motor.unrolled"));
            }
            return result;
        }
        if (stats.readOnly()) {
            result.add(Component.translatable("tooltip.metered_motor.unknown_version"));
            return result;
        }
        result.add(Component.translatable("tooltip.metered_motor.tier", StatsText.tier(stats.tier())));
        result.add(Component.translatable("tooltip.metered_motor.rpm", stats.rpm()));
        result.add(Component.translatable("tooltip.metered_motor.capacity", stats.capacity()));
        result.add(Component.translatable("tooltip.metered_motor.efficiency", StatsText.twoDecimals(stats.efficiency())));
        result.add(Component.translatable("tooltip.metered_motor.rate", StatsText.twoDecimals(stats.ratePerMinute())));
        return result;
    }
}
