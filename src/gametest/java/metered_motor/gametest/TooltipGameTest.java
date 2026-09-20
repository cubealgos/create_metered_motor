package metered_motor.gametest;

import java.util.List;
import metered_motor.MeteredMotor;
import metered_motor.block.MotorBlocks;
import metered_motor.client.MeteredMotorTooltip;
import metered_motor.model.Stats;
import metered_motor.model.Tier;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;

/**
 * MM-14: the tooltip shows an unknown motor, not stat numbers, for a component newer than this
 * build; the tier's fixed stats for a current-version component; and "unrolled" for no component
 * at all (MOTOR-REQ-014, UI-REQ-006, DATA-REQ-003). MM-21 dropped the efficiency line
 * (`decisions/DEC-009-fixed-tiers.md`). {@link MeteredMotorTooltip#lines(ItemStack)} is pure of
 * the client-only Fabric callback, so this dedicated-server game test calls it directly.
 */
public final class TooltipGameTest {
    @GameTest
    public void aNewerVersionShowsAsUnknownWithNoStatLines(GameTestHelper helper) {
        Stats future = new Stats(Stats.VERSION + 1, Tier.I);
        ItemStack stack = new ItemStack(MotorBlocks.ITEM);
        stack.set(MeteredMotor.STATS, future);

        List<Component> lines = MeteredMotorTooltip.lines(stack);
        helper.assertTrue(lines.size() == 1, "exactly one line for a read-only component: " + lines);
        helper.assertTrue(key(lines.get(0)).equals("tooltip.metered_motor.unknown_version"), "the unknown-version key: " + lines);
        helper.succeed();
    }

    @GameTest
    public void aCurrentVersionShowsTheStatLines(GameTestHelper helper) {
        Stats stats = new Stats(Stats.VERSION, Tier.II);
        ItemStack stack = new ItemStack(MotorBlocks.ITEM);
        stack.set(MeteredMotor.STATS, stats);

        List<Component> lines = MeteredMotorTooltip.lines(stack);
        helper.assertTrue(lines.size() == 4, "tier, rpm, capacity and rate, no efficiency: " + lines);
        helper.assertTrue(key(lines.get(0)).equals("tooltip.metered_motor.tier"), "the tier line: " + lines);
        helper.assertTrue(key(lines.get(3)).equals("tooltip.metered_motor.rate"), "the rate line: " + lines);
        helper.succeed();
    }

    @GameTest
    public void noComponentShowsTheUnrolledLine(GameTestHelper helper) {
        ItemStack stack = new ItemStack(MotorBlocks.ITEM);

        List<Component> lines = MeteredMotorTooltip.lines(stack);
        helper.assertTrue(lines.size() == 2, "tier and unrolled: " + lines);
        helper.assertTrue(key(lines.get(1)).equals("tooltip.metered_motor.unrolled"), "the unrolled key: " + lines);
        helper.succeed();
    }

    private static String key(Component component) {
        return component.getContents() instanceof TranslatableContents t ? t.getKey() : component.getString();
    }
}
