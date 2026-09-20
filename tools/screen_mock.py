#!/usr/bin/env python3
"""Composes a headless mock of the motor screen (MM-17, MM-18, MM-20, MM-21) from Create Fly's own
`stock_keeper.png` and `player_inventory.png` atlases, so the layout and the alignment fix can be
judged from a PNG without booting a client.

Reads the two atlases straight out of a Create Fly jar (found by globbing the local Gradle
cache, like `tools/icon.py` and `tools/recolour.py` do, or given explicitly with --jar) and
pastes the exact regions `MeteredMotorScreen`/`Layout` blit: the title strip, the tiled body
strips (a full-height run, then MM-18's final partial strip), the bottom band, the five slot
backgrounds and the player inventory frame, at the same atlas coordinates and window geometry
those two classes use.

MM-18: this mock now also draws the "Inventory" label `AbstractSimiContainerScreen.
renderPlayerInventory` draws (at the frame's own corner + (8, 6)), and outlines every real slot
position `Layout` computes — the five motor slots and all 36 player slots (27 main + 9 hotbar) —
as thin rectangles, so a misalignment between the slots `MeteredMotorMenu.addSlots()` would place
and the frame this screen draws is visible headless, the way MM-18 found it. The table's header
and its eight label/value cells are drawn with a monospace placeholder font (not Minecraft's own
bitmap font, so the glyphs will not match pixel for pixel) at the same x/y `MeteredMotorScreen`
uses, so the panel's fit around them and each column's own text edge will.

MM-20: the four padding insets (`TEXT_LEFT`, `LINE_TOP_PADDING`, `SLOT_GAP`, `BOTTOM_PADDING`)
widened, and the player inventory frame is now pasted cropped to `PLAYER_INVENTORY_HEIGHT` (98 of
its own 108px, the same crop `MeteredMotorScreen.playerInventoryFrame` blits) instead of the whole
texture, so the pointer triangle Kevin's third client check found no longer appears in the mock
either. The "Inventory" label is drawn in Create's own measured label colour (`0xFF404040`,
`javap`'d off `AbstractSimiContainerScreen.renderPlayerInventory`), replacing the earlier guessed
grey.

MM-21 (`decisions/DEC-009-fixed-tiers.md`): the efficiency row dropped (nothing is rolled any
more), so the table shrank from four rows to three, regrouped with no leftover single-column row —
Speed/Capacity, Rate/Load, Inside/Remaining — and `TABLE_ROWS` 4 -> 3 frees one `LINE_STRIDE` of
window height.

The geometry constants below are a Python copy of `src/main/java/metered_motor/menu/Layout.java`
(that class has no Minecraft imports for the same reason: so a plain JUnit test and this script
can both load pure numbers). Keep the two in sync by hand if the layout changes again.

Not part of `just check` (no Minecraft classes needed, no build dependency): a manual visual aid,
run by hand. Requires Pillow.

Usage: python3 tools/screen_mock.py --out /path/to/mm20-mock.png [--jar PATH_TO_CREATE_FLY_JAR]
"""
from __future__ import annotations

import argparse
import glob
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

JAR_GLOB = str(Path.home() / ".gradle/caches/modules-2/files-2.1/maven.modrinth/create-fly/*/*/*.jar")
STOCK_KEEPER_PATH = "assets/create/textures/gui/stock_keeper.png"
PLAYER_INVENTORY_PATH = "assets/create/textures/gui/player_inventory.png"

# ---- Layout.java, copied (see module docstring) ----
SLOTS = 5
SLOT_SIZE = 18
WIDTH = 208
ATLAS_U = 24
TITLE_V = 0
TITLE_H = 18
TITLE_TEXT_Y = 4
FONT_LINE_HEIGHT = 9
LINE_GAP = 0
LINE_STRIDE = FONT_LINE_HEIGHT + LINE_GAP
LINE_TOP_PADDING = 3  # MM-20: 1 -> 3, a visible gap below the title strip
HEADER_Y = TITLE_H + LINE_TOP_PADDING
TABLE_ROWS = 3  # MM-21: 4 -> 3, the efficiency row dropped (decisions/DEC-009-fixed-tiers.md)
TABLE_START_Y = HEADER_Y + LINE_STRIDE
TABLE_END_Y = TABLE_START_Y + TABLE_ROWS * LINE_STRIDE
TEXT_LEFT = 12  # MM-20: 8 -> 12
COLUMN_GAP = 8
COLUMN_WIDTH = (WIDTH - 2 * TEXT_LEFT - COLUMN_GAP) // 2
LEFT_LABEL_X = TEXT_LEFT
LEFT_VALUE_RIGHT_X = TEXT_LEFT + COLUMN_WIDTH
RIGHT_LABEL_X = LEFT_VALUE_RIGHT_X + COLUMN_GAP
RIGHT_VALUE_RIGHT_X = WIDTH - TEXT_LEFT
SLOT_GAP = LINE_STRIDE  # MM-20: 2 -> 9 (a full line's gap), exactly Layout.LINE_STRIDE
SLOT_Y = TABLE_END_Y + SLOT_GAP
SLOT_X = (WIDTH - SLOTS * SLOT_SIZE) // 2
SLOT_BG_U = 32
SLOT_BG_V = 200
BOTTOM_PADDING = 3  # MM-20: 1 -> 3
PANEL_V = 48
PANEL_H = 20
BODY_HEIGHT = LINE_TOP_PADDING + LINE_STRIDE + TABLE_ROWS * LINE_STRIDE + SLOT_GAP + SLOT_SIZE + BOTTOM_PADDING
FULL_PANELS = BODY_HEIGHT // PANEL_H
LAST_PANEL_H = BODY_HEIGHT - FULL_PANELS * PANEL_H
BOTTOM_V = 154
BOTTOM_H = 6
TOP_HEIGHT = TITLE_H + BODY_HEIGHT + BOTTOM_H
GAP = 0
PLAYER_INVENTORY_WIDTH = 176
# MM-20: the source file's own full height (108, AllGuiTextures.PLAYER_INVENTORY) versus the
# height actually pasted below (97) — cropped to stop above the pointer triangle (rows 100-107)
# and its own build-up rows (97-99, each already showing the triangle's "collar" or its border
# broken outright); see Layout's class doc.
PLAYER_INVENTORY_TEXTURE_HEIGHT = 108
PLAYER_INVENTORY_HEIGHT = 97
PLAYER_INV_X = (WIDTH - PLAYER_INVENTORY_WIDTH) // 2
FRAME_Y = TOP_HEIGHT + GAP
MAIN_INV_X = PLAYER_INV_X + 8
MAIN_INV_Y = FRAME_Y + 18
WINDOW_HEIGHT = TOP_HEIGHT + GAP + PLAYER_INVENTORY_HEIGHT
COLOUR_TITLE = (0x4A, 0x2D, 0x31)
COLOUR_VALUE = (0xCD, 0xBC, 0xA8)
# COLOUR_VALUE blended 55% toward the panel's own brown (measured ~0x895B4C): neither compass
# screen's own secondary colour is actually legible against this darker brown — MeteredMotorScreen's
# own COLOUR_LABEL doc has the measurements.
COLOUR_LABEL = (0xAE, 0x90, 0x7F)
# AbstractSimiContainerScreen.renderPlayerInventory's own label colour (javap'd as the constant
# int -12566464 = 0xFF404040); MeteredMotorScreen.playerInventoryFrame reuses it verbatim (MM-20).
COLOUR_PLAYER_INVENTORY_LABEL = (0x40, 0x40, 0x40)

SCALE = 4
OUTLINE_MOTOR_SLOT = (255, 60, 60)
OUTLINE_PLAYER_SLOT = (60, 200, 255)

SAMPLE_HEADER = "Tier III · Stopped"
# MM-21: fixed-tier numbers (decisions/DEC-009-fixed-tiers.md), tier III's own ladder point.
SAMPLE_ROWS = [
    ("Speed", "64 rpm", "Capacity", "294912 SU"),
    ("Rate", "3.20 em/min", "Load", "0%"),
    ("Inside", "0 emeralds", "Remaining", "4m 12s"),
]


def find_jar(explicit: str | None) -> Path:
    if explicit:
        return Path(explicit)
    candidates = sorted(glob.glob(JAR_GLOB))
    if not candidates:
        raise FileNotFoundError(f"no Create Fly jar found at {JAR_GLOB}; pass --jar")
    return Path(candidates[-1])


def load_atlas(jar: Path, entry: str) -> Image.Image:
    with zipfile.ZipFile(jar) as zf:
        with zf.open(entry) as fh:
            return Image.open(fh).convert("RGBA").copy()


def compose(stock_keeper: Image.Image, player_inventory: Image.Image) -> Image.Image:
    canvas = Image.new("RGBA", (WIDTH, WINDOW_HEIGHT), (0, 0, 0, 0))

    def region(u: int, v: int, w: int, h: int) -> Image.Image:
        return stock_keeper.crop((u, v, u + w, v + h))

    y = 0
    canvas.paste(region(ATLAS_U, TITLE_V, WIDTH, TITLE_H), (0, y), region(ATLAS_U, TITLE_V, WIDTH, TITLE_H))
    y += TITLE_H
    for _ in range(FULL_PANELS):
        strip = region(ATLAS_U, PANEL_V, WIDTH, PANEL_H)
        canvas.paste(strip, (0, y), strip)
        y += PANEL_H
    if LAST_PANEL_H > 0:
        strip = region(ATLAS_U, PANEL_V, WIDTH, LAST_PANEL_H)
        canvas.paste(strip, (0, y), strip)
        y += LAST_PANEL_H
    bottom = region(ATLAS_U, BOTTOM_V, WIDTH, BOTTOM_H)
    canvas.paste(bottom, (0, y), bottom)

    for i in range(SLOTS):
        slot = region(SLOT_BG_U, SLOT_BG_V, SLOT_SIZE, SLOT_SIZE)
        canvas.paste(slot, (SLOT_X + i * SLOT_SIZE, SLOT_Y), slot)

    # MM-20: cropped to PLAYER_INVENTORY_HEIGHT (97 of the source's own 108px), the same crop
    # MeteredMotorScreen.playerInventoryFrame blits, so the pointer triangle and its own build-up
    # rows never appear in the mock either.
    player_inv_crop = player_inventory.crop((0, 0, PLAYER_INVENTORY_WIDTH, PLAYER_INVENTORY_HEIGHT))
    canvas.paste(player_inv_crop, (PLAYER_INV_X, FRAME_Y), player_inv_crop)

    draw = ImageDraw.Draw(canvas)
    try:
        font = ImageFont.truetype("/System/Library/Fonts/Menlo.ttc", 7)
    except OSError:
        font = ImageFont.load_default()

    def truncate(text: str, max_width: float) -> str:
        # UI-REQ-004, mirroring `MeteredMotorScreen.truncate`: an ellipsis, not a wrap — this
        # table has no spare line to wrap into.
        if draw.textlength(text, font=font) <= max_width:
            return text
        ellipsis_w = draw.textlength("...", font=font)
        kept = ""
        for ch in text:
            if draw.textlength(kept + ch, font=font) + ellipsis_w > max_width:
                break
            kept += ch
        return kept + "..."

    def draw_pair(label: str, value: str, label_x: int, value_right_x: int, y: int) -> None:
        draw.text((label_x, y), label, fill=COLOUR_LABEL, font=font)
        label_w = draw.textlength(label, font=font)
        available = max(0.0, value_right_x - (label_x + label_w + 3))
        text = truncate(value, available)
        vw = draw.textlength(text, font=font)
        draw.text((value_right_x - vw, y), text, fill=COLOUR_VALUE, font=font)

    header_w = draw.textlength(SAMPLE_HEADER, font=font)
    title_w = draw.textlength("The Metered Motor", font=font)
    draw.text((WIDTH / 2 - title_w / 2, TITLE_TEXT_Y), "The Metered Motor", fill=COLOUR_TITLE, font=font)
    draw.text((LEFT_LABEL_X, HEADER_Y), SAMPLE_HEADER, fill=COLOUR_VALUE, font=font)

    for row, (left_label, left_value, right_label, right_value) in enumerate(SAMPLE_ROWS):
        line_y = TABLE_START_Y + row * LINE_STRIDE
        draw_pair(left_label, left_value, LEFT_LABEL_X, LEFT_VALUE_RIGHT_X, line_y)
        if right_label is not None:
            draw_pair(right_label, right_value, RIGHT_LABEL_X, RIGHT_VALUE_RIGHT_X, line_y)

    draw.text((PLAYER_INV_X + 8, FRAME_Y + 6), "Inventory", fill=COLOUR_PLAYER_INVENTORY_LABEL, font=font)

    # MM-18: outline every real slot position `Layout` computes, so a misalignment between the
    # slots the menu would place and the frame drawn above is visible without booting a client.
    for i in range(SLOTS):
        x = SLOT_X + i * SLOT_SIZE
        draw.rectangle((x, SLOT_Y, x + SLOT_SIZE - 1, SLOT_Y + SLOT_SIZE - 1), outline=OUTLINE_MOTOR_SLOT, width=1)

    def outline_player_slot(slot_x: int, slot_y: int) -> None:
        # The cell (border to border) sits one pixel above/left of the slot's own (icon) origin —
        # `Layout`'s class doc and `player_inventory.png` itself (MM-18's texture sampling).
        draw.rectangle(
            (slot_x - 1, slot_y - 1, slot_x - 1 + SLOT_SIZE - 1, slot_y - 1 + SLOT_SIZE - 1), outline=OUTLINE_PLAYER_SLOT, width=1)

    for row in range(3):
        for col in range(9):
            outline_player_slot(MAIN_INV_X + col * SLOT_SIZE, MAIN_INV_Y + row * SLOT_SIZE)
    for col in range(9):
        outline_player_slot(MAIN_INV_X + col * SLOT_SIZE, MAIN_INV_Y + 58)

    return canvas


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--jar", default=None, help="Create Fly jar path; defaults to the newest one in the Gradle cache")
    parser.add_argument("--out", required=True, help="where to write the composed PNG")
    args = parser.parse_args()

    jar = find_jar(args.jar)
    stock_keeper = load_atlas(jar, STOCK_KEEPER_PATH)
    player_inventory = load_atlas(jar, PLAYER_INVENTORY_PATH)

    mock = compose(stock_keeper, player_inventory)
    mock = mock.resize((mock.width * SCALE, mock.height * SCALE), Image.NEAREST)

    out = Path(args.out)
    out.parent.mkdir(parents=True, exist_ok=True)
    mock.convert("RGB").save(out)
    print(f"screen_mock: wrote {out} ({mock.width}x{mock.height}, {SCALE}x); window {WIDTH}x{WINDOW_HEIGHT} at 1x")


if __name__ == "__main__":
    main()
