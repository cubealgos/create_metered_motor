package metered_motor.client;

import metered_motor.client.screen.MotorScreens;
import net.fabricmc.api.ClientModInitializer;

/** The client entrypoint: registers the rolled-stats tooltip (TRADE-REQ-005) and the motor screen (MM-6). */
public final class MeteredMotorClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MeteredMotorTooltip.register();
        MotorScreens.register();
    }
}
