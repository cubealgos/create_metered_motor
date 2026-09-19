package metered_motor.trade;

import metered_motor.MeteredMotor;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * Registers the trade path's loot types: {@code metered_motor:roll} and
 * {@code metered_motor:no_motor_offered} (ARCH-DEC-004, TRADE-REQ-002, TRADE-REQ-006). Called once
 * from {@link MeteredMotor#onInitialize()}.
 */
public final class TradeRegistration {
    private TradeRegistration() {
    }

    public static void register() {
        Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, MeteredMotor.id("roll"), RollFunction.MAP_CODEC);
        Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, MeteredMotor.id("no_motor_offered"), NoMotorOffered.MAP_CODEC);
    }
}
