package metered_motor.block;

import com.zurrtum.create.content.kinetics.base.DirectionalKineticBlock;
import com.zurrtum.create.foundation.block.IBE;
import metered_motor.MeteredMotor;
import metered_motor.model.Stats;
import metered_motor.model.Tier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * The metered motor block: a Create directional kinetic source, placed and shafted like the
 * creative motor, that refuses placement of a stats component newer than this build can read
 * (MOTOR-REQ-001, MOTOR-REQ-002, MOTOR-REQ-014, ARCH-DEC-002).
 */
public final class MeteredMotorBlock extends DirectionalKineticBlock implements IBE<MeteredMotorBlockEntity> {
    /** The rolled tier, set once at placement, that selects the block's model set (MOTOR-REQ-013, MOTOR-DEC-004). */
    public static final EnumProperty<MotorTier> TIER = EnumProperty.create("tier", MotorTier.class);

    public MeteredMotorBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TIER);
    }

    /**
     * Delegates to {@link DirectionalKineticBlock}'s placement (the creative motor's own, unchanged)
     * unless the held item's stats are a version newer than this build, in which case placement is
     * refused by returning {@code null}: {@code BlockItem.place} then fails and keeps the item and
     * its component untouched (MOTOR-REQ-014, DATA-REQ-001). Otherwise sets {@link #TIER} from the
     * held stats' tier, or tier I for an unrolled item (MOTOR-FAIL-003), so the blockstate JSON picks
     * the model set without any custom renderer for the casing.
     */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Stats heldStats = context.getItemInHand().get(MeteredMotor.STATS);
        if (heldStats != null && heldStats.readOnly()) {
            return null;
        }
        BlockState placed = super.getStateForPlacement(context);
        if (placed == null) {
            return null;
        }
        Tier tier = heldStats != null ? heldStats.tier() : Tier.I;
        return placed.setValue(TIER, MotorTier.of(tier));
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
