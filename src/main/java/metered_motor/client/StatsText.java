package metered_motor.client;

import java.util.Locale;
import metered_motor.block.MotorState;
import metered_motor.model.Tier;

/**
 * Number and enum formatting shared by every place the rolled stats or the live readout become
 * text: the item tooltip, the motor screen (`MOTOR-REQ-012`, docs/spec/domains/ui.md
 * `UI-REQ-003`) and the goggles overlay. Every number goes through {@link Locale#ROOT}
 * (`UI-REQ-007`) so a client's own locale never turns a decimal point into a comma mid-readout.
 * Pure of Minecraft, Fabric and Create on purpose: the motor screen's own sync game test calls
 * these methods directly against the block entity's real values, and the server-only game-test
 * environment must be able to load this class.
 */
public final class StatsText {
    private StatsText() {
    }

    /** The tier's name as rolled: {@code "I"}, {@code "II"} or {@code "III"}. */
    public static String tier(Tier tier) {
        return tier.name();
    }

    /** Two decimal places, e.g. efficiency or the rate at full load. */
    public static String twoDecimals(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    /** A fraction 0..1 as a whole-number percentage, e.g. {@code 0.5} becomes {@code "50"}. */
    public static String wholePercent(double fraction) {
        return String.format(Locale.ROOT, "%.0f", fraction * 100.0);
    }

    /** The state's lower-case name, matching a {@code screen.metered_motor.state.<name>} or {@code goggles.metered_motor.state.<name>} key's suffix. */
    public static String state(MotorState state) {
        return state.name().toLowerCase(Locale.ROOT);
    }

    /**
     * Minutes and seconds until the inventory runs out at the current load, floored to the
     * second; {@code null} when {@code secondsRemaining} is negative (not running, or drawing no
     * load: {@link metered_motor.block.MeteredMotorBlockEntity#secondsRemaining()}), for the
     * caller to show the idle text instead (`MOTOR-DEC-001`).
     */
    public static String remaining(double secondsRemaining) {
        if (secondsRemaining < 0) {
            return null;
        }
        long total = (long) Math.floor(secondsRemaining);
        long minutes = total / 60;
        long seconds = total % 60;
        return String.format(Locale.ROOT, "%dm %02ds", minutes, seconds);
    }
}
