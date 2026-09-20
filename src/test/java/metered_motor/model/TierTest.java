package metered_motor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * The fixed tier ladder (`decisions/DEC-009-fixed-tiers.md` §The ladder, `MOTOR-REQ-004`,
 * `MOTOR-REQ-005`): every tier runs at 64 rpm; capacity is the real steam-engine set-up's own
 * total; the rate at full load is capacity divided by the uniform 92,160 burn divisor.
 */
class TierTest {
    @Test
    void everyTierRunsAt64Rpm() {
        for (Tier tier : Tier.values()) {
            assertEquals(64, tier.rpm(), tier + " runs at 64 rpm");
        }
    }

    @Test
    void theLaddersFixedCapacities() {
        assertEquals(16_384, Tier.I.capacity(), "one engine, level-1 boiler");
        assertEquals(65_536, Tier.II.capacity(), "four engines, level-4 boiler");
        assertEquals(294_912, Tier.III.capacity(), "the full level-18 boiler, 18 engines");
    }

    @Test
    void ratePerMinuteIsCapacityOverTheUniform92160Divisor() {
        assertEquals(16_384.0 / 92_160.0, Tier.I.ratePerMinute(), 1e-9);
        assertEquals(65_536.0 / 92_160.0, Tier.II.ratePerMinute(), 1e-9);
        assertEquals(294_912.0 / 92_160.0, Tier.III.ratePerMinute(), 1e-9);
    }

    @Test
    void theSpecsRoundedRatesAtFullLoad() {
        // decisions/DEC-009-fixed-tiers.md's ladder table, rounded to two decimal places.
        assertEquals(0.18, Math.round(Tier.I.ratePerMinute() * 100.0) / 100.0);
        assertEquals(0.71, Math.round(Tier.II.ratePerMinute() * 100.0) / 100.0);
        assertEquals(3.2, Math.round(Tier.III.ratePerMinute() * 100.0) / 100.0);
    }

    @Test
    void tierIiiAtFullLoadLandsExactlyOnTheOneStackADayCeiling() {
        // 20 Minecraft-day minutes * rate must equal exactly 64 emeralds (one stack): the divisor
        // was solved from this exact bound (decisions/DEC-009-fixed-tiers.md "Why 92,160").
        assertEquals(64.0, Tier.III.ratePerMinute() * 20.0, 1e-9);
    }

    @Test
    void toolsmithLevelsAndPricesAreUnchangedFromTheRolledDesign() {
        assertEquals(3, Tier.I.toolsmithLevel());
        assertEquals(24, Tier.I.price());
        assertEquals(4, Tier.II.toolsmithLevel());
        assertEquals(40, Tier.II.price());
        assertEquals(5, Tier.III.toolsmithLevel());
        assertEquals(64, Tier.III.price());
    }

    @Test
    void ofNumberRoundTripsWithNumber() {
        for (Tier tier : Tier.values()) {
            assertEquals(tier, Tier.ofNumber(tier.number()));
        }
    }
}
