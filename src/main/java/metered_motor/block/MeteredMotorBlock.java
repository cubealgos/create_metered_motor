package metered_motor.block;

import com.zurrtum.create.AllShapes;
import com.zurrtum.create.content.kinetics.base.DirectionalKineticBlock;
import com.zurrtum.create.foundation.block.IBE;
import metered_motor.MeteredMotor;
import metered_motor.menu.MotorMenus;
import metered_motor.model.Stats;
import metered_motor.model.Tier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The metered motor block: a Create directional kinetic source, placed and shafted like the
 * creative motor, that refuses placement of a stats component newer than this build can read
 * (MOTOR-REQ-001, MOTOR-REQ-002, MOTOR-REQ-014, ARCH-DEC-002).
 */
public final class MeteredMotorBlock extends DirectionalKineticBlock implements IBE<MeteredMotorBlockEntity> {
    /** The tier, set once at placement, that selects the block's model set (MOTOR-REQ-013, MOTOR-DEC-004). */
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

    /**
     * The casing's actual (non-full-cube) silhouette, exactly {@code CreativeMotorBlock}'s own
     * {@code AllShapes.MOTOR_BLOCK.get(facing)} (MM-16). Without this override the block falls
     * back to {@link Block}'s default full-cube shape, which Minecraft also uses (absent a
     * dynamic shape) to precompute the block's per-facing *occlusion* shape — so neighbouring
     * blocks wrongly believed the motor fully covered their touching face and culled it, even
     * though the rendered casing (block.json, copied from Create's own) never filled that face,
     * leaving the neighbour's face missing (see through to whatever is beyond it).
     */
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AllShapes.MOTOR_BLOCK.get(state.getValue(FACING));
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

    /**
     * Right-click, not sneaking, opens the motor screen (docs/spec/domains/ui.md `UI-UC-001`);
     * sneaking falls through so a block in hand can still be placed against it.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        withBlockEntityDo(level, pos, motor -> MotorMenus.open((ServerPlayer) player, motor));
        return InteractionResult.SUCCESS;
    }
}
