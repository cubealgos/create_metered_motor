package metered_motor.block;

import java.util.Locale;
import metered_motor.MeteredMotor;
import metered_motor.model.Stats;
import metered_motor.model.Tier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;

/**
 * Adapts {@link Tier} to a block state value: {@link MeteredMotorBlock}'s {@code tier} property
 * selects the block's model set (andesite grey, brass or gold casing) the way {@code facing}
 * selects its orientation (MOTOR-REQ-013, MOTOR-DEC-004).
 */
public enum MotorTier implements StringRepresentable {
    I(Tier.I),
    II(Tier.II),
    III(Tier.III);

    private final Tier tier;

    MotorTier(Tier tier) {
        this.tier = tier;
    }

    /** The domain tier this block state value stands for. */
    public Tier tier() {
        return tier;
    }

    /** The block state value for a domain tier. */
    public static MotorTier of(Tier tier) {
        return switch (tier) {
            case I -> I;
            case II -> II;
            case III -> III;
        };
    }

    /**
     * The tier a stack's item model shall show: tier I for a stack with no {@link
     * MeteredMotor#STATS} component (unrolled, MOTOR-FAIL-003) or with one newer than this build
     * can read ({@link Stats#readOnly()}, MOTOR-REQ-014), otherwise the rolled tier. Server-safe
     * and pure of any client class so both {@link MeteredMotorBlock#getStateForPlacement} and the
     * client-only item model property can share one mapping (MOTOR-REQ-013, TRADE-REQ-005).
     */
    public static MotorTier of(ItemStack stack) {
        Stats stats = stack.get(MeteredMotor.STATS);
        if (stats == null || stats.readOnly()) {
            return I;
        }
        return of(stats.tier());
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
