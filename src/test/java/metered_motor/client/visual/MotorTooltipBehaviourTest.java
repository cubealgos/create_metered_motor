package metered_motor.client.visual;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import metered_motor.block.MotorState;
import metered_motor.client.visual.MotorTooltipBehaviour.Row;
import metered_motor.model.Stats;
import metered_motor.model.Tier;
import org.junit.jupiter.api.Test;

/**
 * {@link MotorTooltipBehaviour#rows} builds the goggle readout as plain data, without touching
 * {@code LangBuilder} or {@code Minecraft.getInstance().font} (only a running client provides
 * either), so it is checkable headless (docs/spec/operations/testing.md, MM-19's acceptance
 * criteria): the header first, every row still at its indent, and the row keys in order, for a
 * running and a stopped motor. MM-21 (`decisions/DEC-009-fixed-tiers.md`): no efficiency row, and
 * the capacity row is omitted when {@code omitCapacity} is {@code true} — Create's own "Generator
 * Stats" line already showed the same number (MOTOR-REQ-012, UI-REQ-005).
 */
final class MotorTooltipBehaviourTest {
    private static final List<String> BODY_ROW_KEYS_WITH_CAPACITY = List.of(
        "goggles.metered_motor.row.tier",
        "goggles.metered_motor.row.speed",
        "goggles.metered_motor.row.capacity",
        "goggles.metered_motor.row.rate",
        "goggles.metered_motor.row.load",
        "goggles.metered_motor.row.inside",
        "goggles.metered_motor.row.remaining",
        "goggles.metered_motor.row.state");
    private static final List<String> BODY_ROW_KEYS_WITHOUT_CAPACITY = List.of(
        "goggles.metered_motor.row.tier",
        "goggles.metered_motor.row.speed",
        "goggles.metered_motor.row.rate",
        "goggles.metered_motor.row.load",
        "goggles.metered_motor.row.inside",
        "goggles.metered_motor.row.remaining",
        "goggles.metered_motor.row.state");

    @Test
    void aRunningMotorsHeaderComesFirstThenEveryRowIndentedInOrder() {
        Stats stats = new Stats(1, Tier.III);
        List<Row> rows = MotorTooltipBehaviour.rows(stats, MotorState.RUNNING, 0.0, 0, -1, false);

        assertEquals("goggles.metered_motor.title", rows.get(0).labelKey());
        assertEquals(0, rows.get(0).indent(), "the header sits at indent 0 so the goggles icon never covers it");

        List<String> bodyKeys = rows.stream().skip(1).map(Row::labelKey).toList();
        assertEquals(BODY_ROW_KEYS_WITH_CAPACITY, bodyKeys, "the row keys must appear in the ticket's declared order, no efficiency row");
        assertTrue(rows.stream().skip(1).allMatch(row -> row.indent() == 1), "every body row is indented one level under the header");

        Row remaining = rows.get(7);
        assertTrue(remaining.valueIsKey(), "no load at zero load is a translated phrase, not a formatted number");
        assertEquals("goggles.metered_motor.no_load", remaining.value());

        Row state = rows.get(8);
        assertTrue(state.valueIsKey());
        assertEquals("goggles.metered_motor.state.running", state.value());

        Row speed = rows.get(2);
        assertEquals("64", speed.value());
        assertEquals("goggles.metered_motor.unit.rpm", speed.unitKey());
    }

    @Test
    void theCapacityRowIsOmittedWhenCreateAlreadyShowedIt() {
        Stats stats = new Stats(1, Tier.III);
        List<Row> rows = MotorTooltipBehaviour.rows(stats, MotorState.RUNNING, 0.0, 0, -1, true);

        List<String> bodyKeys = rows.stream().skip(1).map(Row::labelKey).toList();
        assertEquals(BODY_ROW_KEYS_WITHOUT_CAPACITY, bodyKeys, "no capacity row when Create's own line already showed it");
        assertTrue(bodyKeys.stream().noneMatch("goggles.metered_motor.row.capacity"::equals));
    }

    @Test
    void aStoppedMotorStillShowsTheHeaderFirstAndReportsNoLoad() {
        Stats stats = Stats.of(Tier.I);
        List<Row> rows = MotorTooltipBehaviour.rows(stats, MotorState.STOPPED, 0.0, 0, -1, false);

        assertEquals("goggles.metered_motor.title", rows.get(0).labelKey());
        assertEquals(0, rows.get(0).indent());
        assertEquals(BODY_ROW_KEYS_WITH_CAPACITY, rows.stream().skip(1).map(Row::labelKey).toList());

        Row state = rows.get(8);
        assertEquals("goggles.metered_motor.state.stopped", state.value());
        assertFalse(state.unitKey() != null, "the state row carries no unit");

        Row remaining = rows.get(7);
        assertEquals("goggles.metered_motor.no_load", remaining.value());
    }

    @Test
    void aRunningMotorDrawingDownItsInventoryReportsTheTimeRemainingInsteadOfNoLoad() {
        Stats stats = new Stats(1, Tier.III);
        List<Row> rows = MotorTooltipBehaviour.rows(stats, MotorState.RUNNING, 1.0, 64, 252.0, false);

        Row remaining = rows.get(7);
        assertFalse(remaining.valueIsKey(), "a running motor with a positive countdown shows the formatted time, not a key");
        assertEquals("4m 12s", remaining.value());
        assertEquals(1, remaining.indent());
    }
}
