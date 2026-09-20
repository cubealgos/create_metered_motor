package metered_motor.model;

import java.util.Objects;

/**
 * A motor's stats: fixed for its life, carried by the {@code metered_motor:stats} component
 * (docs/spec/contracts/data-contract.md, MOTOR-REQ-001). Version 2 (`decisions/DEC-009-fixed-tiers.md`)
 * stores only the tier — rpm, stress capacity and rate are never rolled, so they are derived from
 * {@link Tier} in code, and every motor of a tier is identical. {@link #rpm()}, {@link #capacity()}
 * and {@link #ratePerMinute()} delegate to the tier so every existing caller (the tooltip, the
 * screen, the goggles overlay) reads the same accessors it always has.
 */
public record Stats(int version, Tier tier) {
    /** The schema this build writes (`contracts/data-contract.md` version 2). */
    public static final int VERSION = 2;

    public Stats {
        Objects.requireNonNull(tier, "tier");
        if (version < 1) {
            throw new IllegalArgumentException("version must be at least 1, was " + version);
        }
    }

    /** True when written by a newer build than this one: kept intact, shown as unknown, never edited or placed (MOTOR-REQ-014). */
    public boolean readOnly() {
        return version > VERSION;
    }

    /** The tier's fixed generated speed while running: 64 rpm for every tier (`DEC-009`). */
    public int rpm() {
        return tier.rpm();
    }

    /** The tier's fixed stress capacity in SU (`DEC-009` §The ladder). */
    public int capacity() {
        return tier.capacity();
    }

    /** The tier's fixed rate at full load: capacity divided by the uniform burn divisor (MOTOR-REQ-005, `DEC-009`). */
    public double ratePerMinute() {
        return tier.ratePerMinute();
    }

    /** The current-version stats for a tier: the fallback for a missing or malformed component
     *  (MOTOR-FAIL-003, DATA-REQ-003), and every motor a trade file or the debug command gives,
     *  since nothing is rolled any more (`DEC-009`). */
    public static Stats of(Tier tier) {
        return new Stats(VERSION, tier);
    }
}
