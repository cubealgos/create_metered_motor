package metered_motor.model;

/**
 * The three fixed motor tiers, each pinned to a real Create Fly steam-engine set-up
 * (docs/spec/decisions/DEC-009-fixed-tiers.md, `MOTOR-DEC-005`): tier I is one engine on a
 * level-1 boiler, tier II is four engines on a level-4 boiler, tier III is the full level-18
 * boiler with 18 engines. Every motor of a tier is identical — nothing is rolled — so a tier's
 * rpm, stress capacity and rate at full load are fixed constants, not stored data
 * (`contracts/data-contract.md` version 2).
 */
public enum Tier {
    /** One engine, level-1 boiler: 16,384 SU. */
    I(3, 24, 16_384),
    /** Four engines, level-4 boiler: 65,536 SU. */
    II(4, 40, 65_536),
    /** The full level-18 boiler, 18 engines: 294,912 SU. */
    III(5, 64, 294_912);

    /** Every tier runs at the steam engine's own top ("active") speed (`DEC-009`, ruling 2). */
    private static final int RPM = 64;
    /** SU per emerald-minute at full load, fixed uniformly across tiers, not per tier
     *  (`MOTOR-REQ-005`, `MOTOR-DEC-003` amended by `DEC-009`). */
    private static final double BURN_DIVISOR = 92_160.0;

    private final int toolsmithLevel;
    private final int price;
    private final int capacity;

    Tier(int toolsmithLevel, int price, int capacity) {
        this.toolsmithLevel = toolsmithLevel;
        this.price = price;
        this.capacity = capacity;
    }

    /** The toolsmith level (villager trade level, 3 to 5) that offers this tier. */
    public int toolsmithLevel() {
        return toolsmithLevel;
    }

    /** The trade's price in emeralds. */
    public int price() {
        return price;
    }

    /** Every tier's fixed generated speed while running: 64 rpm, the steam engine's own active speed (`DEC-009`). */
    public int rpm() {
        return RPM;
    }

    /** The tier's fixed stress capacity in SU, the real steam-engine set-up's own total (`DEC-009` §The ladder). */
    public int capacity() {
        return capacity;
    }

    /** The tier's fixed rate at full load: capacity divided by the uniform burn divisor (`MOTOR-REQ-005`, `DEC-009`). */
    public double ratePerMinute() {
        return capacity / BURN_DIVISOR;
    }

    /** The tier's 1-based number as the data contract writes it (docs/spec/contracts/data-contract.md). */
    public int number() {
        return ordinal() + 1;
    }

    /** The tier for its 1-based number; throws for anything but 1, 2 or 3. */
    public static Tier ofNumber(int number) {
        return switch (number) {
            case 1 -> I;
            case 2 -> II;
            case 3 -> III;
            default -> throw new IllegalArgumentException("tier " + number + " does not exist");
        };
    }
}
