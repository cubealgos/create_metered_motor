package metered_motor.client.screen;

import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.menu.AbstractSimiContainerScreen;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import java.util.ArrayList;
import java.util.List;
import metered_motor.block.MeteredMotorBlockEntity;
import metered_motor.client.StatsText;
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
 * <p>The readout panel reuses the exact atlas slice {@code create_brass_compass}'s
 * {@code EditScreen} already blits in production (the stock-keeper request window's title,
 * tileable body strip and bottom band, at columns 16..240 of its 256-wide atlas) rather than one
 * of Create Fly's own screens: the stock keeper's request window itself — the ticket's working
 * guess — turned out to hold no real vanilla slots at all (its {@code addSlots()} adds only the
 * player's own inventory, off-screen at x=-1000; everything else is a client-side widget list),
 * so it could not have supplied the five real slots this screen needs even by copying its
 * layout. The five slots themselves sit on the generic {@link AllGuiTextures#JEI_SLOT} graphic
 * Create Fly already draws item slots with outside JEI's own screens, and the player's inventory
 * sits on Create's own {@link AllGuiTextures#PLAYER_INVENTORY} frame, exactly as every other
 * {@code AbstractSimiContainerScreen} draws it.
 */
public final class MeteredMotorScreen extends AbstractSimiContainerScreen<MeteredMotorMenu> {
    private static final Identifier ATLAS = AllGuiTextures.STOCK_KEEPER_REQUEST_HEADER.getLocation();
    private static final AllGuiTextures SLOT_BG = AllGuiTextures.JEI_SLOT;
    private static final AllGuiTextures PLAYER_INVENTORY = AllGuiTextures.PLAYER_INVENTORY;
    private static final int ATLAS_U = 16;
    private static final int TITLE_V = 0;
    private static final int TITLE_H = 18;
    private static final int PANEL_V = 48;
    private static final int PANEL_H = 20;
    private static final int PANELS = 7;
    private static final int BOTTOM_V = 148;
    private static final int BOTTOM_H = 12;
    private static final int LINE_START_Y = 22;
    private static final int LINE_GAP = 1;
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
        region(graphics, y, TITLE_V, TITLE_H);
        Component heading = Component.translatable("screen.metered_motor.title");
        graphics.text(font, heading, leftPos + MeteredMotorMenu.WIDTH / 2 - font.width(heading) / 2, y + 5, COLOUR_TITLE, false);
        y += TITLE_H;
        for (int i = 0; i < PANELS; i++) {
            region(graphics, y, PANEL_V, PANEL_H);
            y += PANEL_H;
        }
        region(graphics, y, BOTTOM_V, BOTTOM_H);

        MeteredMotorBlockEntity motor = menu.contentHolder;
        int lineY = topPos + LINE_START_Y;
        for (Component line : readoutLines(motor)) {
            // UI-REQ-004, the compass's BC-10: wrapped to the panel from the start, never blitted raw.
            for (FormattedCharSequence wrapped : font.split(line, MeteredMotorMenu.WIDTH - 2 * TEXT_LEFT)) {
                graphics.text(font, wrapped, leftPos + TEXT_LEFT, lineY, COLOUR_TEXT, false);
                lineY += font.lineHeight + LINE_GAP;
            }
        }

        for (int i = 0; i < MeteredMotorMenu.SLOTS; i++) {
            SLOT_BG.render(graphics, leftPos + MeteredMotorMenu.SLOT_X + i * MeteredMotorMenu.SLOT_SIZE - 1, topPos + MeteredMotorMenu.SLOT_Y - 1);
        }

        renderPlayerInventory(
            graphics, getLeftOfCentered(PLAYER_INVENTORY.getWidth()), topPos + MeteredMotorMenu.TOP_HEIGHT + MeteredMotorMenu.GAP);
    }

    private void region(GuiGraphicsExtractor graphics, int y, int v, int h) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, ATLAS, leftPos, y, ATLAS_U, v, MeteredMotorMenu.WIDTH, h, 256, 256);
    }

    /**
     * Every readout line (`UI-REQ-003`): tier, rpm, stress capacity, efficiency, the rate at full
     * load, the current load, emeralds inside (blocks counted as nine, `MOTOR-REQ-006`), the
     * remaining time at the current load, and the state — read straight off the client's synced
     * block entity (`UI-DEC-002`), through {@link StatsText}'s formatting so the motor screen's
     * own game test can check the same numbers server-side.
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
                ? Component.translatable("screen.metered_motor.remaining_idle")
                : Component.translatable("screen.metered_motor.remaining", remaining));
        lines.add(Component.translatable("screen.metered_motor.state." + StatsText.state(motor.state())));
        return lines;
    }
}
