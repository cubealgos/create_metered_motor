package metered_motor.menu;

/**
 * Pure geometry for the motor screen and its menu (docs/spec/domains/ui.md `UI-REQ-001`,
 * `UI-REQ-003`, `UI-REQ-004`), free of every Minecraft, Fabric and Create import so a plain
 * JUnit test can load it directly: {@link MeteredMotorMenu} itself extends Create's
 * {@code MenuBase} and cannot be class-loaded outside a running client or server. Both
 * {@link MeteredMotorMenu} and {@code MeteredMotorScreen} read these numbers instead of
 * carrying their own copies, so the real slots, the readout text and the frame the screen
 * paints around them never drift apart.
 *
 * <p>MM-18 fixed two bugs together.
 *
 * <p><b>Part 1 — the player inventory's slots were misaligned with its own frame.</b>
 * {@code MenuBase.addPlayerSlots(x, y)} sets every player slot's own position directly off that
 * one (x, y): the three main rows at {@code x + col*18, y + row*18}, the hotbar row at
 * {@code x + col*18, y + 58}. {@code AbstractSimiContainerScreen.renderPlayerInventory(graphics,
 * x, y)} blits the whole {@code PLAYER_INVENTORY} texture at that same (x, y), with the
 * "Inventory" label at {@code (x + 8, y + 6)}. The two calls must share one origin, offset by the
 * texture's own built-in inset from its corner to the first slot's icon — an inset neither method
 * documents, so it was read two independent ways and cross-checked:
 * <ol>
 *   <li>Create Fly's own {@code ToolboxMenu.addSlots()} calls {@code addPlayerSlots(8, 165)}
 *       while {@code ToolboxScreen.extractBackground} calls {@code renderPlayerInventory(graphics,
 *       leftPos, topPos + imageHeight - PLAYER.getHeight())}; with {@code imageHeight=255} and
 *       {@code PLAYER.getHeight()=108} that frame sits at {@code topPos + 147}, so row 0's slots
 *       (at {@code topPos + 165}) sit {@code (8, 18)} past the frame's own corner.</li>
 *   <li>Sampling {@code player_inventory.png} directly (Pillow, not through the atlas since this
 *       texture is its own file): the dark top/left border of the row-0 cell ends and the grey
 *       16x16 interior — where the item icon actually draws — begins at pixel {@code (8, 18)} from
 *       the texture's own top-left corner; the hotbar cell's interior begins at {@code (8, 76)},
 *       which is {@code 18 + 58} — the same {@code y + 58} {@code addPlayerSlots} itself adds for
 *       the hotbar row.</li>
 * </ol>
 * Both agree: {@code (8, 18)}. This screen's previous layout passed {@code PLAYER_INV_X} (the
 * frame's own local x, no {@code +8}) straight to {@code addPlayerSlots}, so every player slot's
 * item rendered 8px left of its own cell. The y argument
 * ({@code MAIN_INV_Y = TOP_HEIGHT + GAP + 18}) already carried the correct {@code +18}, so
 * vertical alignment was never actually broken — only horizontal. {@link #MAIN_INV_X} now carries
 * the missing {@code +8} the same way {@link #MAIN_INV_Y} already carried the {@code +18}, and
 * both are re-exported so {@code MeteredMotorMenu.addSlots()} and
 * {@code MeteredMotorScreen.extractBackground()} read the one pair of numbers instead of each
 * computing its own.
 *
 * <p><b>Part 2 — the readout was a loose nine-line column on a panel far taller than it needed to
 * be.</b> It is now one bright "Tier &lt;tier&gt; · &lt;state&gt;" line, then a four-row table of
 * label/value pairs two to a row (label dim and left, value bright and right-aligned to its own
 * half of the panel): Speed/Capacity, Efficiency/Rate, Load/Inside, and Remaining alone on the
 * last row. Five text lines at the font's own 9px line height, with no gap between them, is
 * already the largest the ≤200px window budget allows once the title strip (18px, unchanged), the
 * bottom band, the slot row (18px) and the player inventory frame (108px, Create's own fixed
 * texture) are accounted for — the bottom band was trimmed from 12px to {@link #BOTTOM_H} 6px (a
 * plain light-to-dark gradient in the source atlas, confirmed by direct inspection to carry no
 * fine detail that trimming would cut into) and the gap above the player inventory frame dropped
 * to {@link #GAP} 0 (Create's own {@code ToolboxScreen} overlaps its background and the player
 * inventory frame rather than leaving a gap at all, so touching with no gap is well inside
 * precedent) to make room; even so the window comes in at {@link #WINDOW_HEIGHT}, one pixel under
 * the 200px budget.
 *
 * <p>The atlas coordinates are {@code create_brass_compass}'s proven {@code stock_keeper.png}
 * slice (MM-6's finding), narrowed from the compass {@code EditScreen}'s 224px width to the
 * atlas's actually-opaque 208px span (MM-17's finding): sampling the atlas PNG directly shows the
 * panel and bottom bands are fully transparent (alpha 0) at columns 16..23 and 232..239 — only
 * the title band is opaque that far out. 208px (columns 24..231) is opaque end to end at every
 * row this screen uses.
 */
public final class Layout {
    private Layout() {
    }

    // ---- The five real slots (MOTOR-REQ-008, MOTOR-REQ-009) ----
    public static final int SLOTS = 5;
    public static final int SLOT_SIZE = 18;

    // ---- The window's width and the atlas source column it is blitted from (see class doc) ----
    public static final int WIDTH = 208;
    public static final int ATLAS_U = 24;

    // ---- The title strip ----
    public static final int TITLE_V = 0;
    public static final int TITLE_H = 18;
    /** The title text's y within the strip: `create_brass_compass`'s `EditScreen` offset, centred on a 9 px line. */
    public static final int TITLE_TEXT_Y = 4;

    // ---- The readout: one header line ("Tier III · Stopped"), then a four-row table ----
    public static final int FONT_LINE_HEIGHT = 9;
    /** No gap between lines: at the font's own line height, five lines already use the whole
     *  budget the window has (see class doc); Minecraft's own multi-line text commonly reads fine
     *  spaced by nothing more than the font's line height. */
    public static final int LINE_GAP = 0;
    public static final int LINE_STRIDE = FONT_LINE_HEIGHT + LINE_GAP;
    /** Padding between the title strip and the header line. */
    public static final int LINE_TOP_PADDING = 1;
    /** The header line's ("Tier III · Stopped") y, measured from the window's own top edge. */
    public static final int HEADER_Y = TITLE_H + LINE_TOP_PADDING;
    /** The table's four rows, directly under the header line. */
    public static final int TABLE_ROWS = 4;
    public static final int TABLE_START_Y = HEADER_Y + LINE_STRIDE;
    public static final int TABLE_END_Y = TABLE_START_Y + TABLE_ROWS * LINE_STRIDE;

    // ---- The table's two pair-columns: label dim and left, value bright and right-aligned to
    //      its own half of the panel, so a long value in one half never crosses into the other
    //      (UI-REQ-004). ----
    public static final int TEXT_LEFT = 8;
    public static final int COLUMN_GAP = 8;
    public static final int COLUMN_WIDTH = (WIDTH - 2 * TEXT_LEFT - COLUMN_GAP) / 2;
    public static final int LEFT_LABEL_X = TEXT_LEFT;
    public static final int LEFT_VALUE_RIGHT_X = TEXT_LEFT + COLUMN_WIDTH;
    public static final int RIGHT_LABEL_X = LEFT_VALUE_RIGHT_X + COLUMN_GAP;
    public static final int RIGHT_VALUE_RIGHT_X = WIDTH - TEXT_LEFT;

    // ---- The slot row, directly under the table ----
    /** Padding between the table's last row and the slot row. */
    public static final int SLOT_GAP = 2;
    /** The slot row's y, measured from the window's own top edge. */
    public static final int SLOT_Y = TABLE_END_Y + SLOT_GAP;
    public static final int SLOT_X = (WIDTH - SLOTS * SLOT_SIZE) / 2;
    /** `stock_keeper.png`'s own request-slot art: a warm brown-and-tan 18x18 background that
     *  reads as Create's, unlike the flat grey `AllGuiTextures.JEI_SLOT` this screen drew before. */
    public static final int SLOT_BG_U = 32;
    public static final int SLOT_BG_V = 200;

    // ---- Padding below the slot row, then the tiled body and the bottom band ----
    public static final int BOTTOM_PADDING = 1;
    public static final int PANEL_V = 48;
    public static final int PANEL_H = 20;
    /** Everything the body must hold: top padding, the header, the table, the slot gap, the slot row itself, bottom padding. */
    public static final int BODY_HEIGHT = LINE_TOP_PADDING + LINE_STRIDE + TABLE_ROWS * LINE_STRIDE + SLOT_GAP + SLOT_SIZE + BOTTOM_PADDING;
    /** As many full 20px strips as fit, plus one final partial strip for the remainder (MM-18: no
     *  longer required to divide evenly — the source strip is a fine, low-period dither with no
     *  vertical pattern a partial crop would break, confirmed by direct inspection). */
    public static final int FULL_PANELS = BODY_HEIGHT / PANEL_H;
    public static final int LAST_PANEL_H = BODY_HEIGHT - FULL_PANELS * PANEL_H;
    /** MM-18: trimmed from the compass's 12px band to 6px (its own bottom rows: the same solid
     *  dark border, just less of the lighter fill above it) to fit the ≤200px budget; see class doc. */
    public static final int BOTTOM_V = 154;
    public static final int BOTTOM_H = 6;

    /** The panel's total height: the title strip, the tiled body, and the bottom band. */
    public static final int TOP_HEIGHT = TITLE_H + BODY_HEIGHT + BOTTOM_H;

    // ---- Below the panel: the gap, then the player's own inventory frame ----
    /** MM-18: touching, not a gap — see class doc (Create's own `ToolboxScreen` overlaps here rather than leaving space). */
    public static final int GAP = 0;
    /** `AllGuiTextures.PLAYER_INVENTORY`'s own size (176x108): duplicated here, not read live, so
     *  this class stays free of Create imports; keep in sync if Create Fly's own texture resizes. */
    public static final int PLAYER_INVENTORY_WIDTH = 176;
    public static final int PLAYER_INVENTORY_HEIGHT = 108;
    /** The player inventory frame's own local x, centred under {@link #WIDTH} (MM-18's class doc, Part 1). */
    public static final int PLAYER_INV_X = (WIDTH - PLAYER_INVENTORY_WIDTH) / 2;
    /** The frame's own local y: directly under the panel. */
    public static final int FRAME_Y = TOP_HEIGHT + GAP;
    /** The first player slot's x: the frame's own corner, plus the texture's built-in 8px inset (MM-18's class doc, Part 1). */
    public static final int MAIN_INV_X = PLAYER_INV_X + 8;
    /** The first player slot's y: the frame's own corner, plus the texture's built-in 18px inset (MM-18's class doc, Part 1). */
    public static final int MAIN_INV_Y = FRAME_Y + 18;

    /** The whole window's height: the readout panel, the gap, and the player inventory frame. */
    public static final int WINDOW_HEIGHT = TOP_HEIGHT + GAP + PLAYER_INVENTORY_HEIGHT;

    static {
        if (LAST_PANEL_H < 0 || LAST_PANEL_H > PANEL_H) {
            throw new ExceptionInInitializerError("LAST_PANEL_H " + LAST_PANEL_H + " must be between 0 and " + PANEL_H);
        }
    }
}
