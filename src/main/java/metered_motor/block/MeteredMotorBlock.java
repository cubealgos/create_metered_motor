package metered_motor.block;

import com.zurrtum.create.content.kinetics.base.DirectionalKineticBlock;
import com.zurrtum.create.foundation.block.IBE;
import metered_motor.MeteredMotor;
import metered_motor.model.Stats;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The metered motor block: a Create directional kinetic source, placed and shafted like the
 * creative motor, that refuses placement of a stats component newer than this build can read
 * (MOTOR-REQ-001, MOTOR-REQ-002, MOTOR-REQ-014, ARCH-DEC-002).
 */
public final class MeteredMotorBlock extends DirectionalKineticBlock implements IBE<MeteredMotorBlockEntity> {
    public MeteredMotorBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Delegates to {@link DirectionalKineticBlock}'s placement (the creative motor's own, unchanged)
     * unless the held item's stats are a version newer than this build, in which case placement is
     * refused by returning {@code null}: {@code BlockItem.place} then fails and keeps the item and
     * its component untouched (MOTOR-REQ-014, DATA-REQ-001).
     */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Stats heldStats = context.getItemInHand().get(MeteredMotor.STATS);
        if (heldStats != null && heldStats.readOnly()) {
            return null;
        }
        return super.getStateForPlacement(context);
    }

    @Override
    public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
        return state.getValue(FACING) == face;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public Class<MeteredMotorBlockEntity> getBlockEntityClass() {
        return MeteredMotorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MeteredMotorBlockEntity> getBlockEntityType() {
        return MotorBlocks.BLOCK_ENTITY_TYPE;
    }
}
