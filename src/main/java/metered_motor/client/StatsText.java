package metered_motor.client;

import java.util.Locale;

/**
 * Number formatting shared by the item tooltip and the goggles overlay, so both surfaces read the
 * same numbers the same way (UI-REQ-007).
 */
public final class StatsText {
    private StatsText() {
    }

    /** Two decimals, e.g. an efficiency multiplier or a rate in emeralds per minute. */
    public static String decimal(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    /** A fraction in [0, 1] as a whole-number percentage. */
    public static String percent(double fraction) {
        return String.format(Locale.ROOT, "%.0f", fraction * 100.0);
    }

    /** Seconds as {@code m:ss}; the caller is responsible for a negative (not running) fallback. */
    public static String duration(double seconds) {
        long whole = Math.max(0, Math.round(seconds));
        long minutes = whole / 60;
        long secs = whole % 60;
        return String.format(Locale.ROOT, "%d:%02d", minutes, secs);
    }
}
