package metered_motor.menu;

import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.foundation.gui.menu.MenuProvider;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import metered_motor.MeteredMotor;
import metered_motor.block.MeteredMotorBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;

/**
 * The motor menu's registration (docs/spec/domains/ui.md `UI-DEC-001`) and the one entry point
 * {@link metered_motor.block.MeteredMotorBlock}'s right-click needs, so the block stays a
 * one-method addition to this ticket.
 */
public final class MotorMenus {
    /** In {@code CreateRegistries.MENU_TYPE}, so Create Fly's own screen framework draws it (`ARCH-DEC-002`, `SURFACE`: not public). */
    public static final MenuType<MeteredMotorBlockEntity> MENU_TYPE = Registry.register(
        CreateRegistries.MENU_TYPE, MeteredMotor.id("motor"),
        (MenuType<MeteredMotorBlockEntity>) (syncId, inventory, motor) -> new MeteredMotorMenu(syncId, inventory, motor));

    private MotorMenus() {
    }

    /** Forces the registration above to run; call once from {@link MeteredMotor#onInitialize()}. */
    public static void register() {
    }

    /** Opens the motor screen for a player who right-clicked the block, not sneaking (UI-UC-001). */
    public static void open(ServerPlayer player, MeteredMotorBlockEntity motor) {
        MenuProvider.openHandledScreen(player, new MotorMenuProvider(motor.getBlockPos()));
    }
}
