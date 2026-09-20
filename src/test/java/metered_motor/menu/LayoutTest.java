package metered_motor.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * The motor screen's layout arithmetic, checked without loading any Minecraft, Fabric or Create
 * class: {@link Layout} is pure on purpose (its own class doc), because {@link MeteredMotorMenu}
 * extends Create's {@code MenuBase} and cannot be class-loaded in a plain JVM unit test
 * (docs/spec/domains/ui.md `UI-REQ-001`, `UI-REQ-003`, `UI-REQ-004`, MM-17's acceptance criteria).
 */
final class LayoutTest {
    private static final int GUI_SCALE_4_BUDGET = 270;

    @Test
    void theWholeWindowFitsA1080pClientAtGuiScale4() {
        assertEquals(Layout.TOP_HEIGHT + Layout.GAP + Layout.PLAYER_INVENTORY_HEIGHT, Layout.WINDOW_HEIGHT);
        assertTrue(
            Layout.WINDOW_HEIGHT <= GUI_SCALE_4_BUDGET,
            "TOP_HEIGHT + GAP + player inventory height must be <= " + GUI_SCALE_4_BUDGET + " but was " + Layout.WINDOW_HEIGHT);
    }

    @Test
    void theTitleTextSitsFullyInsideTheTitleStrip() {
        assertTrue(Layout.TITLE_TEXT_Y >= 0, "the title text's y must not start above the strip");
        assertTrue(
            Layout.TITLE_TEXT_Y + Layout.FONT_LINE_HEIGHT <= Layout.TITLE_H,
            "the title text must not run past the " + Layout.TITLE_H + "px strip: y=" + Layout.TITLE_TEXT_Y);
    }

    @Test
    void theTiledBodyHoldsExactlyTheReadoutAndTheSlotRow() {
        // No leftover: the tiled 20px strips divide the body evenly (Layout's own static guard
        // throws otherwise), so nothing is chopped off and no strip is wasted.
        assertEquals(0, Layout.BODY_HEIGHT % Layout.PANEL_H);
        assertEquals(Layout.PANELS * Layout.PANEL_H, Layout.BODY_HEIGHT);

        // The nine lines, the gap and the slot row must fit inside the body, with only the
        // declared padding left over below the slot row — not the large empty field MM-17 found.
        int slotRowBottom = Layout.SLOT_Y + Layout.SLOT_SIZE;
        int bodyBottom = Layout.TITLE_H + Layout.BODY_HEIGHT;
        assertEquals(
            Layout.BOTTOM_PADDING, bodyBottom - slotRowBottom,
            "the slack below the slot row must be exactly the declared bottom padding, not an empty field");
    }

    @Test
    void theSlotRowIsCentredAndDirectlyUnderTheReadout() {
        int slotsWidth = Layout.SLOTS * Layout.SLOT_SIZE;
        assertEquals(Layout.SLOT_X, Layout.WIDTH - Layout.SLOT_X - slotsWidth, "the slot row must be centred in the window");

        int lastLineBottom = Layout.LINE_START_Y + Layout.LINE_COUNT * Layout.LINE_STRIDE;
        assertEquals(Layout.SLOT_GAP, Layout.SLOT_Y - lastLineBottom, "the slot row must sit directly under the ninth line, one declared gap below it");
    }

    @Test
    void thePlayerInventoryIsCentredUnderTheWindow() {
        assertEquals(
            Layout.PLAYER_INV_X, Layout.WIDTH - Layout.PLAYER_INV_X - Layout.PLAYER_INVENTORY_WIDTH,
            "the player inventory frame must be centred under the window");
    }
}
