#!/usr/bin/env python3
"""Render docs/modrinth/icon.png: the mod's own tier II motor block model on the cubealgos
navy badge Create add-ons share (MM-12).

Loads models/block/metered_motor/item_ii.json (MM-15: one of the three per-tier item models the
item's `minecraft:select` definition now picks among by rolled tier; already textured brass,
tier II's own) and does a real 3D projection of every element -- including the rotated "Axis"
box, the flap-display strip and the corner rims -- using the model's own per-element rotation,
the gui display transform overridden to the rotation Kevin picked from the rendered rotation
sheet ([30, 315, -45], scale 0.625), painter's-order face sorting with backface culling,
per-face UV affine sampling (handling uv flips and the 0/90/180/270 "rotation" key) and
vanilla's flat face shading (up 1.0, down 0.5, north/south 0.8, east/west 0.6).

Textures: `metered_motor:` ones are read from this repo's own
`src/main/resources/assets/metered_motor/textures/` exactly as the model names them --
item_ii.json already points at the tier II (`motor_ii`, `casing_ii`) textures, so no
substitution is needed (MM-7's single tier-I-only item model, which this replaced, needed one).
`create:` ones (the axis and flap-display textures, not yet part of this mod) are read straight
out of the Create Fly jar -- found by globbing the local Gradle cache, or given explicitly with
--jar -- and are never vendored into this repo.

The projection is then composited onto the navy badge: outer white rim, pale band, dark ring,
blueprint-blue disc, half-alpha grid, fit box 320 px, LANCZOS "smooth" mode (the projection is
already anti-aliased art, not raw texels, so it is scaled like a photograph, not zoomed like a
pixelated block texture).

Ports the two heimathafen prototypes this was developed from
(standards/marketing/modrinth/block-model-render.py and .../navy-badge.py) inline, so this repo
never imports from heimathafen at build time. Requires Pillow.

Usage: python3 tools/icon.py [--jar PATH_TO_CREATE_FLY_JAR]
"""
from __future__ import annotations

import argparse
import glob
import json
import math
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter

ROOT = Path(__file__).resolve().parent.parent
MODEL_PATH = ROOT / "src/main/resources/assets/metered_motor/models/block/metered_motor/item_ii.json"
MM_TEX_DIR = ROOT / "src/main/resources/assets/metered_motor/textures"
OUT = ROOT / "docs/modrinth/icon.png"
JAR_GLOB = str(Path.home() / ".gradle/caches/modules-2/files-2.1/maven.modrinth/create-fly/*/*/*.jar")

GUI_OVERRIDE = {"rotation": [30, 315, -45], "scale": [0.625, 0.625, 0.625]}  # MM-12 constraint


# ============================================================== 3D projection
# Ported from heimathafen standards/marketing/modrinth/block-model-render.py.

RENDER_SIZE = 512
RENDER_SUPERSAMPLE = 4
RENDER_CANVAS = RENDER_SIZE * RENDER_SUPERSAMPLE

FACE_VERTS = {
    "down":  [(0, 0, 0), (0, 0, 1), (1, 0, 1), (1, 0, 0)],
    "up":    [(0, 1, 1), (0, 1, 0), (1, 1, 0), (1, 1, 1)],
    "north": [(1, 1, 0), (1, 0, 0), (0, 0, 0), (0, 1, 0)],
    "south": [(0, 1, 1), (0, 0, 1), (1, 0, 1), (1, 1, 1)],
    "west":  [(0, 1, 0), (0, 0, 0), (0, 0, 1), (0, 1, 1)],
    "east":  [(1, 1, 1), (1, 0, 1), (1, 0, 0), (1, 1, 0)],
}
FACE_NORMAL = {
    "down": (0, -1, 0), "up": (0, 1, 0),
    "north": (0, 0, -1), "south": (0, 0, 1),
    "west": (-1, 0, 0), "east": (1, 0, 0),
}
SHADE = {"up": 1.0, "down": 0.5, "north": 0.8, "south": 0.8, "east": 0.6, "west": 0.6}


def rot_axis(p, axis, deg):
    ang = math.radians(deg)
    c, s = math.cos(ang), math.sin(ang)
    x, y, z = p
    if axis == "x":
        return (x, y * c - z * s, y * s + z * c)
    if axis == "y":
        return (x * c + z * s, y, -x * s + z * c)
    return (x * c - y * s, x * s + y * c, z)


def rotate_about(p, origin, axis, deg):
    rel = (p[0] - origin[0], p[1] - origin[1], p[2] - origin[2])
    r = rot_axis(rel, axis, deg)
    return (r[0] + origin[0], r[1] + origin[1], r[2] + origin[2])


def display_transform(p, pivot, rx, ry, rz):
    rel = (p[0] - pivot[0], p[1] - pivot[1], p[2] - pivot[2])
    rel = rot_axis(rel, "x", rx)
    rel = rot_axis(rel, "y", ry)
    rel = rot_axis(rel, "z", rz)
    return rel


def affine_from_points(src, dst):
    """3-point affine solve: dst = A*src + t. Returns forward (a,b,c,d,e,f)."""
    (x0, y0), (x1, y1), (x2, y2) = src
    (u0, v0), (u1, v1), (u2, v2) = dst
    mat = [[x0, y0, 1], [x1, y1, 1], [x2, y2, 1]]
    det = (mat[0][0] * (mat[1][1] * mat[2][2] - mat[1][2] * mat[2][1])
           - mat[0][1] * (mat[1][0] * mat[2][2] - mat[1][2] * mat[2][0])
           + mat[0][2] * (mat[1][0] * mat[2][1] - mat[1][1] * mat[2][0]))
    if abs(det) < 1e-9:
        return None

    def solve(vals):
        res = []
        for col in range(3):
            m2 = [row[:] for row in mat]
            for r in range(3):
                m2[r][col] = vals[r]
            d = (m2[0][0] * (m2[1][1] * m2[2][2] - m2[1][2] * m2[2][1])
                 - m2[0][1] * (m2[1][0] * m2[2][2] - m2[1][2] * m2[2][0])
                 + m2[0][2] * (m2[1][0] * m2[2][1] - m2[1][1] * m2[2][0]))
            res.append(d / det)
        return res

    a, b, c = solve([u0, u1, u2])
    d, e, f = solve([v0, v1, v2])
    return (a, b, c, d, e, f)


def invert_affine(coef):
    a, b, c, d, e, f = coef
    det = a * e - b * d
    if abs(det) < 1e-9:
        return None
    ia = e / det
    ib = -b / det
    ic = -(ia * c + ib * f)
    id_ = -d / det
    ie = a / det
    if_ = -(id_ * c + ie * f)
    return (ia, ib, ic, id_, ie, if_)


def build_faces(model, textures, pivot, gui):
    """Returns list of (depth, canvas_quad[4], texture_img, uv_patch_quad[4], shade)."""
    faces = []
    rx, ry, rz = gui["rotation"]
    for elem in model["elements"]:
        frm, to = elem["from"], elem["to"]
        erot = elem.get("rotation")
        corners = {}
        for bx in (0, 1):
            for by in (0, 1):
                for bz in (0, 1):
                    p = (
                        frm[0] if bx == 0 else to[0],
                        frm[1] if by == 0 else to[1],
                        frm[2] if bz == 0 else to[2],
                    )
                    if erot:
                        p = rotate_about(p, erot["origin"], erot["axis"], erot["angle"])
                    corners[(bx, by, bz)] = p
        for face_name, face in elem.get("faces", {}).items():
            verts_frac = FACE_VERTS[face_name]
            verts3d = [corners[v] for v in verts_frac]

            normal = FACE_NORMAL[face_name]
            if erot:
                normal = rot_axis(normal, erot["axis"], erot["angle"])
            cam_normal = rot_axis(rot_axis(rot_axis(normal, "x", rx), "y", ry), "z", rz)
            if cam_normal[2] <= 1e-4:
                continue  # backface culled

            cam_pts = [display_transform(p, pivot, rx, ry, rz) for p in verts3d]
            depth = sum(p[2] for p in cam_pts) / 4.0
            canvas_quad = [(p[0], -p[1]) for p in cam_pts]

            tex_key = face["texture"].lstrip("#")
            tex_img = textures[tex_key]
            u1, v1, u2, v2 = face["uv"]
            flip_x = u1 > u2
            flip_y = v1 > v2
            lo = (min(u1, u2), min(v1, v2))
            hi = (max(u1, u2), max(v1, v2))
            patch = tex_img.crop((round(lo[0]), round(lo[1]), round(hi[0]), round(hi[1])))
            if patch.width == 0 or patch.height == 0:
                continue
            if flip_x:
                patch = patch.transpose(Image.FLIP_LEFT_RIGHT)
            if flip_y:
                patch = patch.transpose(Image.FLIP_TOP_BOTTOM)
            pw, ph = patch.size
            default_patch_quad = [(0, 0), (0, ph), (pw, ph), (pw, 0)]
            rotation = face.get("rotation", 0)
            shift = (rotation // 90) % 4
            patch_quad = [default_patch_quad[(i + shift) % 4] for i in range(4)]

            faces.append((depth, canvas_quad, patch, patch_quad, SHADE[face_name]))
    faces.sort(key=lambda f: f[0])  # far to near
    return faces


def fit_scale(faces, canvas, margin=0.88):
    xs, ys = [], []
    for _, quad, *_ in faces:
        for x, y in quad:
            xs.append(x)
            ys.append(y)
    w = max(xs) - min(xs)
    h = max(ys) - min(ys)
    cx = (max(xs) + min(xs)) / 2
    cy = (max(ys) + min(ys)) / 2
    scale = (canvas * margin) / max(w, h)
    return scale, cx, cy


def render_model(model, textures, gui):
    pivot = (8.0, 8.0, 8.0)
    faces = build_faces(model, textures, pivot, gui)
    scale, cx, cy = fit_scale(faces, RENDER_CANVAS)

    canvas = Image.new("RGBA", (RENDER_CANVAS, RENDER_CANVAS), (0, 0, 0, 0))
    for depth, quad, patch, patch_quad, shade in faces:
        dst = [((x - cx) * scale + RENDER_CANVAS / 2, (y - cy) * scale + RENDER_CANVAS / 2)
               for x, y in quad]
        patch = patch.convert("RGBA")
        if shade != 1.0:
            r, g, b, a = patch.split()
            r = r.point(lambda v: int(v * shade))
            g = g.point(lambda v: int(v * shade))
            b = b.point(lambda v: int(v * shade))
            patch = Image.merge("RGBA", (r, g, b, a))

        fwd = affine_from_points(patch_quad[:3], dst[:3])
        if fwd is None:
            continue
        inv = invert_affine(fwd)
        if inv is None:
            continue
        layer = patch.transform((RENDER_CANVAS, RENDER_CANVAS), Image.AFFINE, inv,
                                 resample=Image.NEAREST, fillcolor=(0, 0, 0, 0))
        canvas.alpha_composite(layer)

    return canvas.resize((RENDER_SIZE, RENDER_SIZE), Image.LANCZOS)


# ============================================================== texture resolution

def find_jar(explicit: Path | None) -> Path:
    if explicit is not None:
        if not explicit.exists():
            raise SystemExit(f"icon: --jar {explicit} does not exist")
        return explicit
    matches = sorted(Path(p) for p in glob.glob(JAR_GLOB))
    if not matches:
        raise SystemExit(
            f"icon: no Create Fly jar found under {JAR_GLOB}; pass --jar explicitly")
    return matches[-1]


def load_texture(location: str, jar: Path | None) -> Image.Image:
    namespace, path = location.split(":", 1)
    if namespace == "metered_motor":
        file = MM_TEX_DIR / f"{path}.png"
        if not file.exists():
            raise SystemExit(f"icon: texture {location!r} not found at {file}")
        return Image.open(file).convert("RGBA")
    if namespace == "create":
        assert jar is not None
        entry = f"assets/create/textures/{path}.png"
        with zipfile.ZipFile(jar) as zf:
            with zf.open(entry) as fh:
                return Image.open(fh).convert("RGBA").copy()
    raise SystemExit(f"icon: unknown texture namespace {namespace!r} in {location!r}")


# ============================================================== navy badge
# Ported from heimathafen standards/marketing/modrinth/navy-badge.py.

BADGE_SIZE = 512
BADGE_CENTRE = BADGE_SIZE // 2
BADGE_SUPERSAMPLE = 4
RIM = (255, 255, 255, 255)
BAND = (232, 236, 244, 255)
RING = (9, 12, 27, 255)
BLUEPRINT = (13, 18, 38, 255)
GRID = (52, 76, 128, 255)
OUTLINE = (255, 255, 255, 235)
SHADOW = (20, 50, 90, 130)
FIT_BOX = 320  # MM-12: smooth mode, already-rendered subject


def badge() -> Image.Image:
    big = BADGE_SIZE * BADGE_SUPERSAMPLE
    img = Image.new("RGBA", (big, big), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    for radius, colour in ((256, RIM), (250, BAND), (238, RING), (200, BLUEPRINT)):
        r = radius * BADGE_SUPERSAMPLE
        c = BADGE_CENTRE * BADGE_SUPERSAMPLE
        draw.ellipse((c - r, c - r, c + r, c + r), fill=colour)
    img = img.resize((BADGE_SIZE, BADGE_SIZE), Image.LANCZOS)

    grid = Image.new("RGBA", (BADGE_SIZE, BADGE_SIZE), (0, 0, 0, 0))
    g = ImageDraw.Draw(grid)
    for k in range(-4, 5):
        p = BADGE_CENTRE + k * 48
        g.line((p, 0, p, BADGE_SIZE), fill=GRID, width=3)
        g.line((0, p, BADGE_SIZE, p), fill=GRID, width=3)
    glow = Image.new("RGBA", (BADGE_SIZE, BADGE_SIZE), (0, 0, 0, 0))
    ImageDraw.Draw(glow).ellipse(
        (BADGE_CENTRE - 120, BADGE_CENTRE - 120, BADGE_CENTRE + 120, BADGE_CENTRE + 120),
        fill=(34, 48, 92, 150))
    glow = glow.filter(ImageFilter.GaussianBlur(50))
    inner = Image.alpha_composite(glow, grid)
    mask = Image.new("L", (BADGE_SIZE, BADGE_SIZE), 0)
    ImageDraw.Draw(mask).ellipse(
        (BADGE_CENTRE - 238, BADGE_CENTRE - 238, BADGE_CENTRE + 238, BADGE_CENTRE + 238),
        fill=255)
    clipped = Image.new("RGBA", (BADGE_SIZE, BADGE_SIZE), (0, 0, 0, 0))
    clipped.paste(inner, (0, 0), mask)
    return Image.alpha_composite(img, clipped)


def compose(base: Image.Image, sprite: Image.Image, box: int = FIT_BOX) -> Image.Image:
    """"smooth" mode: sprite is already-rendered/anti-aliased art (the motor's projection), so
    it is LANCZOS-scaled to fit, not zoomed like a raw pixel-art texture."""
    w, h = sprite.size
    longest = max(w, h)
    factor = box / longest
    size = (round(w * factor), round(h * factor))
    scale = size[0] / w
    sprite = sprite.resize(size, Image.LANCZOS)
    alpha = sprite.getchannel("A")
    x = BADGE_CENTRE - size[0] // 2
    y = BADGE_CENTRE - size[1] // 2
    step = max(1, round(scale))
    grown = Image.new("L", (BADGE_SIZE, BADGE_SIZE), 0)
    for dx in (-step, 0, step):
        for dy in (-step, 0, step):
            grown.paste(alpha, (x + dx, y + dy), alpha)
    shadow = Image.new("RGBA", (BADGE_SIZE, BADGE_SIZE), (0, 0, 0, 0))
    shadow.paste(SHADOW, (0, 0), grown.transform(grown.size, Image.AFFINE, (1, 0, -14, 0, 1, -14)))
    shadow = shadow.filter(ImageFilter.GaussianBlur(10))
    outline = Image.new("RGBA", (BADGE_SIZE, BADGE_SIZE), (0, 0, 0, 0))
    outline.paste(OUTLINE, (0, 0), grown)
    img = Image.alpha_composite(base, shadow)
    img = Image.alpha_composite(img, outline)
    img.alpha_composite(sprite, (x, y))
    return img


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--jar", type=Path, default=None,
                         help="Create Fly jar to read the create: textures from")
    args = parser.parse_args()

    model = json.loads(MODEL_PATH.read_text())
    tex_refs = {k: v for k, v in model["textures"].items() if k != "particle"}
    jar = find_jar(args.jar) if any(v.startswith("create:") for v in tex_refs.values()) else None
    textures = {key: load_texture(value, jar) for key, value in tex_refs.items()}

    sprite = render_model(model, textures, GUI_OVERRIDE)
    icon = compose(badge(), sprite)

    OUT.parent.mkdir(parents=True, exist_ok=True)
    icon.save(OUT, optimize=True)
    print(f"wrote {OUT} ({OUT.stat().st_size} bytes)")
    if jar is not None:
        print(f"create: textures read from {jar}")


if __name__ == "__main__":
    main()
