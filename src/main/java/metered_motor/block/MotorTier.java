package metered_motor.block;

import java.util.Locale;
import metered_motor.model.Tier;
import net.minecraft.util.StringRepresentable;

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

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
