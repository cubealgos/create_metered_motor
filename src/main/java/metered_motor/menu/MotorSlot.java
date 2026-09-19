package metered_motor.menu;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * One of the motor's five slots: a plain vanilla slot over the block entity's own {@link Container}
 * whose {@link #mayPlace} delegates to {@link Container#canPlaceItem}, so a click here refuses
 * exactly what a hopper would (MOTOR-REQ-008, docs/spec/domains/ui.md UI-REQ-002).
 */
final class MotorSlot extends Slot {
    MotorSlot(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return container.canPlaceItem(getContainerSlot(), stack);
    }
}
