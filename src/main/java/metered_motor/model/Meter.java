package metered_motor.model;

/**
 * The fractional emerald counter a motor's block entity advances once a second while running
 * (docs/spec/contracts/data-contract.md, MOTOR-REQ-006, MOTOR-REQ-007).
 */
public final class Meter {
    private double fraction;

    public Meter() {
        this(0.0);
    }

    /** Restores a meter at a saved fraction; 1.0 is the valid "held" state of MOTOR-REQ-007. */
    public Meter(double fraction) {
        if (fraction < 0.0 || fraction > 1.0) {
            throw new IllegalArgumentException("a meter's fraction must be within [0, 1], was " + fraction);
        }
        this.fraction = fraction;
    }

    /** The meter's current fraction, 0 ≤ m ≤ 1 (docs/spec/contracts/data-contract.md). */
    public double fraction() {
        return fraction;
    }

    /**
     * One second's advance: adds {@code ratePerMinute / 60 * min(1, max(0, load))} to the counter
     * (MOTOR-REQ-006) and returns how many whole emeralds became due, keeping the remaining
     * fraction.
     */
    public int advance(double ratePerMinute, double load) {
        double clampedLoad = Math.min(1.0, Math.max(0.0, load));
        fraction += ratePerMinute / 60.0 * clampedLoad;
        int due = (int) Math.floor(fraction);
        if (due > 0) {
            fraction -= due;
        }
        return due;
    }

    /** Whether an emerald is currently owed and waiting to be taken. */
    public boolean isDue() {
        return fraction >= 1.0;
    }

    /**
     * When the meter reached one but the inventory held no emerald to take, leaves it at exactly
     * one so the next emerald that arrives is taken immediately (MOTOR-REQ-007).
     */
    public void holdAtOne() {
        fraction = 1.0;
    }
}
