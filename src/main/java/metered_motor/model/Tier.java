package metered_motor.model;

/**
 * The three motor tiers, each with the toolsmith level and price that sells it and the default
 * rpm, stress capacity and efficiency bands its roll draws from (docs/spec/domains/trade.md §3).
 */
public enum Tier {
    I(3, 24, new Band(16, 64), new Band(512, 2_048), new Band(0.75, 1.25)),
    II(4, 40, new Band(32, 128), new Band(2_048, 8_192), new Band(0.75, 1.25)),
    III(5, 64, new Band(64, 256), new Band(8_192, 18_432), new Band(0.75, 1.25));

    private final int toolsmithLevel;
    private final int price;
    private final Band rpmBand;
    private final Band capacityBand;
    private final Band efficiencyBand;

    Tier(int toolsmithLevel, int price, Band rpmBand, Band capacityBand, Band efficiencyBand) {
        this.toolsmithLevel = toolsmithLevel;
        this.price = price;
        this.rpmBand = rpmBand;
        this.capacityBand = capacityBand;
        this.efficiencyBand = efficiencyBand;
    }

    /** The toolsmith level (villager trade level, 3 to 5) that offers this tier. */
    public int toolsmithLevel() {
        return toolsmithLevel;
    }

    /** The trade's price in emeralds. */
    public int price() {
        return price;
    }

    public Band rpmBand() {
        return rpmBand;
    }

    public Band capacityBand() {
        return capacityBand;
    }

    public Band efficiencyBand() {
        return efficiencyBand;
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
