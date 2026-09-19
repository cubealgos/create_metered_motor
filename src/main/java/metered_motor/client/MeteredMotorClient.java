package metered_motor.client;

import metered_motor.client.visual.MotorVisuals;
import net.fabricmc.api.ClientModInitializer;

/** The client entrypoint: registers the rolled-stats tooltip (TRADE-REQ-005) and the goggles overlay and shaft visual (MOTOR-REQ-012, MOTOR-REQ-013). */
public final class MeteredMotorClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MeteredMotorTooltip.register();
        MotorVisuals.register();
    }
}
