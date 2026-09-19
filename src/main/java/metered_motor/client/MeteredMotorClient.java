package metered_motor.client;

import net.fabricmc.api.ClientModInitializer;

/** The client entrypoint: registers the rolled-stats tooltip (TRADE-REQ-005). */
public final class MeteredMotorClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MeteredMotorTooltip.register();
    }
}
