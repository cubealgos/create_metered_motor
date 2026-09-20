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
 * slots actually sit in, without a second copy of the numbers; they are re-exported from
 * {@link Layout} rather than declared here, since {@link Layout} carries no Minecraft imports and
 * so can be class-loaded by a plain unit test ({@code LayoutTest}), unlike this class (MM-17).
 */
public final class MeteredMotorMenu extends MenuBase<MeteredMotorBlockEntity> {
    /** The five emerald slots (MOTOR-REQ-008, MOTOR-REQ-009). */
    public static final int SLOTS = Layout.SLOTS;
    public static final int SLOT_SIZE = Layout.SLOT_SIZE;
    /** The screen's total width: `create_brass_compass`'s stock-keeper request atlas slice, narrowed to its opaque 208px span (`Layout`'s class doc, MM-17). */
    public static final int WIDTH = Layout.WIDTH;
    /** The readout panel's total height: the title strip, the body tiled to fit the nine readout lines and the slot row, and the bottom band (`Layout`). */
    public static final int TOP_HEIGHT = Layout.TOP_HEIGHT;
    /** The gap between the readout panel and Create's player-inventory frame, as every `AbstractSimiContainerScreen` leaves. */
    public static final int GAP = Layout.GAP;
    public static final int SLOT_X = Layout.SLOT_X;
    public static final int SLOT_Y = Layout.SLOT_Y;
    /** The standard vanilla player-inventory width, centred under {@link #WIDTH}. */
    public static final int PLAYER_INVENTORY_WIDTH = Layout.PLAYER_INVENTORY_WIDTH;
    public static final int PLAYER_INV_X = Layout.PLAYER_INV_X;
    public static final int MAIN_INV_Y = Layout.MAIN_INV_Y;

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
