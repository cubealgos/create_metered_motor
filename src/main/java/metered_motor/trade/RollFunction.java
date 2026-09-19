package metered_motor.trade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import metered_motor.MeteredMotor;
import metered_motor.model.Band;
import metered_motor.model.Roll;
import metered_motor.model.Stats;
import metered_motor.model.Tier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * The {@code metered_motor:roll} loot function: rolls a motor's stats from a trade file's tier
 * and optional bands, using the loot context's random source, and writes them to
 * {@link MeteredMotor#STATS} on the offered item (TRADE-REQ-002, ARCH-DEC-004).
 */
public final class RollFunction extends LootItemConditionalFunction {
    private static final Codec<BandSpec> BAND_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.DOUBLE.fieldOf("min").forGetter(BandSpec::min),
        Codec.DOUBLE.fieldOf("max").forGetter(BandSpec::max)
    ).apply(instance, BandSpec::new));

    /** Registered as {@code metered_motor:roll} by {@link TradeRegistration}. */
    public static final MapCodec<RollFunction> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> commonFields(instance).and(instance.group(
        Codec.intRange(1, 3).fieldOf("tier").forGetter(RollFunction::tierNumber),
        BAND_CODEC.optionalFieldOf("rpm").forGetter(f -> f.rpm),
        BAND_CODEC.optionalFieldOf("capacity").forGetter(f -> f.capacity),
        BAND_CODEC.optionalFieldOf("efficiency").forGetter(f -> f.efficiency)
    )).apply(instance, RollFunction::new));

    private final int tierNumber;
    private final Optional<BandSpec> rpm;
    private final Optional<BandSpec> capacity;
    private final Optional<BandSpec> efficiency;

    private RollFunction(List<LootItemCondition> predicates, int tierNumber, Optional<BandSpec> rpm,
        Optional<BandSpec> capacity, Optional<BandSpec> efficiency) {
        super(predicates);
        this.tierNumber = tierNumber;
        this.rpm = rpm;
        this.capacity = capacity;
        this.efficiency = efficiency;
    }

    private int tierNumber() {
        return tierNumber;
    }

    @Override
    public MapCodec<RollFunction> codec() {
        return MAP_CODEC;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        Tier tier = Tier.ofNumber(tierNumber);
        BandSpec rpmSpec = rpm.orElseGet(() -> BandSpec.of(tier.rpmBand()));
        BandSpec capacitySpec = capacity.orElseGet(() -> BandSpec.of(tier.capacityBand()));
        BandSpec efficiencySpec = efficiency.orElseGet(() -> BandSpec.of(tier.efficiencyBand()));
        Stats stats = Roll.roll(tier, rpmSpec.min(), rpmSpec.max(), capacitySpec.min(), capacitySpec.max(),
            efficiencySpec.min(), efficiencySpec.max(), () -> context.getRandom().nextDouble(),
            message -> MeteredMotor.LOGGER.warn("metered_motor:roll: {}", message));
        stack.set(MeteredMotor.STATS, stats);
        return stack;
    }

    /** A trade file's candidate band before it is validated against the tier's default (TRADE-REQ-004). */
    private record BandSpec(double min, double max) {
        static BandSpec of(Band band) {
            return new BandSpec(band.min(), band.max());
        }
    }
}
