#!/usr/bin/env python3
"""Recolours Create's creative motor textures into this mod's three tiers (MOTOR-REQ-013, MOTOR-DEC-004).

Reads `textures/block/creative_motor.png` and `textures/block/creative_casing.png` from the Create
Fly jar path given as the first argument (or from `tools/source/` if the two PNGs were vendored
there instead: pass no argument and vendored files are used when present), and writes six PNGs
under `src/main/resources/assets/metered_motor/textures/block/`: `motor_<tier>.png` and
`casing_<tier>.png` for tier `i`, `ii`, `iii`.

The creative motor's magenta is a narrow hue band (HLS hue roughly 0.78 to 0.92, saturation above
0.2); every pixel in that band is recoloured to the tier's colour at the SAME hue and saturation as
the target, keeping the pixel's own lightness, so shading and outlines survive untouched. Pixels
outside the band (the dark inner parts) are copied as is. Requires Pillow.
"""
from __future__ import annotations

import argparse
import colorsys
import zipfile
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
OUT_DIR = ROOT / "src/main/resources/assets/metered_motor/textures/block"
SOURCE_DIR = ROOT / "tools/source"
JAR_MOTOR_PATH = "assets/create/textures/block/creative_motor.png"
JAR_CASING_PATH = "assets/create/textures/block/creative_casing.png"

HUE_MIN = 0.78
HUE_MAX = 0.92
SATURATION_MIN = 0.2

# (r, g, b) targets, 0-255 (docs/spec/domains/motor.md MOTOR-DEC-004).
TIER_TARGETS = {
    "i": (132, 136, 130),  # andesite grey
    "ii": (196, 148, 72),  # brass
    "iii": (232, 196, 80),  # gold
}


def load_source(name: str, jar: Path | None) -> Image.Image:
    vendored = SOURCE_DIR / name
    if jar is not None:
        with zipfile.ZipFile(jar) as zf:
            entry = JAR_MOTOR_PATH if name == "creative_motor.png" else JAR_CASING_PATH
            with zf.open(entry) as fh:
                return Image.open(fh).convert("RGBA").copy()
    if vendored.exists():
        return Image.open(vendored).convert("RGBA")
    raise FileNotFoundError(
        f"{name}: pass the Create Fly jar as an argument, or vendor it at {vendored}")


def recolour(image: Image.Image, target_rgb: tuple[int, int, int]) -> Image.Image:
    target_h, _target_l, target_s = colorsys.rgb_to_hls(*(c / 255 for c in target_rgb))
    out = image.convert("RGBA")
    width, height = out.size
    pixels = out.load()
    for y in range(height):
        for x in range(width):
            r, g, b, a = pixels[x, y]
            if a == 0:
                continue
            h, l, s = colorsys.rgb_to_hls(r / 255, g / 255, b / 255)
            if HUE_MIN <= h <= HUE_MAX and s > SATURATION_MIN:
                nr, ng, nb = colorsys.hls_to_rgb(target_h, l, target_s)
                pixels[x, y] = (round(nr * 255), round(ng * 255), round(nb * 255), a)
    return out


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("jar", nargs="?", type=Path, help="path to the Create Fly jar")
    args = parser.parse_args()

    motor = load_source("creative_motor.png", args.jar)
    casing = load_source("creative_casing.png", args.jar)
    for name, (w, h) in (("creative_motor.png", motor.size), ("creative_casing.png", casing.size)):
        if (w, h) != (16, 16):
            raise ValueError(f"{name}: expected 16x16, got {w}x{h}")

    OUT_DIR.mkdir(parents=True, exist_ok=True)
    for tier, target in TIER_TARGETS.items():
        motor_out = OUT_DIR / f"motor_{tier}.png"
        casing_out = OUT_DIR / f"casing_{tier}.png"
        recolour(motor, target).save(motor_out)
        recolour(casing, target).save(casing_out)
        print(f"wrote {motor_out}")
        print(f"wrote {casing_out}")


if __name__ == "__main__":
    main()
