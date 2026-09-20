package metered_motor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Version handling and tier delegation of `docs/spec/contracts/data-contract.md` (version 2,
 * `decisions/DEC-009-fixed-tiers.md`): {@link Stats} stores only {@code version} and {@code tier};
 * {@link #rpm()}, {@link #capacity()} and {@link #ratePerMinute()} delegate to {@link Tier}
 * (MOTOR-REQ-005, MOTOR-REQ-014, MOTOR-FAIL-003).
 */
class StatsTest {
    @Test
    void rpmCapacityAndRateDelegateToTheTier() {
        for (Tier tier : Tier.values()) {
            Stats stats = new Stats(Stats.VERSION, tier);
            assertEquals(tier.rpm(), stats.rpm());
            assertEquals(tier.capacity(), stats.capacity());
            assertEquals(tier.ratePerMinute(), stats.ratePerMinute(), 1e-9);
        }
    }

    @Test
    void ofIsTheCurrentVersionStatsForATier() {
        Stats stats = Stats.of(Tier.I);
        assertEquals(Tier.I, stats.tier());
        assertEquals(Stats.VERSION, stats.version());
        assertFalse(stats.readOnly());
    }

    @Test
    void constructionRejectsAVersionBelowOne() {
        assertThrows(IllegalArgumentException.class, () -> new Stats(0, Tier.I));
    }

    @Test
    void constructionRejectsANullTier() {
        assertThrows(NullPointerException.class, () -> new Stats(1, null));
    }

    @Test
    void aVersionNewerThanThisBuildIsReadOnly() {
        assertFalse(new Stats(Stats.VERSION, Tier.I).readOnly());
        assertTrue(new Stats(Stats.VERSION + 1, Tier.I).readOnly());
    }
}
