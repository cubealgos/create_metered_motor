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

    /**
     * MM-20's Part 2 acceptance criterion: the player inventory frame is actually cropped, not
     * merely documented as such, and the crop still leaves room for every real slot cell (the
     * hotbar row, the lowest one) — {@link #everyPlayerSlotOriginLiesInsideItsDrawnCell} already
     * checks the slots themselves against {@link Layout#PLAYER_INVENTORY_HEIGHT}; this test checks
     * the crop against the source texture's own full height instead, so a regression that quietly
     * widened the crop back toward 108 (redrawing the triangle) would be caught even if it somehow
     * still left the slots inside the (now taller) frame.
     */
    @Test
    void thePlayerInventoryFrameIsCroppedAboveItsOwnPointerTriangle() {
        assertTrue(
            Layout.PLAYER_INVENTORY_HEIGHT < Layout.PLAYER_INVENTORY_TEXTURE_HEIGHT,
            "the blitted frame height must be shorter than the source texture's own height, or the pointer triangle is back");
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
    void theTableIsThreeRowsDirectlyUnderTheHeaderLine() {
        // MM-21: 4 -> 3, the efficiency row dropped (decisions/DEC-009-fixed-tiers.md); the three
        // rows regroup with no leftover single-column row: Speed/Capacity, Rate/Load, Inside/Remaining.
        assertEquals(3, Layout.TABLE_ROWS);
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

    /**
     * A reviewer's own acceptance check on the mock (Kevin, 2026-09-20, after `just client`):
     * both columns' right-aligned value edges must sit at the panel's own inner text margin — the
     * same 8px inset {@link Layout#LEFT_LABEL_X} uses from the left edge — never past it, so a
     * truncated-or-not value can never run past the panel's own right frame edge.
     */
    @Test
    void bothColumnsRightEdgesSitAtOrInsideThePanelsOwnMargin() {
        int panelInnerRightEdge = Layout.WIDTH - Layout.TEXT_LEFT;
        assertTrue(
            Layout.LEFT_VALUE_RIGHT_X <= panelInnerRightEdge,
            "the left column's value edge must not run past the panel's own margin: " + Layout.LEFT_VALUE_RIGHT_X);
        assertTrue(
            Layout.RIGHT_VALUE_RIGHT_X <= panelInnerRightEdge,
            "the right column's value edge must not run past the panel's own margin: " + Layout.RIGHT_VALUE_RIGHT_X);
        assertEquals(
            panelInnerRightEdge, Layout.RIGHT_VALUE_RIGHT_X,
            "the right column's value edge is exactly the panel's own inner margin, the same inset the left column uses");
    }

    @Test
    void theSlotRowIsCentredAndDirectlyUnderTheTable() {
        int slotsWidth = Layout.SLOTS * Layout.SLOT_SIZE;
        assertEquals(Layout.SLOT_X, Layout.WIDTH - Layout.SLOT_X - slotsWidth, "the slot row must be centred in the window");
        assertEquals(Layout.SLOT_GAP, Layout.SLOT_Y - Layout.TABLE_END_Y, "the slot row must sit directly under the table, one declared gap below it");
    }

    /**
     * MM-20's own acceptance criterion: an explicit assertion on the four padding insets Kevin's
     * third client check asked for, each checked against the ticket's own wording rather than
     * just re-deriving {@link Layout}'s formulas (which would pass trivially even if every
     * constant regressed back toward MM-18's cramped numbers).
     */
    @Test
    void theFourMM20PaddingInsetsAreAsWide() {
        assertEquals(12, Layout.TEXT_LEFT, "the side margin must be 12px (MM-20: 8 -> 12)");
        assertTrue(Layout.LINE_TOP_PADDING >= 3, "the header must sit at least 3px below the title strip's own bottom edge");
        assertEquals(Layout.LINE_STRIDE, Layout.SLOT_GAP, "the slot row must sit a full line's gap below the table, not a token few pixels");
        assertTrue(Layout.BOTTOM_PADDING >= 2, "the slot row and the bottom band must keep at least a couple of pixels apart");
    }

    /**
     * MM-20's own acceptance criterion: no content element's x lies inside the new left/right
     * padding — every column's text and the slot row itself must sit at or inside the panel's own
     * {@link Layout#TEXT_LEFT} margin on both sides.
     */
    @Test
    void noContentXLiesWithinTheNewSideMargins() {
        int marginRight = Layout.WIDTH - Layout.TEXT_LEFT;
        assertTrue(Layout.LEFT_LABEL_X >= Layout.TEXT_LEFT, "the left column's label must not start inside the left margin");
        assertTrue(Layout.RIGHT_VALUE_RIGHT_X <= marginRight, "the right column's value edge must not run past the right margin");
        assertTrue(Layout.SLOT_X >= Layout.TEXT_LEFT, "the slot row must not start inside the left margin: " + Layout.SLOT_X);
        assertTrue(
            Layout.SLOT_X + Layout.SLOTS * Layout.SLOT_SIZE <= marginRight,
            "the slot row must not run past the right margin: " + (Layout.SLOT_X + Layout.SLOTS * Layout.SLOT_SIZE));
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
