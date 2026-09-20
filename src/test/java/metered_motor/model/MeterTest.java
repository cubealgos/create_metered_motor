package metered_motor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** The meter's per-second arithmetic (docs/spec/domains/motor.md, MOTOR-REQ-006, MOTOR-REQ-007). */
class MeterTest {
    @Test
    void takesExactlyOneEmeraldAfterTheComputedNumberOfSecondsUnderAFixedLoad() {
        // An arbitrary round rate (0.05 a second at full load) chosen for a clean 20-second
        // computation; a fixed tier's own real rate is checked in aTiersFixedRateTakesOneEmeraldAtTheComputedSecond.
        double ratePerMinute = 3.0;
        Meter meter = new Meter();
        int totalDue = 0;
        for (int second = 1; second <= 20; second++) {
            int due = meter.advance(ratePerMinute, 1.0);
            totalDue += due;
            if (second < 20) {
                assertEquals(0, due, "no emerald before the twentieth second");
            }
        }
        assertEquals(1, totalDue, "exactly one emerald across the twenty seconds");
        assertEquals(0.0, meter.fraction(), 1e-9, "the fraction is consumed, not overpaid");
    }

    @Test
    void aTiersFixedRateTakesOneEmeraldAtTheComputedSecond() {
        // MOTOR-REQ-005/006: each tier's own fixed rate (decisions/DEC-009-fixed-tiers.md), at
        // full load, must take exactly one emerald at the second the rate's own arithmetic predicts.
        for (Tier tier : Tier.values()) {
            double ratePerMinute = tier.ratePerMinute();
            long dueAtSecond = (long) Math.ceil(60.0 / ratePerMinute);
            Meter meter = new Meter();
            int totalDue = 0;
            for (long second = 1; second <= dueAtSecond; second++) {
                int due = meter.advance(ratePerMinute, 1.0);
                totalDue += due;
                if (second < dueAtSecond) {
                    assertEquals(0, due, tier + ": no emerald before second " + dueAtSecond + ", was due at " + second);
                }
            }
            assertEquals(1, totalDue, tier + ": exactly one emerald by second " + dueAtSecond);
        }
    }

    @Test
    void noEmeraldAccumulatesAtZeroLoad() {
        Meter meter = new Meter();
        for (int i = 0; i < 1000; i++) {
            assertEquals(0, meter.advance(3.0, 0.0));
        }
        assertEquals(0.0, meter.fraction(), 1e-9);
    }

    @Test
    void loadIsClampedToOne() {
        Meter overload = new Meter();
        Meter fullLoad = new Meter();
        overload.advance(3.0, 5.0);
        fullLoad.advance(3.0, 1.0);
        assertEquals(fullLoad.fraction(), overload.fraction(), 1e-9, "load above one behaves as one");
    }

    @Test
    void holdAtOneLeavesTheMeterAtOneForTheNextArrival() {
        Meter meter = new Meter();
        meter.advance(3.0, 1.0); // not yet due
        meter.holdAtOne();
        assertEquals(1.0, meter.fraction(), 1e-9);
        assertTrue(meter.isDue());
    }

    @Test
    void freshMeterIsNotDue() {
        assertFalse(new Meter().isDue());
    }
}
