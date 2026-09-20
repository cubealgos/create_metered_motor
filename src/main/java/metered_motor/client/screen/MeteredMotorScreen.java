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
import net.minecraft.util.FormattedCharSequence;
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
 * exactly as every other {@code AbstractSimiContainerScreen} draws it.
 */
public final class MeteredMotorScreen extends AbstractSimiContainerScreen<MeteredMotorMenu> {
    private static final Identifier ATLAS = AllGuiTextures.STOCK_KEEPER_REQUEST_HEADER.getLocation();
    private static final AllGuiTextures PLAYER_INVENTORY = AllGuiTextures.PLAYER_INVENTORY;
    private static final int TEXT_LEFT = 8;
    private static final int COLOUR_TITLE = 0xFF4A2D31;
    private static final int COLOUR_TEXT = 0xFFCDBCA8;

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
        for (int i = 0; i < Layout.PANELS; i++) {
            region(graphics, y, Layout.PANEL_V, Layout.PANEL_H);
            y += Layout.PANEL_H;
        }
        region(graphics, y, Layout.BOTTOM_V, Layout.BOTTOM_H);

        MeteredMotorBlockEntity motor = menu.contentHolder;
        int lineY = topPos + Layout.LINE_START_Y;
        for (Component line : readoutLines(motor)) {
            // UI-REQ-004, the compass's BC-10: wrapped to the panel from the start, never blitted raw.
            for (FormattedCharSequence wrapped : font.split(line, MeteredMotorMenu.WIDTH - 2 * TEXT_LEFT)) {
                graphics.text(font, wrapped, leftPos + TEXT_LEFT, lineY, COLOUR_TEXT, false);
                lineY += font.lineHeight + Layout.LINE_GAP;
            }
        }

        for (int i = 0; i < MeteredMotorMenu.SLOTS; i++) {
            slotBackground(graphics, leftPos + MeteredMotorMenu.SLOT_X + i * MeteredMotorMenu.SLOT_SIZE, topPos + MeteredMotorMenu.SLOT_Y);
        }

        renderPlayerInventory(
            graphics, getLeftOfCentered(PLAYER_INVENTORY.getWidth()), topPos + MeteredMotorMenu.TOP_HEIGHT + MeteredMotorMenu.GAP);
    }

    private void region(GuiGraphicsExtractor graphics, int y, int v, int h) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, ATLAS, leftPos, y, Layout.ATLAS_U, v, MeteredMotorMenu.WIDTH, h, 256, 256);
    }

    private void slotBackground(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, ATLAS, x, y, Layout.SLOT_BG_U, Layout.SLOT_BG_V, Layout.SLOT_SIZE, Layout.SLOT_SIZE, 256, 256);
    }

    /**
     * Every readout line (`UI-REQ-003`): tier, rpm, stress capacity, efficiency, the rate at full
     * load, the current load, emeralds inside (blocks counted as nine, `MOTOR-REQ-006`), the
     * remaining time at the current load, and the state — read straight off the client's synced
     * block entity (`UI-DEC-002`), through {@link StatsText}'s formatting so the motor screen's
     * own game test can check the same numbers server-side.
     *
     * <p>MM-17: the remaining-time line no longer reads the bare word "Idle" directly above the
     * state line's own "Running"/"Stopped"/"Paused" — a motor can be running while idle (fed,
     * unsignalled, but drawing no load) and the two lines answer different questions. Idle now
     * reads "Remaining: no load" instead.
     */
    private static List<Component> readoutLines(MeteredMotorBlockEntity motor) {
        Stats stats = motor.stats();
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("screen.metered_motor.tier", StatsText.tier(stats.tier())));
        lines.add(Component.translatable("screen.metered_motor.rpm", stats.rpm()));
        lines.add(Component.translatable("screen.metered_motor.capacity", stats.capacity()));
        lines.add(Component.translatable("screen.metered_motor.efficiency", StatsText.twoDecimals(stats.efficiency())));
        lines.add(Component.translatable("screen.metered_motor.rate", StatsText.twoDecimals(stats.ratePerMinute())));
        lines.add(Component.translatable("screen.metered_motor.load", StatsText.wholePercent(motor.load())));
        lines.add(Component.translatable("screen.metered_motor.emeralds_inside", motor.emeraldsInside()));
        String remaining = StatsText.remaining(motor.secondsRemaining());
        lines.add(
            remaining == null
                ? Component.translatable("screen.metered_motor.remaining_none")
                : Component.translatable("screen.metered_motor.remaining", remaining));
        lines.add(Component.translatable("screen.metered_motor.state." + StatsText.state(motor.state())));
        return lines;
    }
}
