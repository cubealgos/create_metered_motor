package metered_motor.trade;

import metered_motor.MeteredMotor;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * Registers the trade path's loot condition type: {@code metered_motor:no_motor_offered}
 * (ARCH-DEC-004, TRADE-REQ-006). Called once from {@link MeteredMotor#onInitialize()}.
 *
 * <p>MM-21 (`decisions/DEC-009-fixed-tiers.md`) withdrew {@code metered_motor:roll}: a trade
 * file's {@code gives} item template now carries the tier's stats component directly, fixed at
 * authoring time (26.2's {@code ItemStackTemplate} codec accepts a {@code components} map,
 * confirmed by full bytecode disassembly of its {@code MAP_CODEC}), so there is nothing left to
 * roll and no loot function type left to register.
 */
public final class TradeRegistration {
    private TradeRegistration() {
    }

    public static void register() {
        Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, MeteredMotor.id("no_motor_offered"), NoMotorOffered.MAP_CODEC);
    }
}
