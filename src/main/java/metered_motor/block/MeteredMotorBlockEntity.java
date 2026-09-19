package metered_motor.block;

import com.zurrtum.create.content.kinetics.base.GeneratingKineticBlockEntity;
import metered_motor.MeteredMotor;
import metered_motor.component.StatsCodec;
import metered_motor.model.Meter;
import metered_motor.model.Stats;
import metered_motor.model.Tier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * The motor's kinetic source of truth while placed: the rolled {@link Stats}, the {@link MotorState}
 * state machine, the five-slot emerald inventory and the once-a-second {@link Meter} that burns it
 * in proportion to the network's load, and the generated speed and stress capacity Create's network
 * reads while running, zero otherwise (MOTOR-REQ-002, MOTOR-REQ-003, MOTOR-REQ-004, MOTOR-REQ-006,
 * MOTOR-REQ-007, MOTOR-REQ-008, MOTOR-REQ-009, MOTOR-REQ-010, MOTOR-REQ-011, MOTOR-FAIL-002,
 * MOTOR-FAIL-004, ARCH-DEC-003, DATA-REQ-004).
 *
 * <p>Breaking the block needs no extra code to drop the inventory: {@code BlockEntity}'s own
 * {@code preRemoveSideEffects} already drops any {@link Container} it finds {@code this} to be,
 * reached through {@code SmartBlockEntity.preRemoveSideEffects} calling {@code super} before its
 * own {@code destroy()} (read in the Create Fly and merged-deobf jars at this ticket); implementing
 * {@link WorldlyContainer} is the whole of MOTOR-REQ-003's inventory-drop half.
 *
 * <p>The network's load is read the way Create's own stress gauge reads it
 * ({@code StressGaugeBlockEntity.getNetworkStress()}/{@code getNetworkCapacity()}, docs/spec/README.md
 * Verification 3): both simply return the {@code stress}/{@code capacity} fields {@code KineticBlockEntity}
 * carries, which {@code KineticNetwork.updateFromNetwork} pushes to every member through
 * {@code updateFromNetwork(float, float, int)} whenever the network's totals change. This class reads
 * those same inherited protected fields directly, through {@link #networkStress()} and
 * {@link #networkCapacity()}, rather than adding a second path.
 */
public final class MeteredMotorBlockEntity extends GeneratingKineticBlockEntity implements WorldlyContainer {
    /** The five slots, all reachable from every face (MOTOR-REQ-008, MOTOR-REQ-009). */
    private static final int SLOTS = 5;
    private static final int MAX_STACK_SIZE = 64;
    private static final int[] ALL_SLOTS = {0, 1, 2, 3, 4};
    /** How many loose emeralds one emerald block is worth (MOTOR-REQ-006). */
    private static final int EMERALDS_PER_BLOCK = 9;
    /** Once every 20 server ticks, i.e. once a second (MOTOR-REQ-006). */
    private static final int TICKS_PER_METER_ADVANCE = 20;

    private Stats stats = Stats.middleOf(Tier.I);
    private MotorState state = MotorState.STOPPED;
    private NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
    private Meter meter = new Meter();
    /** The load the meter last read, 0 while idle (docs/spec/domains/motor.md MOTOR-DEC-001). */
    private double load = 0.0;

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
        boolean signalled = level.hasNeighborSignal(worldPosition);
        // MOTOR-REQ-010: no burn while paused. MOTOR-REQ-011 falls out of tick() only running
        // while the chunk ticks: nothing below advances the meter or moves items otherwise.
        if (!signalled && state == MotorState.RUNNING && level.getGameTime() % TICKS_PER_METER_ADVANCE == 0) {
            burnOneSecond();
        }
        MotorState derived = signalled ? MotorState.PAUSED : hasFuel() ? MotorState.RUNNING : MotorState.STOPPED;
        if (derived != state) {
            state = derived;
            // MOTOR-REQ-004, MOTOR-REQ-010: updateGeneratedRotation() drives both directions —
            // starting attaches a network and reports the added capacity; stopping detaches from
            // it entirely (GeneratingKineticBlockEntity.applyNewSpeed), which is what actually
            // removes our contribution, so no separate capacity notification is needed or safe
            // here (the network may not exist yet on the tick a stop follows a start). This same
            // check catches both MOTOR-REQ-007 (held at one, empty) and MOTOR-FAIL-004 (the take
            // above emptied the inventory) on the tick the meter causes them, since hasFuel() is
            // re-read after burnOneSecond() ran.
            updateGeneratedRotation();
            sendData();
        }
    }

    /**
     * One second's worth of the meter: reads the network's load the way Create's stress gauge
     * does, advances the {@link Meter} and takes whatever emeralds became due, holding the meter
     * at one when none could be taken (MOTOR-REQ-006, MOTOR-REQ-007, MOTOR-FAIL-002, MOTOR-FAIL-004).
     */
    private void burnOneSecond() {
        float capacity = networkCapacity();
        load = capacity > 0 ? Math.min(1.0, networkStress() / capacity) : 0.0;
        int due = meter.advance(stats.ratePerMinute(), load);
        for (int i = 0; i < due; i++) {
            if (!takeOneEmerald()) {
                meter.holdAtOne();
                break;
            }
        }
        setChanged();
        sendData();
    }

    /**
     * Takes one emerald from the inventory, splitting an emerald block into nine loose emeralds
     * in a free or the same slot when no loose emerald is there first (MOTOR-REQ-006). Returns
     * {@code false} without changing anything when the inventory holds nothing to take.
     */
    private boolean takeOneEmerald() {
        int looseSlot = findSlotOf(Items.EMERALD);
        if (looseSlot >= 0) {
            shrinkSlot(looseSlot, 1);
            setChanged();
            return true;
        }
        int blockSlot = findSlotOf(Items.EMERALD_BLOCK);
        if (blockSlot < 0) {
            return false;
        }
        int destSlot = items.get(blockSlot).getCount() == 1 ? blockSlot : findEmptySlot();
        if (destSlot < 0) {
            // No room to hold the eight loose emeralds the split would leave behind: leave the
            // block untouched rather than destroying items nobody asked to lose.
            return false;
        }
        shrinkSlot(blockSlot, 1);
        ItemStack destStack = items.get(destSlot);
        if (destStack.isEmpty()) {
            items.set(destSlot, new ItemStack(Items.EMERALD, EMERALDS_PER_BLOCK - 1));
        } else {
            destStack.grow(EMERALDS_PER_BLOCK - 1);
        }
        setChanged();
        return true;
    }

    /** Shrinks a slot and normalises it back to {@link ItemStack#EMPTY} once spent, so a stack
     * that reaches zero never lingers as a "ghost" that still matches its old item type. */
    private void shrinkSlot(int slot, int amount) {
        ItemStack stack = items.get(slot);
        stack.shrink(amount);
        if (stack.isEmpty()) {
            items.set(slot, ItemStack.EMPTY);
        }
    }

    private int findSlotOf(Item item) {
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty() && stack.is(item)) {
                return i;
            }
        }
        return -1;
    }

    private int findEmptySlot() {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    /** The network's last-pushed stress, read the way {@code StressGaugeBlockEntity.getNetworkStress()} does. */
    private float networkStress() {
        return stress;
    }

    /** The network's last-pushed capacity, read the way {@code StressGaugeBlockEntity.getNetworkCapacity()} does. */
    private float networkCapacity() {
        return capacity;
    }

    private static boolean isAcceptedFuel(ItemStack stack) {
        return !stack.isEmpty() && (stack.is(Items.EMERALD) || stack.is(Items.EMERALD_BLOCK));
    }

    /** Whether the inventory holds at least one emerald or emerald block (MOTOR-REQ-006..009). */
    private boolean hasFuel() {
        for (ItemStack stack : items) {
            if (isAcceptedFuel(stack)) {
                return true;
            }
        }
        return false;
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

    /** How many emeralds the inventory holds, an emerald block counting nine (MOTOR-REQ-006). */
    public int emeraldsInside() {
        int total = 0;
        for (ItemStack stack : items) {
            if (stack.is(Items.EMERALD)) {
                total += stack.getCount();
            } else if (stack.is(Items.EMERALD_BLOCK)) {
                total += stack.getCount() * EMERALDS_PER_BLOCK;
            }
        }
        return total;
    }

    /** The load the meter last read: {@code min(1, network stress / network capacity)}, 0 while idle (MOTOR-DEC-001). */
    public double load() {
        return load;
    }

    /** The meter advancing toward the next emerald, held at exactly one while stopped empty (MOTOR-REQ-006, MOTOR-REQ-007). */
    public Meter meter() {
        return meter;
    }

    /**
     * Seconds until the inventory runs out at the current load, or {@code -1} while not running
     * or drawing no load (nothing to divide by). Not part of the data contract: a display figure
     * for the overlay (MOTOR-REQ-012), which the screen ticket (MM-6) may recompute differently.
     */
    public double secondsRemaining() {
        double perSecond = stats.ratePerMinute() / 60.0 * load;
        if (state != MotorState.RUNNING || perSecond <= 0.0) {
            return -1;
        }
        double unitsRemaining = emeraldsInside() - meter.fraction();
        return unitsRemaining / perSecond;
    }

    // -- WorldlyContainer (MOTOR-REQ-008, MOTOR-REQ-009, ARCH-DEC-003) --

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return ALL_SLOTS;
    }

    /** Only emeralds and emerald blocks, from every face and every automation (MOTOR-REQ-008). */
    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return isAcceptedFuel(stack);
    }

    /** Never: only a player in the screen takes emeralds out (MOTOR-REQ-009). */
    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return false;
    }

    // -- Container --

    @Override
    public int getContainerSize() {
        return SLOTS;
    }

    @Override
    public int getMaxStackSize() {
        return MAX_STACK_SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize(stack)) {
            stack.setCount(getMaxStackSize(stack));
        }
        setChanged();
    }

    @Override
    public void clearContent() {
        items.clear();
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    /** Same rule as face insertion, for the screen ticket's player-facing slots (MOTOR-REQ-008). */
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return isAcceptedFuel(stack);
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
        output.putDouble("Meter", meter.fraction());
        ContainerHelper.saveAllItems(output, items);
        if (clientPacket) {
            // Derived, not saved state (docs/spec/contracts/data-contract.md): recomputed every
            // second while running, so only worth sending, never worth persisting.
            output.putFloat("Load", (float) load);
        }
    }

    @Override
    protected void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        stats = input.read("Stats", StatsCodec.CODEC).orElseGet(() -> Stats.middleOf(Tier.I));
        String saved = input.getStringOr("State", MotorState.STOPPED.name());
        state = java.util.Arrays.stream(MotorState.values()).filter(m -> m.name().equals(saved)).findFirst().orElse(MotorState.STOPPED);
        double savedMeter = input.getDoubleOr("Meter", 0.0);
        meter = new Meter(Math.min(1.0, Math.max(0.0, savedMeter)));
        items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
        if (clientPacket) {
            load = input.getFloatOr("Load", 0f);
        }
    }
}
