package metered_motor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.DoubleSupplier;
import org.junit.jupiter.api.Test;

/** The offer's roll: uniform draws within band, and a malformed band's fallback (TRADE-REQ-002, TRADE-REQ-004). */
class RollTest {
    @Test
    void aUniformRollLandsInsideItsTiersDefaultBand() {
        Tier tier = Tier.II;
        List<String> reports = new ArrayList<>();
        Stats rolled = Roll.roll(tier, tier.rpmBand().min(), tier.rpmBand().max(),
            tier.capacityBand().min(), tier.capacityBand().max(),
            tier.efficiencyBand().min(), tier.efficiencyBand().max(),
            fixed(0.5), reports::add);
        assertTrue(reports.isEmpty(), "a valid band is never reported");
        assertEquals(tier.rpmBand().middle(), rolled.rpm(), 0.5);
        assertEquals(tier.capacityBand().middle(), rolled.capacity(), 0.5);
        assertEquals(tier.efficiencyBand().middle(), rolled.efficiency(), 1e-9);
        assertTrue(rolled.rpm() >= tier.rpmBand().min() && rolled.rpm() <= tier.rpmBand().max());
        assertTrue(rolled.capacity() >= tier.capacityBand().min() && rolled.capacity() <= tier.capacityBand().max());
    }

    @Test
    void aBandWhoseMinimumExceedsItsMaximumFallsBackToTheTiersDefaultAndIsReported() {
        List<String> reports = new ArrayList<>();
        Tier tier = Tier.I;
        Stats rolled = Roll.roll(tier, 100, 10, // rpm: min > max, rejected
            tier.capacityBand().min(), tier.capacityBand().max(),
            tier.efficiencyBand().min(), tier.efficiencyBand().max(),
            fixed(0.0), reports::add);
        assertEquals(1, reports.size());
        assertTrue(reports.get(0).contains("rpm"), reports.get(0));
        assertEquals(tier.rpmBand().min(), rolled.rpm());
    }

    @Test
    void aBandFarOutsideTheTiersDefaultByMoreThanAFactorOfFourFallsBackAndIsReported() {
        List<String> reports = new ArrayList<>();
        Tier tier = Tier.I; // capacity default 512 to 2,048
        Stats rolled = Roll.roll(tier, tier.rpmBand().min(), tier.rpmBand().max(),
            1, 1_000_000, // capacity: far past 4x the default range on both ends
            tier.efficiencyBand().min(), tier.efficiencyBand().max(),
            fixed(1.0), reports::add);
        assertEquals(1, reports.size());
        assertTrue(reports.get(0).contains("capacity"), reports.get(0));
        assertEquals(tier.capacityBand().max(), rolled.capacity());
    }

    @Test
    void aBandWithinFourTimesTheTiersDefaultIsHonoured() {
        List<String> reports = new ArrayList<>();
        Tier tier = Tier.I;
        // capacity default is 512 to 2,048; 400 to 3,000 stays within a factor of four both ways.
        Stats rolled = Roll.roll(tier, tier.rpmBand().min(), tier.rpmBand().max(),
            400, 3_000,
            tier.efficiencyBand().min(), tier.efficiencyBand().max(),
            fixed(0.0), reports::add);
        assertTrue(reports.isEmpty());
        assertEquals(400, rolled.capacity());
    }

    private static DoubleSupplier fixed(double value) {
        return () -> value;
    }
}
