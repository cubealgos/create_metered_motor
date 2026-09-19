package metered_motor.model;

import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

/**
 * The villager offer's roll: three independent uniform draws (rpm, capacity, efficiency) from
 * candidate bands, with a malformed candidate rejected in favour of the tier's default band
 * (docs/spec/domains/trade.md §3, TRADE-REQ-002, TRADE-REQ-004).
 */
public final class Roll {
    /** Beyond this multiple of the tier's default band, a candidate band is rejected. */
    private static final double MAX_FACTOR = 4.0;

    private Roll() {
    }

    /**
     * Rolls a tier's stats from candidate rpm, capacity and efficiency bands (each a min and a
     * max) and a random source drawing in [0, 1). A candidate whose minimum exceeds its maximum,
     * or that strays from the tier's default band by more than a factor of four, is rejected in
     * favour of the tier's default band and reported once through {@code report} (no logger here;
     * the package stays pure) (TRADE-REQ-004).
     */
    public static Stats roll(Tier tier, double rpmMin, double rpmMax, double capacityMin, double capacityMax,
        double efficiencyMin, double efficiencyMax, DoubleSupplier random, Consumer<String> report) {
        Band rpm = resolve("rpm", rpmMin, rpmMax, tier.rpmBand(), report);
        Band capacity = resolve("capacity", capacityMin, capacityMax, tier.capacityBand(), report);
        Band efficiency = resolve("efficiency", efficiencyMin, efficiencyMax, tier.efficiencyBand(), report);
        int rolledRpm = (int) Math.round(rpm.roll(random));
        int rolledCapacity = (int) Math.round(capacity.roll(random));
        double rolledEfficiency = efficiency.roll(random);
        return new Stats(Stats.VERSION, tier, rolledRpm, rolledCapacity, rolledEfficiency);
    }

    private static Band resolve(String name, double min, double max, Band tierDefault, Consumer<String> report) {
        if (min > max || min < tierDefault.min() / MAX_FACTOR || max > tierDefault.max() * MAX_FACTOR) {
            report.accept(name + " band [" + min + ", " + max + "] rejected, using the tier default " + tierDefault);
            return tierDefault;
        }
        return new Band(min, max);
    }
}
