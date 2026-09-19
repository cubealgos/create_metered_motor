package metered_motor.model;

import java.util.Objects;

/**
 * A motor's rolled stats, fixed for its life once rolled and carried by the {@code metered_motor:stats}
 * component (docs/spec/contracts/data-contract.md, MOTOR-REQ-001).
 */
public record Stats(int version, Tier tier, int rpm, int capacity, double efficiency) {
    /** The schema this build writes. */
    public static final int VERSION = 1;
    private static final double MIN_EFFICIENCY = 0.1;
    private static final double MAX_EFFICIENCY = 10.0;
    /** SU per emerald-minute at full load, a fixed number in code, not data (MOTOR-DEC-003). */
    private static final double BURN_DIVISOR = 8_192.0;

    public Stats {
        Objects.requireNonNull(tier, "tier");
        if (version < 1) {
            throw new IllegalArgumentException("version must be at least 1, was " + version);
        }
        if (rpm <= 0) {
            throw new IllegalArgumentException("rpm must be positive, was " + rpm);
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive, was " + capacity);
        }
        if (efficiency < MIN_EFFICIENCY || efficiency > MAX_EFFICIENCY) {
            throw new IllegalArgumentException(
                "efficiency must be within [" + MIN_EFFICIENCY + ", " + MAX_EFFICIENCY + "], was " + efficiency);
        }
    }

    /** True when written by a newer build than this one: kept intact, shown as unknown, never edited or placed (MOTOR-REQ-014). */
    public boolean readOnly() {
        return version > VERSION;
    }

    /** The rate at full load: capacity divided by the fixed burn divisor and by efficiency (MOTOR-REQ-005, MOTOR-DEC-003). */
    public double ratePerMinute() {
        return capacity / BURN_DIVISOR / efficiency;
    }

    /** The unrolled fallback: the middle of a tier's bands, for a missing or malformed component (MOTOR-FAIL-003, DATA-REQ-003). */
    public static Stats middleOf(Tier tier) {
        int midRpm = (int) Math.round(tier.rpmBand().middle());
        int midCapacity = (int) Math.round(tier.capacityBand().middle());
        double midEfficiency = tier.efficiencyBand().middle();
        return new Stats(VERSION, tier, midRpm, midCapacity, midEfficiency);
    }
}
