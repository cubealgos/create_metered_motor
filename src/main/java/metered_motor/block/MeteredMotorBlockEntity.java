package metered_motor.block;

import com.zurrtum.create.content.kinetics.base.GeneratingKineticBlockEntity;
import metered_motor.MeteredMotor;
import metered_motor.component.StatsCodec;
import metered_motor.model.Stats;
import metered_motor.model.Tier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * The motor's kinetic source of truth while placed: the rolled {@link Stats}, the {@link MotorState}
 * state machine and the generated speed and stress capacity Create's network reads while running,
 * zero otherwise (MOTOR-REQ-002, MOTOR-REQ-003, MOTOR-REQ-004, MOTOR-REQ-010, MOTOR-REQ-011,
 * MOTOR-FAIL-003, DATA-REQ-004).
 */
public final class MeteredMotorBlockEntity extends GeneratingKineticBlockEntity {
    private Stats stats = Stats.middleOf(Tier.I);
    private MotorState state = MotorState.STOPPED;
    /**
     * Placeholder for MM-4's real inventory check: {@code true} once the (not yet existing)
     * inventory holds an emerald to burn. Until MM-4 lands, nothing but a test sets this; the tick
     * below already treats it as the fuel edge of the state machine so MM-4 only has to wire the
     * real check in.
     */
    private boolean hasFuel = false;

    public MeteredMotorBlockEntity(BlockPos pos, BlockState state) {
        super(MotorBlocks.BLOCK_ENTITY_TYPE, pos, state);
    }

    @Override
    public void initialize() {
        super.initialize();
        // The restored state may already call for rotation (a running motor saved and reloaded).
        updateGeneratedRotation();
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide()) {
            return;
        }
        MotorState derived = level.hasNeighborSignal(worldPosition) ? MotorState.PAUSED
            : hasFuel ? MotorState.RUNNING : MotorState.STOPPED;
        if (derived != state) {
            state = derived;
            // MOTOR-REQ-004, MOTOR-REQ-010: updateGeneratedRotation() drives both directions —
            // starting attaches a network and reports the added capacity; stopping detaches from
            // it entirely (GeneratingKineticBlockEntity.applyNewSpeed), which is what actually
            // removes our contribution, so no separate capacity notification is needed or safe
            // here (the network may not exist yet on the tick a stop follows a start).
            updateGeneratedRotation();
        }
    }

    /** The rolled rpm while running, zero otherwise (MOTOR-REQ-004). */
    @Override
    public float getGeneratedSpeed() {
        return state == MotorState.RUNNING ? stats.rpm() : 0;
    }

    /** The rolled stress capacity while running, zero otherwise (MOTOR-REQ-004). */
    @Override
    public float calculateAddedStressCapacity() {
        return state == MotorState.RUNNING ? stats.capacity() : 0;
    }

    /** The stats this block entity carries, copied from the placed item and back on breaking (MOTOR-REQ-003). */
    public Stats stats() {
        return stats;
    }

    /** The motor's current state (docs/spec/domains/motor.md §3). */
    public MotorState state() {
        return state;
    }

    /** Whether the (future) inventory holds fuel; see the field's placeholder note. */
    public boolean hasFuel() {
        return hasFuel;
    }

    /** Forces the fuel flag; MM-4 replaces every caller of this with the real inventory check. */
    public void setHasFuel(boolean hasFuel) {
        this.hasFuel = hasFuel;
    }

    /** Copies the placed item's stats into the block entity (MOTOR-REQ-003, DATA-REQ-004). */
    @Override
    protected void applyImplicitComponents(DataComponentGetter componentGetter) {
        super.applyImplicitComponents(componentGetter);
        stats = componentGetter.getOrDefault(MeteredMotor.STATS, Stats.middleOf(Tier.I));
    }

    /** Writes the block entity's stats back onto the broken item (MOTOR-REQ-003, DATA-REQ-004). */
    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(MeteredMotor.STATS, stats);
    }

    @Override
    protected void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);
        output.store("Stats", StatsCodec.CODEC, stats);
        output.putString("State", state.name());
        output.putBoolean("HasFuel", hasFuel);
    }

    @Override
    protected void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        stats = input.read("Stats", StatsCodec.CODEC).orElseGet(() -> Stats.middleOf(Tier.I));
        String saved = input.getStringOr("State", MotorState.STOPPED.name());
        state = java.util.Arrays.stream(MotorState.values()).filter(m -> m.name().equals(saved)).findFirst().orElse(MotorState.STOPPED);
        hasFuel = input.getBooleanOr("HasFuel", false);
    }
}
