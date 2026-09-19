package metered_motor.client.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.content.kinetics.base.SingleKineticRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import metered_motor.block.MeteredMotorBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

/**
 * The fallback renderer for a Flywheel-less client: draws the shaft turning on the block's facing
 * side, mirroring Create Fly's own {@code CreativeMotorRenderer} shape (the half-shaft partial,
 * tinted, rotated without the block entity's own offset) so the two never drift apart
 * (MOTOR-REQ-013, docs/spec/README.md Verification 8). {@link MotorVisuals} registers the
 * Flywheel visual that replaces this renderer's shaft draw whenever Flywheel is active.
 */
public final class MeteredMotorRenderer implements BlockEntityRenderer<MeteredMotorBlockEntity, SingleKineticRenderState> {
    public MeteredMotorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public SingleKineticRenderState createRenderState() {
        return new SingleKineticRenderState();
    }

    @Override
    public void extractRenderState(MeteredMotorBlockEntity blockEntity, SingleKineticRenderState state, float partialTick,
        Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        Level level = SmartBlockEntityRenderer.extractBase(blockEntity, state, crumblingOverlay);
        // BlockEntityRenderState.blockState is private to net.minecraft's own package at our
        // compile-time classpath (Create Fly's access widener only applies to its own build), so
        // this reads the state straight from the block entity instead (equally correct: extractBase
        // above runs synchronously on the same tick).
        BlockState blockState = blockEntity.getBlockState();
        Direction facing = blockState.getValue(BlockStateProperties.FACING);
        state.model = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, blockState, facing)
            .cardinalLighting(level)
            .light(state.lightCoords)
            .color(KineticBlockEntityRenderer.getTintColor(blockEntity))
            .extractRenderState();
        state.angle = KineticBlockEntityRenderer.getRotateAngleWithoutBeOffset(facing.getAxis(), blockEntity, state, level);
    }

    @Override
    public void submit(SingleKineticRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        state.submit(poseStack, collector);
    }
}
