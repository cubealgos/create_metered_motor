package metered_motor.client.visual;

import com.zurrtum.create.client.catnip.lang.LangBuilder;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.GeneratingKineticTooltipBehaviour;
import java.util.ArrayList;
import java.util.List;
import metered_motor.block.MeteredMotorBlockEntity;
import metered_motor.block.MotorState;
import metered_motor.client.StatsText;
import metered_motor.model.Stats;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * The goggles readout, appended after Create's own kinetic lines (speed, stress capacity) that
 * {@link GeneratingKineticTooltipBehaviour} already draws for any generating kinetic source
 * (MOTOR-REQ-012, UI-REQ-005, UI-UC-002). {@link MotorVisuals} attaches this to the block entity
 * type through {@code com.zurrtum.create.api.behaviour.BlockEntityBehaviour.addClient}, the same
 * client-side registry Create Fly's own {@code AllBlockEntityBehaviours} populates for its kinetic
 * blocks (docs/spec/README.md Verification 10).
 *
 * <p>The readout follows the shape Create's own goggle text uses (MM-19, read from the jar):
 * {@code CreateLang}/{@link LangBuilder}'s {@code forGoggles(List)} indents a header line by four
 * characters' width so the goggles icon that overlays the tooltip's corner never covers it
 * ({@code GeneratingKineticTooltipBehaviour.addToGoggleTooltip}'s {@code gui.goggles.generator_stats}
 * line, {@code KineticTooltipBehaviour.addToGoggleTooltip}'s {@code gui.goggles.kinetic_stats});
 * {@code forGoggles(List, 1)} indents a body row one character further, the way
 * {@code tooltip.capacityProvided}'s value line and {@code StressGaugeTooltipBehaviour}'s
 * percentage line are. Create styles a row's label {@link ChatFormatting#GRAY} and its number
 * {@link ChatFormatting#AQUA} (its dimmer annotations, e.g. "at current speed",
 * {@link ChatFormatting#DARK_GRAY}); this readout's rows follow the same three-colour scheme,
 * collapsed onto one line per row rather than Create's own label-then-value pair of lines, since
 * nine rows of that shape would run twice as long as Create's own two- or three-line blocks.
 *
 * <p>Every number and enum name goes through {@link StatsText}, the same formatting the item
 * tooltip and the motor screen (MM-6) use, so the readout reads identically on every surface
 * (UI-REQ-007). {@link #rows} builds the readout as plain data — no {@link LangBuilder}, no
 * {@code Minecraft.getInstance().font} that {@code forGoggles} needs and only a running client
 * provides — so it is unit-testable headless (docs/spec/operations/testing.md);
 * {@link #addRow} is the only part that touches Create's client-only builders.
 */
public final class MotorTooltipBehaviour extends GeneratingKineticTooltipBehaviour<MeteredMotorBlockEntity> {
    /**
     * The namespace {@link LangBuilder}'s constructor takes, consulted only by its own
     * {@code translate(String)} — unused here, since every key below is already the full literal
     * key an {@code en_us} entry carries, not one Create's {@code create.} prefix would apply to.
     * Named rather than blank so a future call to {@code translate} on one of these builders fails
     * loudly (a wrongly-prefixed key) instead of silently looking up {@code .<key>}.
     */
    private static final String NAMESPACE = "metered_motor";

    public MotorTooltipBehaviour(MeteredMotorBlockEntity blockEntity) {
        super(blockEntity);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        // Create's own lines first (speed, stress capacity added by the superclass); this motor's
        // readout is always shown regardless of whether stress impact is enabled, so the boolean
        // super() returns is not the gate for showing the readout at all — only for whether this
        // row set repeats the capacity Create's own "Generator Stats" line just showed
        // (GeneratingKineticTooltipBehaviour.addToGoggleTooltip returns true exactly when it drew
        // that line, confirmed by full bytecode disassembly: it returns early without drawing when
        // stress impact is disabled or the block's own added capacity is zero, MOTOR-REQ-012,
        // UI-REQ-005, `DEC-009`).
        boolean createShowedCapacity = super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        MeteredMotorBlockEntity motor = blockEntity;
        Stats stats = motor.stats();
        for (Row row : rows(stats, motor.state(), motor.load(), motor.emeraldsInside(), motor.secondsRemaining(), createShowedCapacity)) {
            addRow(tooltip, row);
        }
        return true;
    }

    /**
     * One line of the readout before {@link #addRow} turns it into a styled, indented
     * {@link Component}: a header carries only {@code labelKey}, at indent 0; a body row carries
     * a formatted {@code value} — or, when {@code valueIsKey}, a second translation key to
     * resolve instead, for the state name and "no load" — and an optional {@code unitKey}, always
     * at indent 1 (MM-19).
     */
    record Row(String labelKey, String value, boolean valueIsKey, String unitKey, int indent) {
        static Row header(String labelKey) {
            return new Row(labelKey, null, false, null, 0);
        }

        static Row value(String labelKey, String value, String unitKey) {
            return new Row(labelKey, value, false, unitKey, 1);
        }

        static Row translatedValue(String labelKey, String valueKey) {
            return new Row(labelKey, valueKey, true, null, 1);
        }
    }

    /**
     * The readout's lines in display order, the header first (MM-19, MOTOR-REQ-012, UI-REQ-005,
     * UI-REQ-007). No efficiency row (`DEC-009`: nothing is rolled, so it never existed as a
     * per-motor stat again); the capacity row is skipped entirely when {@code omitCapacity} is
     * {@code true} — Create's own "Generator Stats" line already showed the same number
     * (`MOTOR-REQ-012`, `UI-REQ-005`). Every label and unit key named here has an {@code en_us}
     * entry ({@code SourceSurfaceTest}); every value goes through {@link StatsText}, the same
     * formatting the item tooltip and the motor screen use. Pure of Create and Minecraft's
     * client classes on purpose: a unit test calls this directly.
     */
    static List<Row> rows(Stats stats, MotorState state, double load, int emeraldsInside, double secondsRemaining, boolean omitCapacity) {
        List<Row> rows = new ArrayList<>();
        rows.add(Row.header("goggles.metered_motor.title"));
        rows.add(Row.value("goggles.metered_motor.row.tier", StatsText.tier(stats.tier()), null));
        rows.add(Row.value("goggles.metered_motor.row.speed", String.valueOf(stats.rpm()), "goggles.metered_motor.unit.rpm"));
        if (!omitCapacity) {
            rows.add(Row.value("goggles.metered_motor.row.capacity", String.valueOf(stats.capacity()), "goggles.metered_motor.unit.su"));
        }
        rows.add(
            Row.value(
                "goggles.metered_motor.row.rate", StatsText.twoDecimals(stats.ratePerMinute()), "goggles.metered_motor.unit.rate"));
        rows.add(Row.value("goggles.metered_motor.row.load", StatsText.wholePercent(load), "goggles.metered_motor.unit.percent"));
        rows.add(
            Row.value("goggles.metered_motor.row.inside", String.valueOf(emeraldsInside), "goggles.metered_motor.unit.emeralds"));
        String remaining = StatsText.remaining(secondsRemaining);
        rows.add(
            remaining == null
                ? Row.translatedValue("goggles.metered_motor.row.remaining", "goggles.metered_motor.no_load")
                : Row.value("goggles.metered_motor.row.remaining", remaining, null));
        rows.add(
            Row.translatedValue("goggles.metered_motor.row.state", "goggles.metered_motor.state." + StatsText.state(state)));
        return rows;
    }

    /**
     * One {@link Row} through Create's own goggle builders: a header through
     * {@code forGoggles(tooltip)}, unstyled the way {@code gui.goggles.generator_stats} and
     * {@code gui.goggles.kinetic_stats} are (no {@code style} call on either, read from the jar);
     * a body row's label styled {@link ChatFormatting#GRAY} the way {@code tooltip.capacityProvided}
     * is, its value {@link ChatFormatting#AQUA} the way {@code generic.unit.stress}'s number is and
     * its unit {@link ChatFormatting#DARK_GRAY} the way the "at current speed" annotation is,
     * through {@code forGoggles(tooltip, 1)} (MM-19).
     */
    private static void addRow(List<Component> tooltip, Row row) {
        if (row.indent() == 0) {
            new LangBuilder(NAMESPACE).add(Component.translatable(row.labelKey())).forGoggles(tooltip);
            return;
        }
        LangBuilder value = row.valueIsKey()
            ? new LangBuilder(NAMESPACE).add(Component.translatable(row.value()))
            : new LangBuilder(NAMESPACE).text(row.value());
        value.style(ChatFormatting.AQUA);
        if (row.unitKey() != null) {
            value.add(new LangBuilder(NAMESPACE).add(Component.translatable(row.unitKey())).style(ChatFormatting.DARK_GRAY));
        }
        new LangBuilder(NAMESPACE)
            .add(Component.translatable(row.labelKey()))
            .style(ChatFormatting.GRAY)
            .space()
            .add(value)
            .forGoggles(tooltip, 1);
    }
}
