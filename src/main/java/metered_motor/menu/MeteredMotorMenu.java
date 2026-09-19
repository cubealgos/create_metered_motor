package metered_motor.menu;

import com.zurrtum.create.foundation.gui.menu.MenuBase;
import metered_motor.block.MeteredMotorBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * The motor screen's menu (docs/spec/domains/ui.md UI-UC-001, `UI-REQ-001`, `UI-REQ-002`,
 * `UI-REQ-008`): five real {@link MotorSlot}s wrapping the block entity's own
 * {@code WorldlyContainer} directly, so the same {@code canPlaceItem} rule a hopper obeys governs
 * every click here too, plus the player's own inventory. The layout constants are {@code public}
 * so {@code MeteredMotorScreen} (a different package, client-only) draws the same frame the
 * slots actually sit in, without a second copy of the numbers.
 */
public final class MeteredMotorMenu extends MenuBase<MeteredMotorBlockEntity> {
    /** The five emerald slots (MOTOR-REQ-008, MOTOR-REQ-009). */
    public static final int SLOTS = 5;
    public static final int SLOT_SIZE = 18;
    /** The screen's total width: `create_brass_compass`'s stock-keeper request atlas slice, reused at its proven 224px (the ticket's Constraints section). */
    public static final int WIDTH = 224;
    /** The readout panel's total height: an 18px header, seven 20px tiled body strips and a 12px bottom band. */
    public static final int TOP_HEIGHT = 18 + 7 * 20 + 12;
    /** The gap between the readout panel and Create's player-inventory frame, as every `AbstractSimiContainerScreen` leaves. */
    public static final int GAP = 4;
    public static final int SLOT_X = (WIDTH - SLOTS * SLOT_SIZE) / 2;
    public static final int SLOT_Y = TOP_HEIGHT - 36;
    /** The standard vanilla player-inventory width, centred under {@link #WIDTH}. */
    public static final int PLAYER_INVENTORY_WIDTH = 176;
    public static final int PLAYER_INV_X = (WIDTH - PLAYER_INVENTORY_WIDTH) / 2;
    public static final int MAIN_INV_Y = TOP_HEIGHT + GAP + 18;

    public MeteredMotorMenu(int syncId, Inventory inventory, MeteredMotorBlockEntity motor) {
        super(MotorMenus.MENU_TYPE, syncId, inventory, motor);
    }

    @Override
    protected void initAndReadInventory(MeteredMotorBlockEntity motor) {
    }

    @Override
    protected void addSlots() {
        for (int i = 0; i < SLOTS; i++) {
            addSlot(new MotorSlot(contentHolder, i, SLOT_X + i * SLOT_SIZE, SLOT_Y));
        }
        addPlayerSlots(PLAYER_INV_X, MAIN_INV_Y);
    }

    @Override
    protected void saveData(MeteredMotorBlockEntity motor) {
    }

    @Override
    public boolean stillValid(Player player) {
        return contentHolder != null && contentHolder.stillValid(player);
    }

    /**
     * Shift-click: a motor slot empties into the player's inventory; the player's inventory only
     * offers emeralds and emerald blocks into the motor's slots, refused the same way a direct
     * click is, because {@link MotorSlot#mayPlace} still runs inside {@code moveItemStackTo}
     * (`UI-REQ-002`).
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stackInSlot = slot.getItem();
        ItemStack copy = stackInSlot.copy();
        boolean moved = index < SLOTS
            ? moveItemStackTo(stackInSlot, SLOTS, slots.size(), true)
            : moveItemStackTo(stackInSlot, 0, SLOTS, false);
        if (!moved) {
            return ItemStack.EMPTY;
        }
        if (stackInSlot.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stackInSlot.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stackInSlot);
        return copy;
    }
}
