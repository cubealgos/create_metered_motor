#!/usr/bin/env python3
"""Composes a headless mock of the motor screen (MM-17) from Create Fly's own `stock_keeper.png`
and `player_inventory.png` atlases, so the layout and the seam fix can be judged from a PNG
without booting a client.

Reads the two atlases straight out of a Create Fly jar (found by globbing the local Gradle
cache, like `tools/icon.py` and `tools/recolour.py` do, or given explicitly with --jar) and
pastes the exact regions `MeteredMotorScreen`/`Layout` blit: the title strip, the tiled body
strips, the bottom band, the five slot backgrounds and the player inventory frame, at the same
atlas coordinates and window geometry those two classes use. The readout lines are drawn with a
plain PIL font at the same x/y the screen uses — not Minecraft's own bitmap font, so the glyphs
will not match pixel for pixel, but the line positions, the panel's fit around them and any
seam between tiled strips will.

The geometry constants below are a Python copy of `src/main/java/metered_motor/menu/Layout.java`
(that class has no Minecraft imports for the same reason: so a plain JUnit test and this script
can both load pure numbers). Keep the two in sync by hand if the layout changes again.

Not part of `just check` (no Minecraft classes needed, no build dependency): a manual visual aid,
run by hand. Requires Pillow.

Usage: python3 tools/screen_mock.py --out /path/to/mm17-mock.png [--jar PATH_TO_CREATE_FLY_JAR]
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
LINE_COUNT = 9
FONT_LINE_HEIGHT = 9
LINE_GAP = 1
LINE_STRIDE = FONT_LINE_HEIGHT + LINE_GAP
LINE_TOP_PADDING = 4
LINE_START_Y = TITLE_H + LINE_TOP_PADDING
SLOT_GAP = 4
SLOT_Y = LINE_START_Y + LINE_COUNT * LINE_STRIDE + SLOT_GAP
SLOT_X = (WIDTH - SLOTS * SLOT_SIZE) // 2
SLOT_BG_U = 32
SLOT_BG_V = 200
BOTTOM_PADDING = 4
PANEL_V = 48
PANEL_H = 20
BODY_HEIGHT = LINE_TOP_PADDING + LINE_COUNT * LINE_STRIDE + SLOT_GAP + SLOT_SIZE + BOTTOM_PADDING
PANELS = BODY_HEIGHT // PANEL_H
BOTTOM_V = 148
BOTTOM_H = 12
TOP_HEIGHT = TITLE_H + PANELS * PANEL_H + BOTTOM_H
GAP = 4
PLAYER_INVENTORY_WIDTH = 176
PLAYER_INVENTORY_HEIGHT = 108
PLAYER_INV_X = (WIDTH - PLAYER_INVENTORY_WIDTH) // 2
WINDOW_HEIGHT = TOP_HEIGHT + GAP + PLAYER_INVENTORY_HEIGHT
TEXT_LEFT = 8
COLOUR_TITLE = (0x4A, 0x2D, 0x31)
COLOUR_TEXT = (0xCD, 0xBC, 0xA8)

SCALE = 4

SAMPLE_TITLE = "The Metered Motor"
SAMPLE_LINES = [
    "Tier I", "41 rpm", "825 SU capacity", "0.97x efficiency", "0.10 emeralds/min at full load",
    "Load: 0%", "1152 emeralds inside", "Remaining: no load", "Running",
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
    for _ in range(PANELS):
        strip = region(ATLAS_U, PANEL_V, WIDTH, PANEL_H)
        canvas.paste(strip, (0, y), strip)
        y += PANEL_H
    bottom = region(ATLAS_U, BOTTOM_V, WIDTH, BOTTOM_H)
    canvas.paste(bottom, (0, y), bottom)

    for i in range(SLOTS):
        slot = region(SLOT_BG_U, SLOT_BG_V, SLOT_SIZE, SLOT_SIZE)
        canvas.paste(slot, (SLOT_X + i * SLOT_SIZE, SLOT_Y), slot)

    player_inventory = player_inventory.crop((0, 0, PLAYER_INVENTORY_WIDTH, PLAYER_INVENTORY_HEIGHT))
    canvas.paste(player_inventory, (PLAYER_INV_X, TOP_HEIGHT + GAP), player_inventory)

    draw = ImageDraw.Draw(canvas)
    font = ImageFont.load_default()
    title_w = draw.textlength(SAMPLE_TITLE, font=font)
    draw.text((WIDTH / 2 - title_w / 2, TITLE_TEXT_Y), SAMPLE_TITLE, fill=COLOUR_TITLE, font=font)
    line_y = LINE_START_Y
    for line in SAMPLE_LINES:
        draw.text((TEXT_LEFT, line_y), line, fill=COLOUR_TEXT, font=font)
        line_y += LINE_STRIDE
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
