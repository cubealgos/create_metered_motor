package metered_motor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** The burn rate formula and version handling of docs/spec/domains/motor.md (MOTOR-REQ-005, MOTOR-REQ-014, MOTOR-FAIL-003). */
class StatsTest {
    @Test
    void rateFollowsCapacityAndEfficiencyAtTheSpecsBoundExamples() {
        // tier III worst efficiency at the biggest roll: 18,432 SU / 8,192 / 0.75 = 3.0 a minute.
        assertEquals(3.0, new Stats(1, Tier.III, 200, 18_432, 0.75).ratePerMinute(), 1e-9);
        // tier III best roll: the smallest tier III capacity at the best efficiency, 0.8 a minute.
        assertEquals(0.8, new Stats(1, Tier.III, 64, 8_192, 1.25).ratePerMinute(), 1e-9);
        // tier I best roll: the smallest capacity at the best efficiency, 0.05 a minute (one every 20 minutes).
        assertEquals(0.05, new Stats(1, Tier.I, 16, 512, 1.25).ratePerMinute(), 1e-9);
        // tier I worst roll: the biggest tier I capacity at the worst efficiency, one every 3 minutes.
        assertEquals(1.0 / 3.0, new Stats(1, Tier.I, 64, 2_048, 0.75).ratePerMinute(), 1e-9);
    }

    @Test
    void middleOfTierIIsItsBandsMidpoints() {
        Stats unrolled = Stats.middleOf(Tier.I);
        assertEquals(Tier.I, unrolled.tier());
        assertEquals(40, unrolled.rpm());
        assertEquals(1_280, unrolled.capacity());
        assertEquals(1.0, unrolled.efficiency(), 1e-9);
    }

    @Test
    void validationRejectsNonPositiveOrOutOfRangeValuesRatherThanInventingThem() {
        assertThrows(IllegalArgumentException.class, () -> new Stats(1, Tier.I, 0, 512, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new Stats(1, Tier.I, 40, -1, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new Stats(1, Tier.I, 40, 512, 0.05));
        assertThrows(IllegalArgumentException.class, () -> new Stats(1, Tier.I, 40, 512, 11.0));
        assertThrows(IllegalArgumentException.class, () -> new Stats(0, Tier.I, 40, 512, 1.0));
    }

    @Test
    void aVersionNewerThanThisBuildIsReadOnly() {
        assertFalse(new Stats(Stats.VERSION, Tier.I, 40, 1_280, 1.0).readOnly());
        assertTrue(new Stats(Stats.VERSION + 1, Tier.I, 40, 1_280, 1.0).readOnly());
    }
}
