package metered_motor.model;

import java.util.function.DoubleSupplier;

/**
 * A uniform band a value is rolled within, min to max inclusive (docs/spec/domains/trade.md §3).
 */
public record Band(double min, double max) {
    public Band {
        if (min > max) {
            throw new IllegalArgumentException("a band's minimum must not exceed its maximum: [" + min + ", " + max + "]");
        }
    }

    /** The band's midpoint, used for the unrolled fallback (MOTOR-FAIL-003). */
    public double middle() {
        return (min + max) / 2.0;
    }

    /** A uniform draw within the band from a random source in [0, 1) (docs/spec/domains/trade.md §3). */
    public double roll(DoubleSupplier random) {
        return min + (max - min) * random.getAsDouble();
    }
}
