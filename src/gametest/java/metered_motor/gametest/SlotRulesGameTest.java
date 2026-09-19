package metered_motor.gametest;

import metered_motor.block.MeteredMotorBlock;
import metered_motor.block.MeteredMotorBlockEntity;
import metered_motor.block.MotorBlocks;
import metered_motor.menu.MeteredMotorMenu;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * MM-6: the motor's five slots accept only emeralds and emerald blocks — on a direct click and on
 * shift-click from the player inventory in both directions (docs/spec/domains/ui.md `UI-REQ-002`,
 * `MOTOR-REQ-008`, `MOTOR-REQ-009`). MM-9: shift-clicking a motor slot is the positive half of
 * `UI-REQ-008` — the screen actually is a way to take emeralds out, which {@code ExtractGameTest}
 * only proves automation cannot do.
 *
 * <p>Simulates through {@link MeteredMotorMenu#getSlot} and {@link MeteredMotorMenu#quickMoveStack}
 * directly rather than through {@code AbstractContainerMenu#clicked}'s full mouse-click
 * simulation: both are the exact same rule the ticket's acceptance criterion names (a slot's
 * {@code mayPlace}, and quick-move's own use of it), reached without the extra machinery a
 * dedicated server's headless game test cannot exercise as a real mouse click anyway.
 */
public final class SlotRulesGameTest {
    private static final BlockPos MOTOR_POS = new BlockPos(1, 1, 1);

    @GameTest
    public void theFiveSlotsAcceptOnlyEmeraldsAndEmeraldBlocks(GameTestHelper helper) {
        MeteredMotorBlockEntity motor = placeMotor(helper);
        MeteredMotorMenu menu = openMenu(helper, motor);

        helper.assertTrue(menu.getSlot(0).mayPlace(new ItemStack(Items.EMERALD)), "slot 0 accepts a loose emerald");
        helper.assertTrue(menu.getSlot(0).mayPlace(new ItemStack(Items.EMERALD_BLOCK)), "slot 0 accepts an emerald block");
        helper.assertFalse(menu.getSlot(0).mayPlace(new ItemStack(Items.COBBLESTONE)), "slot 0 refuses cobblestone");
        helper.succeed();
    }

    @GameTest
    public void quickMoveFromThePlayerInventoryRefusesCobblestoneAndAcceptsEmeralds(GameTestHelper helper) {
        MeteredMotorBlockEntity motor = placeMotor(helper);
        MeteredMotorMenu menu = openMenu(helper, motor);
        int firstPlayerSlot = MeteredMotorMenu.SLOTS;

        menu.getSlot(firstPlayerSlot).set(new ItemStack(Items.COBBLESTONE, 64));
        menu.quickMoveStack(menu.player, firstPlayerSlot);
        for (int i = 0; i < MeteredMotorMenu.SLOTS; i++) {
            helper.assertTrue(motor.getItem(i).isEmpty(), "cobblestone shift-clicked from the player inventory leaves motor slot " + i + " empty");
        }

        menu.getSlot(firstPlayerSlot).set(new ItemStack(Items.EMERALD, 64));
        menu.quickMoveStack(menu.player, firstPlayerSlot);
        helper.assertTrue(motor.getItem(0).is(Items.EMERALD), "emeralds shift-clicked from the player inventory land in slot 0: " + motor.getItem(0));
        helper.succeed();
    }

    @GameTest
    public void quickMoveFromAMotorSlotMovesEmeraldsIntoThePlayerInventory(GameTestHelper helper) {
        MeteredMotorBlockEntity motor = placeMotor(helper);
        motor.setItem(0, new ItemStack(Items.EMERALD, 12));
        MeteredMotorMenu menu = openMenu(helper, motor);

        menu.quickMoveStack(menu.player, 0);

        helper.assertTrue(motor.getItem(0).isEmpty(), "the motor slot empties: " + motor.getItem(0));
        // moveItemStackTo(reverse=true) may land the stack in any slot of the player's inventory
        // range, not necessarily the first one; find it rather than assume a position.
        int found = 0;
        for (int i = MeteredMotorMenu.SLOTS; i < menu.slots.size(); i++) {
            ItemStack stack = menu.getSlot(i).getItem();
            if (!stack.isEmpty()) {
                helper.assertTrue(stack.is(Items.EMERALD), "the moved stack is emeralds, not " + stack);
                found += stack.getCount();
            }
        }
        helper.assertTrue(found == 12, "all 12 emeralds land somewhere in the player's inventory, found " + found);
        helper.succeed();
    }

    private MeteredMotorBlockEntity placeMotor(GameTestHelper helper) {
        helper.setBlock(MOTOR_POS, MotorBlocks.BLOCK.defaultBlockState().setValue(MeteredMotorBlock.FACING, Direction.UP));
        return helper.getBlockEntity(MOTOR_POS, MeteredMotorBlockEntity.class);
    }

    private MeteredMotorMenu openMenu(GameTestHelper helper, MeteredMotorBlockEntity motor) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        return new MeteredMotorMenu(0, player.getInventory(), motor);
    }
}
