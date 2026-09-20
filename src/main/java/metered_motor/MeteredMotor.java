package metered_motor;

import metered_motor.block.MotorBlocks;
import metered_motor.component.StatsCodec;
import metered_motor.debug.DebugCommand;
import metered_motor.menu.MotorMenus;
import metered_motor.model.Stats;
import metered_motor.trade.TradeRegistration;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The mod's server-and-common entrypoint: registers the stats component (MOTOR-REQ-001). */
public final class MeteredMotor implements ModInitializer {
    public static final String MOD_ID = "metered_motor";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /** The fixed tier stats every motor item and block entity carries (docs/spec/contracts/data-contract.md, ARCH-DEC-005, `decisions/DEC-009-fixed-tiers.md`). */
    public static final DataComponentType<Stats> STATS = Registry.register(
        BuiltInRegistries.DATA_COMPONENT_TYPE,
        id("stats"),
        DataComponentType.<Stats>builder()
            .persistent(StatsCodec.CODEC)
            .networkSynchronized(StatsCodec.STREAM_CODEC)
            .build());

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        TradeRegistration.register();
        MotorBlocks.register();
        MotorMenus.register();
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) DebugCommand.register();
        LOGGER.info("Metered Motor ready beside Create Fly");
    }
}
