package metered_motor.client.visual;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.OrientedRotatingVisual;
import com.zurrtum.create.client.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import metered_motor.block.MotorBlocks;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

/**
 * The motor's client-only registrations: the goggles overlay and the shaft's rendering, both keyed
 * off {@link MotorBlocks#BLOCK_ENTITY_TYPE} (MOTOR-REQ-012, MOTOR-REQ-013). Called once from
 * {@code MeteredMotorClient.onInitializeClient}.
 *
 * <p>The shaft is registered exactly as Create Fly registers its own creative motor's: a plain
 * {@link MeteredMotorRenderer} as the fallback for a Flywheel-less client, and Flywheel's own
 * {@link OrientedRotatingVisual#of} for the half-shaft partial, oriented from {@code south} to the
 * block's {@code facing} value, replacing the fallback's draw whenever Flywheel is active
 * (docs/spec/README.md Verification 8; read from the jar at this ticket, {@code
 * AllBlockEntityRenders.register()}, the {@code AllBlockEntityTypes.MOTOR} entry). The block's own
 * model carries no shaft geometry, matching the creative motor's {@code block.json}, so nothing
 * doubles up.
 */
public final class MotorVisuals {
    private MotorVisuals() {
    }

    public static void register() {
        BlockEntityRenderers.register(MotorBlocks.BLOCK_ENTITY_TYPE, MeteredMotorRenderer::new);
        SimpleBlockEntityVisualizer.builder(MotorBlocks.BLOCK_ENTITY_TYPE)
            .factory(OrientedRotatingVisual.of(AllPartialModels.SHAFT_HALF))
            .skipVanillaRender(blockEntity -> true)
            .apply();
        BlockEntityBehaviour.addClient(MotorBlocks.BLOCK_ENTITY_TYPE, MotorTooltipBehaviour::new);
    }
}
