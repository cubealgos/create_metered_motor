package metered_motor.client.screen;

import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.menu.AbstractSimiContainerScreen;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import java.util.ArrayList;
import java.util.List;
import metered_motor.block.MeteredMotorBlockEntity;
import metered_motor.client.StatsText;
import metered_motor.menu.Layout;
import metered_motor.menu.MeteredMotorMenu;
import metered_motor.model.Stats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * The motor screen (docs/spec/domains/ui.md `UI-UC-001`, `UI-REQ-001`..`UI-REQ-004`,
 * `UI-REQ-007`, `UI-REQ-008`): Create Fly's five slots wrapping the block entity's own container
 * and the player's inventory, drawn on Create's own textures (`UI-DEC-001`).
 *
 * <p>The readout panel reuses {@code create_brass_compass}'s {@code EditScreen} atlas slice (the
 * stock-keeper request window's title, tileable body strip and bottom band) rather than one of
 * Create Fly's own screens: the stock keeper's request window itself — the ticket's working
 * guess — turned out to hold no real vanilla slots at all (its {@code addSlots()} adds only the
 * player's own inventory, off-screen at x=-1000; everything else is a client-side widget list),
 * so it could not have supplied the five real slots this screen needs even by copying its
 * layout. MM-17 narrowed the slice from `EditScreen`'s 224px to the atlas's actually-opaque
 * 208px span ({@link Layout}'s class doc) and moved the five slots onto the same atlas's own
 * {@link Layout#SLOT_BG_U request-slot art}, a warm brown-and-tan 18x18 background that reads as
 * Create's, replacing the flat grey {@link AllGuiTextures#JEI_SLOT} this screen drew before. The
 * player's inventory still sits on Create's own {@link AllGuiTextures#PLAYER_INVENTORY} frame,
 * exactly as every other {@code AbstractSimiContainerScreen} draws it — MM-18 fixed this screen's
 * own copy of that frame's origin to actually agree with where {@code MeteredMotorMenu} put the
 * player slots ({@link Layout}'s class doc, Part 1) and rebuilt the readout as a compact table
 * ({@link Layout}'s class doc, Part 2).
 */
public final class MeteredMotorScreen extends AbstractSimiContainerScreen<MeteredMotorMenu> {
    private static final Identifier ATLAS = AllGuiTextures.STOCK_KEEPER_REQUEST_HEADER.getLocation();
    private static final AllGuiTextures PLAYER_INVENTORY = AllGuiTextures.PLAYER_INVENTORY;
    private static final int COLOUR_TITLE = 0xFF4A2D31;
    /** The header line and every value column: bright, matching MM-17's compass-derived panel text colour. */
    private static final int COLOUR_VALUE = 0xFFCDBCA8;
    /** Every label column: dim, so the bright value it precedes reads as the answer (MM-18, Part 2). Neither
     *  compass screen defines a dim/bright pair for a table like this one, so this is {@link #COLOUR_VALUE} at
     *  roughly 62% brightness — dim enough to read as secondary against the panel's own brown, still legible. */
    private static final int COLOUR_LABEL = 0xFF7F7468;

    public MeteredMotorScreen(MeteredMotorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, MeteredMotorMenu.WIDTH, MeteredMotorMenu.TOP_HEIGHT + MeteredMotorMenu.GAP + PLAYER_INVENTORY.getHeight());
    }

    /** The client factory: reads the motor's position off the buffer, as `MotorMenuProvider` wrote it (UI-UC-001). */
    public static MeteredMotorScreen create(
        Minecraft minecraft, MenuType<MeteredMotorBlockEntity> type, int syncId, Inventory inventory, Component title,
        RegistryFriendlyByteBuf buf
    ) {
        MeteredMotorBlockEntity motor = getBlockEntity(minecraft, buf);
        if (motor == null) {
            return null;
        }
        return new MeteredMotorScreen(new MeteredMotorMenu(syncId, inventory, motor), inventory, title);
    }

    @Override
    protected void init() {
        setWindowOffset(0, 0);
        super.init();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int y = topPos;
        region(graphics, y, Layout.TITLE_V, Layout.TITLE_H);
        Component heading = Component.translatable("screen.metered_motor.title");
        graphics.text(
            font, heading, leftPos + MeteredMotorMenu.WIDTH / 2 - font.width(heading) / 2, y + Layout.TITLE_TEXT_Y, COLOUR_TITLE, false);
        y += Layout.TITLE_H;
        for (int i = 0; i < Layout.FULL_PANELS; i++) {
            region(graphics, y, Layout.PANEL_V, Layout.PANEL_H);
            y += Layout.PANEL_H;
        }
        if (Layout.LAST_PANEL_H > 0) {
            region(graphics, y, Layout.PANEL_V, Layout.LAST_PANEL_H);
            y += Layout.LAST_PANEL_H;
        }
        region(graphics, y, Layout.BOTTOM_V, Layout.BOTTOM_H);

        MeteredMotorBlockEntity motor = menu.contentHolder;
        graphics.text(font, header(motor), leftPos + Layout.LEFT_LABEL_X, topPos + Layout.HEADER_Y, COLOUR_VALUE, false);
        List<Pair> table = tableRows(motor);
        for (int row = 0; row < table.size(); row++) {
            int lineY = topPos + Layout.TABLE_START_Y + row * Layout.LINE_STRIDE;
            Pair pair = table.get(row);
            drawPair(graphics, pair.left(), leftPos + Layout.LEFT_LABEL_X, leftPos + Layout.LEFT_VALUE_RIGHT_X, lineY);
            if (pair.right() != null) {
                drawPair(graphics, pair.right(), leftPos + Layout.RIGHT_LABEL_X, leftPos + Layout.RIGHT_VALUE_RIGHT_X, lineY);
            }
        }

        for (int i = 0; i < MeteredMotorMenu.SLOTS; i++) {
            slotBackground(graphics, leftPos + MeteredMotorMenu.SLOT_X + i * MeteredMotorMenu.SLOT_SIZE, topPos + MeteredMotorMenu.SLOT_Y);
        }

        renderPlayerInventory(graphics, getLeftOfCentered(PLAYER_INVENTORY.getWidth()), topPos + Layout.FRAME_Y);
    }

    private void region(GuiGraphicsExtractor graphics, int y, int v, int h) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, ATLAS, leftPos, y, Layout.ATLAS_U, v, MeteredMotorMenu.WIDTH, h, 256, 256);
    }

    private void slotBackground(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, ATLAS, x, y, Layout.SLOT_BG_U, Layout.SLOT_BG_V, Layout.SLOT_SIZE, Layout.SLOT_SIZE, 256, 256);
    }

    /**
     * One label/value pair (`UI-REQ-003`, `UI-REQ-004`): the label dim and left at {@code labelX},
     * the value bright and right-aligned to {@code valueRightX} — the value truncated first, so a
     * long one never crosses into the other column or past the panel's own edge, rather than
     * wrapped (a table row has no room to grow a second line within the ≤200px window, `Layout`'s
     * class doc).
     */
    private void drawPair(GuiGraphicsExtractor graphics, Pair.Entry entry, int labelX, int valueRightX, int y) {
        graphics.text(font, entry.label(), labelX, y, COLOUR_LABEL, false);
        int labelWidth = font.width(entry.label());
        int available = valueRightX - (labelX + labelWidth + 3);
        String valueText = truncate(entry.value().getString(), Math.max(0, available));
        graphics.text(font, valueText, valueRightX - font.width(valueText), y, COLOUR_VALUE, false);
    }

    /** Truncates to an ellipsis rather than wraps: this table has no spare line to wrap into (`UI-REQ-004`). */
    private String truncate(String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }
        String truncated = font.plainSubstrByWidth(text, Math.max(0, maxWidth - font.width("...")));
        return truncated + "...";
    }

    /** "Tier III · Stopped": the tier and the state together, bright, above the table (`UI-REQ-003`). */
    private static Component header(MeteredMotorBlockEntity motor) {
        Stats stats = motor.stats();
        Component state = Component.translatable("screen.metered_motor.state." + StatsText.state(motor.state()));
        return Component.translatable("screen.metered_motor.header", StatsText.tier(stats.tier()), state);
    }

    /**
     * The table's four rows (`UI-REQ-003`): Speed/Capacity, Efficiency/Rate, Load/Inside, and
     * Remaining alone on the last row — read straight off the client's synced block entity
     * (`UI-DEC-002`), through {@link StatsText}'s formatting so the motor screen's own game test
     * can check the same numbers server-side.
     *
     * <p>MM-17: the remaining-time value no longer reads the bare word "Idle" directly above the
     * header's own "Running"/"Stopped"/"Paused" — a motor can be running while idle (fed,
     * unsignalled, but drawing no load) and the two answer different questions; it reads "no
     * load" instead.
     */
    private static List<Pair> tableRows(MeteredMotorBlockEntity motor) {
        Stats stats = motor.stats();
        List<Pair> rows = new ArrayList<>();
        rows.add(new Pair(
            new Pair.Entry(Component.translatable("screen.metered_motor.label.speed"), Component.translatable("screen.metered_motor.value.rpm", stats.rpm())),
            new Pair.Entry(
                Component.translatable("screen.metered_motor.label.capacity"), Component.translatable("screen.metered_motor.value.capacity", stats.capacity()))));
        rows.add(new Pair(
            new Pair.Entry(
                Component.translatable("screen.metered_motor.label.efficiency"),
                Component.translatable("screen.metered_motor.value.efficiency", StatsText.twoDecimals(stats.efficiency()))),
            new Pair.Entry(
                Component.translatable("screen.metered_motor.label.rate"),
                Component.translatable("screen.metered_motor.value.rate", StatsText.twoDecimals(stats.ratePerMinute())))));
        rows.add(new Pair(
            new Pair.Entry(
                Component.translatable("screen.metered_motor.label.load"), Component.translatable("screen.metered_motor.value.load", StatsText.wholePercent(motor.load()))),
            new Pair.Entry(
                Component.translatable("screen.metered_motor.label.inside"), Component.translatable("screen.metered_motor.value.inside", motor.emeraldsInside()))));
        String remaining = StatsText.remaining(motor.secondsRemaining());
        Component remainingValue = remaining == null
            ? Component.translatable("screen.metered_motor.value.remaining_none")
            : Component.translatable("screen.metered_motor.value.remaining", remaining);
        rows.add(new Pair(new Pair.Entry(Component.translatable("screen.metered_motor.label.remaining"), remainingValue), null));
        return rows;
    }

    /** One table row: a left pair, and an optional right pair (the last row has none). */
    private record Pair(Entry left, Entry right) {
        private record Entry(Component label, Component value) {
        }
    }
}
