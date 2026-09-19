package metered_motor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** A band's own validation and uniform draw (docs/spec/domains/trade.md §3). */
class BandTest {
    @Test
    void aBandWhoseMinimumExceedsItsMaximumIsRejectedAtConstruction() {
        assertThrows(IllegalArgumentException.class, () -> new Band(10, 5));
    }

    @Test
    void rollIsLinearBetweenMinAndMax() {
        Band band = new Band(10, 20);
        assertEquals(10.0, band.roll(() -> 0.0), 1e-9);
        assertEquals(15.0, band.roll(() -> 0.5), 1e-9);
        assertEquals(19.0, band.roll(() -> 0.9), 1e-9);
    }

    @Test
    void middleIsTheMidpoint() {
        assertEquals(15.0, new Band(10, 20).middle(), 1e-9);
    }
}
