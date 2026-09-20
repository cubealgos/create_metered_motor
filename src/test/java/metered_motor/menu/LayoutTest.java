package metered_motor.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * The motor screen's layout arithmetic, checked without loading any Minecraft, Fabric or Create
 * class: {@link Layout} is pure on purpose (its own class doc), because {@link MeteredMotorMenu}
 * extends Create's {@code MenuBase} and cannot be class-loaded in a plain JVM unit test
 * (docs/spec/domains/ui.md `UI-REQ-001`, `UI-REQ-003`, `UI-REQ-004`, MM-17's and MM-18's
 * acceptance criteria).
 */
final class LayoutTest {
    /** MM-18's own budget, tighter than MM-17's 1080p/GUI-scale-4 headroom. */
    private static final int WINDOW_BUDGET = 200;
    /** The `player_inventory.png` texture's own built-in inset from its corner to the first
     *  slot's icon (`Layout`'s class doc, MM-18 Part 1): read two independent ways (Create Fly's
     *  own `ToolboxMenu`/`ToolboxScreen` call sites, and sampling the texture PNG directly for
     *  where the row-0 cell's grey interior begins) and cross-checked. Duplicated here as a
     *  literal, not read off {@link Layout#MAIN_INV_X}/{@link Layout#MAIN_INV_Y}, so this test
     *  would actually fail if `Layout`'s formula ever drifted from the real texture.
     */
    private static final int FRAME_INSET_X = 8;
    private static final int FRAME_INSET_Y = 18;
    private static final int SLOT_PITCH = 18;
    private static final int HOTBAR_OFFSET = 58;

    @Test
    void theWholeWindowFitsTheTwoHundredPixelBudget() {
        assertEquals(Layout.TOP_HEIGHT + Layout.GAP + Layout.PLAYER_INVENTORY_HEIGHT, Layout.WINDOW_HEIGHT);
        assertTrue(
            Layout.WINDOW_HEIGHT <= WINDOW_BUDGET,
            "TOP_HEIGHT + GAP + player inventory height must be <= " + WINDOW_BUDGET + " but was " + Layout.WINDOW_HEIGHT);
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
        // MM-18: the body no longer needs to divide evenly into 20px strips (Layout's class doc)
        // — a final partial strip covers the remainder — but the full strips plus that remainder
        // must still add up to exactly the body, and the remainder must be a real partial strip,
        // not a whole extra one or a negative one.
        assertEquals(Layout.FULL_PANELS * Layout.PANEL_H + Layout.LAST_PANEL_H, Layout.BODY_HEIGHT);
        assertTrue(Layout.LAST_PANEL_H >= 0 && Layout.LAST_PANEL_H < Layout.PANEL_H, "LAST_PANEL_H must be a partial strip: " + Layout.LAST_PANEL_H);

        // The header, the table and the slot row must fit inside the body, with only the
        // declared bottom padding left over — not an empty field.
        int slotRowBottom = Layout.SLOT_Y + Layout.SLOT_SIZE;
        int bodyBottom = Layout.TITLE_H + Layout.BODY_HEIGHT;
        assertEquals(
            Layout.BOTTOM_PADDING, bodyBottom - slotRowBottom,
            "the slack below the slot row must be exactly the declared bottom padding, not an empty field");
    }

    @Test
    void theTableIsFourRowsDirectlyUnderTheHeaderLine() {
        assertEquals(4, Layout.TABLE_ROWS);
        assertEquals(Layout.HEADER_Y + Layout.LINE_STRIDE, Layout.TABLE_START_Y, "the table starts one line below the header, with no extra gap");
        assertEquals(Layout.TABLE_START_Y + Layout.TABLE_ROWS * Layout.LINE_STRIDE, Layout.TABLE_END_Y);
    }

    @Test
    void theTablesTwoColumnsFitThePanelWithoutOverlapping(){
        assertTrue(Layout.LEFT_LABEL_X < Layout.LEFT_VALUE_RIGHT_X, "the left column's value edge must sit right of its label");
        assertTrue(
            Layout.LEFT_VALUE_RIGHT_X + Layout.COLUMN_GAP <= Layout.RIGHT_LABEL_X,
            "the two columns must not overlap: left value edge " + Layout.LEFT_VALUE_RIGHT_X + ", right label starts " + Layout.RIGHT_LABEL_X);
        assertTrue(
            Layout.RIGHT_VALUE_RIGHT_X <= Layout.WIDTH - Layout.TEXT_LEFT,
            "the right column's value edge must not run past the panel (UI-REQ-004): " + Layout.RIGHT_VALUE_RIGHT_X);
        assertEquals(Layout.WIDTH - Layout.TEXT_LEFT, Layout.RIGHT_VALUE_RIGHT_X, "the right column's value edge is the panel's own right text edge");
    }

    @Test
    void theSlotRowIsCentredAndDirectlyUnderTheTable() {
        int slotsWidth = Layout.SLOTS * Layout.SLOT_SIZE;
        assertEquals(Layout.SLOT_X, Layout.WIDTH - Layout.SLOT_X - slotsWidth, "the slot row must be centred in the window");
        assertEquals(Layout.SLOT_GAP, Layout.SLOT_Y - Layout.TABLE_END_Y, "the slot row must sit directly under the table, one declared gap below it");
    }

    @Test
    void thePlayerInventoryIsCentredUnderTheWindow() {
        assertEquals(
            Layout.PLAYER_INV_X, Layout.WIDTH - Layout.PLAYER_INV_X - Layout.PLAYER_INVENTORY_WIDTH,
            "the player inventory frame must be centred under the window");
    }

    /**
     * MM-18's Part 1 acceptance criterion: every player-inventory slot position the menu will use
     * falls inside the corresponding cell of the frame the screen draws — both read off the one
     * shared origin ({@link Layout#PLAYER_INV_X}, {@link Layout#FRAME_Y}), offset by the same
     * (8, 18) inset the texture itself uses (see the class-level constants' own doc).
     */
    @Test
    void everyPlayerSlotOriginLiesInsideItsDrawnCell() {
        assertEquals(Layout.PLAYER_INV_X + FRAME_INSET_X, Layout.MAIN_INV_X, "the first player slot's x must be the frame's corner plus the texture's own inset");
        assertEquals(Layout.FRAME_Y + FRAME_INSET_Y, Layout.MAIN_INV_Y, "the first player slot's y must be the frame's corner plus the texture's own inset");

        int frameRight = Layout.PLAYER_INV_X + Layout.PLAYER_INVENTORY_WIDTH;
        int frameBottom = Layout.FRAME_Y + Layout.PLAYER_INVENTORY_HEIGHT;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotX = Layout.MAIN_INV_X + col * SLOT_PITCH;
                int slotY = Layout.MAIN_INV_Y + row * SLOT_PITCH;
                int cellLeft = Layout.PLAYER_INV_X + FRAME_INSET_X + col * SLOT_PITCH - 1;
                int cellTop = Layout.FRAME_Y + FRAME_INSET_Y + row * SLOT_PITCH - 1;
                assertTrue(
                    slotX >= cellLeft && slotX < cellLeft + SLOT_PITCH && slotY >= cellTop && slotY < cellTop + SLOT_PITCH,
                    "main row " + row + " col " + col + " slot (" + slotX + "," + slotY + ") must sit inside its cell");
                assertTrue(slotX + 16 <= frameRight, "main row " + row + " col " + col + " slot must not run past the frame's right edge");
            }
        }
        for (int col = 0; col < 9; col++) {
            int slotX = Layout.MAIN_INV_X + col * SLOT_PITCH;
            int slotY = Layout.MAIN_INV_Y + HOTBAR_OFFSET;
            int cellLeft = Layout.PLAYER_INV_X + FRAME_INSET_X + col * SLOT_PITCH - 1;
            int cellTop = Layout.FRAME_Y + FRAME_INSET_Y + HOTBAR_OFFSET - 1;
            assertTrue(
                slotX >= cellLeft && slotX < cellLeft + SLOT_PITCH && slotY >= cellTop && slotY < cellTop + SLOT_PITCH,
                "hotbar col " + col + " slot (" + slotX + "," + slotY + ") must sit inside its cell");
            assertTrue(slotY + 16 <= frameBottom, "the hotbar row must not run past the frame's bottom edge");
        }
    }
}
