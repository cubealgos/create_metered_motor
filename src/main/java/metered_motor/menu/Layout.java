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
 * <p>MM-17: the previous layout tiled seven 20 px body strips (140 px) under a fixed
 * {@code LINE_START_Y}/{@code SLOT_Y} that left roughly 22 px of empty panel between the ninth
 * readout line and the slot row, and the resulting window (170 + 4 + 108 = 282 px) overran the
 * ≤270 px budget a 1080p client has at GUI scale 4, which is what pushed the title off the top
 * of the screen when Minecraft centred it. This class instead sizes the body to exactly what the
 * nine readout lines and the slot row need, tiled in as many 20 px strips as that takes.
 *
 * <p>The atlas coordinates are {@code create_brass_compass}'s proven {@code stock_keeper.png}
 * slice (MM-6's finding, this ticket's Constraints section), narrowed from the compass
 * {@code EditScreen}'s 224 px width to the atlas's actually-opaque 208 px span: sampling the
 * atlas PNG directly shows the panel and bottom bands are fully transparent (alpha 0) at
 * columns 16..23 and 232..239 — only the title band is opaque that far out. Blitting the panel
 * and bottom bands at the wider 224 px left an 8 px hole at each edge that read as a seam over
 * any backdrop lighter than the vanilla dark blur (exactly the outdoor screenshot in the
 * ticket). 208 px (columns 24..231) is opaque end to end at every row this screen uses.
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

    // ---- The nine readout lines (`UI-REQ-003`) ----
    public static final int LINE_COUNT = 9;
    public static final int FONT_LINE_HEIGHT = 9;
    public static final int LINE_GAP = 1;
    public static final int LINE_STRIDE = FONT_LINE_HEIGHT + LINE_GAP;
    /** Padding between the title strip and the first readout line. */
    public static final int LINE_TOP_PADDING = 4;
    /** The first readout line's y, measured from the window's own top edge. */
    public static final int LINE_START_Y = TITLE_H + LINE_TOP_PADDING;

    // ---- The slot row, directly under the readout ----
    /** Padding between the ninth readout line and the slot row. */
    public static final int SLOT_GAP = 4;
    /** The slot row's y, measured from the window's own top edge. */
    public static final int SLOT_Y = LINE_START_Y + LINE_COUNT * LINE_STRIDE + SLOT_GAP;
    public static final int SLOT_X = (WIDTH - SLOTS * SLOT_SIZE) / 2;
    /** `stock_keeper.png`'s own request-slot art: a warm brown-and-tan 18x18 background that
     *  reads as Create's, unlike the flat grey `AllGuiTextures.JEI_SLOT` this screen drew before. */
    public static final int SLOT_BG_U = 32;
    public static final int SLOT_BG_V = 200;

    // ---- Padding below the slot row, then the tiled body and the bottom band ----
    public static final int BOTTOM_PADDING = 4;
    public static final int PANEL_V = 48;
    public static final int PANEL_H = 20;
    /** Everything the body must hold: top padding, nine lines, the slot gap, the slot row itself, bottom padding. */
    public static final int BODY_HEIGHT = LINE_TOP_PADDING + LINE_COUNT * LINE_STRIDE + SLOT_GAP + SLOT_SIZE + BOTTOM_PADDING;
    /** As many 20 px strips as the body needs; the static block below guards the division. */
    public static final int PANELS = BODY_HEIGHT / PANEL_H;
    public static final int BOTTOM_V = 148;
    public static final int BOTTOM_H = 12;

    /** The panel's total height: the title strip, the tiled body, and the bottom band. */
    public static final int TOP_HEIGHT = TITLE_H + PANELS * PANEL_H + BOTTOM_H;

    // ---- Below the panel: the gap, then the player's own inventory frame ----
    /** The gap every `AbstractSimiContainerScreen` leaves above the player inventory frame. */
    public static final int GAP = 4;
    /** `AllGuiTextures.PLAYER_INVENTORY`'s own size (176x108): duplicated here, not read live, so
     *  this class stays free of Create imports; keep in sync if Create Fly's own texture resizes. */
    public static final int PLAYER_INVENTORY_WIDTH = 176;
    public static final int PLAYER_INVENTORY_HEIGHT = 108;
    public static final int PLAYER_INV_X = (WIDTH - PLAYER_INVENTORY_WIDTH) / 2;
    public static final int MAIN_INV_Y = TOP_HEIGHT + GAP + 18;

    /** The whole window's height: the readout panel, the gap, and the player inventory frame. */
    public static final int WINDOW_HEIGHT = TOP_HEIGHT + GAP + PLAYER_INVENTORY_HEIGHT;

    static {
        if (BODY_HEIGHT % PANEL_H != 0) {
            throw new ExceptionInInitializerError(
                "BODY_HEIGHT " + BODY_HEIGHT + " does not tile evenly into " + PANEL_H + " px strips");
        }
    }
}
