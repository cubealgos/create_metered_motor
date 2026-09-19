package metered_motor.menu;

import com.zurrtum.create.foundation.gui.menu.MenuBase;
import com.zurrtum.create.foundation.gui.menu.MenuProvider;
import metered_motor.block.MeteredMotorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

/**
 * Opens {@link MeteredMotorMenu} for the motor at {@code pos} (docs/spec/domains/ui.md
 * UI-UC-001): writes the position into the buffer so the client's screen factory can find the
 * same client-side block entity Create Fly already keeps synced
 * ({@code AbstractSimiContainerScreen.getBlockEntity}), the way `create_brass_compass` reads its
 * own menu's payload back off the buffer.
 */
record MotorMenuProvider(BlockPos pos) implements MenuProvider {
    @Override
    public Component getDisplayName() {
        return Component.translatable("screen.metered_motor.title");
    }

    @Override
    public MenuBase<?> createMenu(int syncId, Inventory inventory, Player player, RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        if (!(player.level().getBlockEntity(pos) instanceof MeteredMotorBlockEntity motor)) {
            return null;
        }
        return new MeteredMotorMenu(syncId, inventory, motor);
    }
}
