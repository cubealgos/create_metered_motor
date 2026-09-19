package metered_motor.client.screen;

import com.zurrtum.create.client.AllMenuScreens;
import metered_motor.menu.MotorMenus;

/** Registers {@link MeteredMotorScreen} for {@link MotorMenus#MENU_TYPE} (docs/spec/domains/ui.md UI-UC-001). */
public final class MotorScreens {
    private MotorScreens() {
    }

    public static void register() {
        AllMenuScreens.register(MotorMenus.MENU_TYPE, MeteredMotorScreen::create);
    }
}
