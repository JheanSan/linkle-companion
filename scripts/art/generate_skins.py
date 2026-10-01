#!/usr/bin/env python3
"""
Linkle Companion skin generator.

Paints the ORIGINAL 64x64 slim-arm skins (and the mod icon) used by the mod.
Pure Python standard library, no packages needed:

    python scripts/art/generate_skins.py            # writes skins + icon into src/main/resources
    python scripts/art/generate_skins.py --preview  # also writes enlarged previews to build/skin-preview

The art is drawn from a general description of the character (blonde braided
twin tails, green hooded tunic, white shirt, dark corset, short skirt, brown
thigh-high boots, fingerless gloves, compass pendant). It is not traced from or
based on any game file or existing fan skin.

Layout notes (standard Minecraft 64x64 skin, slim 3px arms):
- The twin-tail hair layer of the mod reads its texture from an area vanilla
  skins never use: (24..40, 0..8). See TAIL_* below.
- Every skin is licensed CC BY 4.0 (see ASSETS.md).
"""

import os
import struct
import sys
import zlib

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
SKIN_DIR = os.path.join(ROOT, "src", "main", "resources", "assets", "linkle_companion",
                        "textures", "entity", "linkle")
ICON_PATH = os.path.join(ROOT, "src", "main", "resources", "assets", "linkle_companion", "icon.png")
PREVIEW_DIR = os.path.join(ROOT, "build", "skin-preview")


# --------------------------------------------------------------------------
# Tiny PNG writer (RGBA, 8 bit)
# --------------------------------------------------------------------------

class Image:
    def __init__(self, w, h):
        self.w, self.h = w, h
        self.px = [[None] * w for _ in range(h)]  # None = transparent

    def set(self, x, y, c):
        if c is not None and 0 <= x < self.w and 0 <= y < self.h:
            self.px[y][x] = c

    def get(self, x, y):
        return self.px[y][x]

    def save(self, path):
        os.makedirs(os.path.dirname(path), exist_ok=True)
        raw = bytearray()
        for row in self.px:
            raw.append(0)  # filter: none
            for c in row:
                if c is None:
                    raw += b"\x00\x00\x00\x00"
                else:
                    a = c[3] if len(c) == 4 else 255
                    raw += bytes((c[0], c[1], c[2], a))

        def chunk(tag, data):
            body = tag + data
            return struct.pack(">I", len(data)) + body + struct.pack(">I", zlib.crc32(body) & 0xFFFFFFFF)

        png = b"\x89PNG\r\n\x1a\n"
        png += chunk(b"IHDR", struct.pack(">IIBBBBB", self.w, self.h, 8, 6, 0, 0, 0))
        png += chunk(b"IDAT", zlib.compress(bytes(raw), 9))
        png += chunk(b"IEND", b"")
        with open(path, "wb") as f:
            f.write(png)


# --------------------------------------------------------------------------
# Cube UV helper: Minecraft unwraps a cube of size (w, h, d) at texOffs (u, v)
# --------------------------------------------------------------------------

def cube_faces(u, v, w, h, d):
    """Returns {face: (x, y, width, height)}. 'right' is the model's right (-X) side."""
    return {
        "top": (u + d, v, w, d),
        "bottom": (u + d + w, v, w, d),
        "right": (u, v + d, d, h),
        "front": (u + d, v + d, w, h),
        "left": (u + d + w, v + d, d, h),
        "back": (u + d + w + d, v + d, w, h),
    }


def paint(img, rect, fn):
    """Calls fn(x, y, w, h) -> color|None for every pixel of a face rect (local coords)."""
    x0, y0, w, h = rect
    for y in range(h):
        for x in range(w):
            img.set(x0 + x, y0 + y, fn(x, y, w, h))


# --------------------------------------------------------------------------
# Palettes
# --------------------------------------------------------------------------

SKIN = {"base": (246, 208, 176), "shade": (226, 180, 146), "light": (253, 224, 196),
        "blush": (240, 168, 150), "mouth": (196, 112, 102)}
HAIR = {"light": (255, 232, 140), "base": (240, 200, 88), "shade": (204, 154, 56), "dark": (150, 104, 36)}
EYES = {"white": (250, 250, 250), "iris": (48, 116, 206), "pupil": (28, 52, 110), "brow": (176, 126, 46)}
SHIRT = {"base": (238, 234, 224), "shade": (206, 198, 184)}
CORSET = {"base": (70, 46, 34), "shade": (52, 34, 25), "light": (98, 66, 46)}
GOLD = {"base": (224, 178, 64), "light": (255, 222, 120), "dark": (160, 116, 36)}
BOOT = {"light": (156, 102, 58), "base": (124, 78, 42), "shade": (94, 57, 30), "dark": (64, 38, 20)}
GLOVE = {"base": (112, 72, 42), "shade": (84, 52, 30)}

# Variants only change the tunic and skirt colours, so the character always reads the same.
VARIANTS = {
    "classic": {"tunic": [(104, 186, 82), (66, 150, 62), (44, 112, 46), (28, 80, 34)],
                "skirt": [(236, 146, 64), (204, 112, 44), (162, 84, 32)]},
    "crimson": {"tunic": [(214, 82, 76), (176, 48, 50), (132, 32, 38), (90, 22, 28)],
                "skirt": [(240, 220, 170), (212, 188, 136), (170, 148, 100)]},
    "azure":   {"tunic": [(96, 156, 226), (58, 112, 196), (40, 80, 154), (26, 52, 108)],
                "skirt": [(246, 244, 236), (212, 208, 198), (170, 164, 152)]},
    "violet":  {"tunic": [(164, 116, 214), (124, 78, 180), (90, 54, 140), (60, 34, 98)],
                "skirt": [(240, 196, 92), (208, 160, 60), (166, 122, 40)]},
    "snow":    {"tunic": [(250, 250, 252), (222, 228, 236), (182, 192, 208), (132, 144, 166)],
                "skirt": [(132, 190, 230), (92, 152, 204), (64, 112, 160)]},
}


# --------------------------------------------------------------------------
# The skin
# --------------------------------------------------------------------------

def make_skin(variant):
    pal = VARIANTS[variant]
    T_LIGHT, T_BASE, T_SHADE, T_DARK = pal["tunic"]
    S_LIGHT, S_BASE, S_SHADE = pal["skirt"]
    img = Image(64, 64)

    # ---------------- head (8x8x8 at 0,0) ----------------
    head = cube_faces(0, 0, 8, 8, 8)

    def hair_top(x, y, w, h):
        if x == 3 and y < 6:  # center part line
            return HAIR["shade"]
        return HAIR["light"] if (x + y) % 5 == 0 else HAIR["base"]

    def face_front(x, y, w, h):
        # rows 0-1 bangs, row 2 fringe tips, rows 3-7 face
        if y == 0:
            return HAIR["base"] if x not in (2, 5) else HAIR["light"]
        if y == 1:
            return HAIR["shade"] if x in (0, 7) else (HAIR["base"] if x != 3 else HAIR["shade"])
        if y == 2:
            if x in (0, 7):
                return HAIR["shade"]
            if x in (1, 6):
                return HAIR["base"]
            if x in (3, 4):
                return HAIR["shade"]  # little fringe tip in the middle
            return SKIN["base"]
        if x in (0, 7):  # side hair framing the face
            return HAIR["shade"] if y < 6 else (SKIN["shade"] if y == 7 else HAIR["dark"])
        if y == 3:
            return EYES["brow"] if x in (1, 2, 5, 6) else SKIN["base"]
        if y == 4:
            return {1: EYES["white"], 2: EYES["iris"], 5: EYES["iris"], 6: EYES["white"]}.get(x, SKIN["base"])
        if y == 5:
            return {1: SKIN["blush"], 2: EYES["pupil"], 5: EYES["pupil"], 6: SKIN["blush"]}.get(x, SKIN["base"])
        if y == 6:
            return SKIN["mouth"] if x in (3, 4) else SKIN["base"]
        return SKIN["shade"] if x in (1, 6) else SKIN["base"]

    def head_side(x, y, w, h):
        # x=0 is the back edge for the right face; mirrored below for the left face
        if y < 3:
            return HAIR["base"] if (x + y) % 4 else HAIR["light"]
        if 3 <= y <= 5 and 4 <= x <= 5:  # ear
            return SKIN["shade"] if x == 4 else SKIN["base"]
        if x >= 6 and y >= 3:  # in front of the ear: skin (cheek)
            return SKIN["base"] if y < 7 else SKIN["shade"]
        return HAIR["shade"] if y >= 6 else HAIR["base"]

    def head_back(x, y, w, h):
        if x in (0, 7):
            return HAIR["shade"]
        return HAIR["dark"] if y == 7 else (HAIR["light"] if (x * 3 + y) % 7 == 0 else HAIR["base"])

    def chin(x, y, w, h):
        return SKIN["shade"]

    paint(img, head["top"], hair_top)
    paint(img, head["bottom"], chin)
    paint(img, head["front"], face_front)
    paint(img, head["right"], head_side)
    paint(img, head["left"], lambda x, y, w, h: head_side(w - 1 - x, y, w, h))
    paint(img, head["back"], head_back)

    # ---------------- hat layer: hair volume ----------------
    hat = cube_faces(32, 0, 8, 8, 8)
    paint(img, hat["top"], lambda x, y, w, h: HAIR["light"] if (x * 2 + y) % 6 == 0 else HAIR["base"])
    paint(img, hat["front"], lambda x, y, w, h: (HAIR["base"] if y == 0 else
                                                 (HAIR["shade"] if y == 1 and x not in (2, 3, 4, 5) else None)))
    paint(img, hat["right"], lambda x, y, w, h: HAIR["base"] if y < 2 or (y < 6 and x < 3) else None)
    paint(img, hat["left"], lambda x, y, w, h: HAIR["base"] if y < 2 or (y < 6 and x > 4) else None)
    paint(img, hat["back"], lambda x, y, w, h: HAIR["base"] if y < 6 else (HAIR["shade"] if x not in (0, 7) else None))

    # ---------------- body (8x12x4 at 16,16) ----------------
    body = cube_faces(16, 16, 8, 12, 4)

    def body_front(x, y, w, h):
        if y == 0:  # hood bunched around the neck
            return T_SHADE if x in (0, 7) else (T_BASE if x in (1, 2, 5, 6) else SHIRT["shade"])
        if y == 1:
            if x in (3, 4):
                return GOLD["dark"]  # compass cord
            return T_BASE if x in (0, 1, 6, 7) else SHIRT["base"]
        if 2 <= y <= 5:
            if y in (3, 4) and x in (3, 4):  # compass pendant
                return GOLD["light"] if (x == 3 and y == 3) else GOLD["base"]
            if x in (0, 7):
                return T_SHADE
            if x in (1, 6):
                return T_BASE
            return SHIRT["shade"] if y == 5 else SHIRT["base"]
        if 6 <= y <= 8:  # corset
            if x in (0, 7):
                return T_SHADE
            if y == 7 and x in (3, 4):
                return CORSET["light"]  # lacing
            return CORSET["base"] if y != 8 else CORSET["shade"]
        if y == 9:  # belt
            if x in (3, 4):
                return GOLD["base"]
            return CORSET["shade"]
        # rows 10-11: skirt with tunic panels at the sides
        if x in (0, 7):
            return T_DARK
        return S_SHADE if y == 11 else S_BASE

    def body_back(x, y, w, h):
        if y <= 2:  # hood
            return T_DARK if y == 2 else (T_SHADE if x in (0, 7) else T_BASE)
        if y in (6, 7, 8):
            return CORSET["base"] if 1 <= x <= 6 else T_SHADE
        if y == 9:
            return CORSET["shade"]
        if y >= 10:
            return S_SHADE if y == 11 else S_BASE
        return T_SHADE if x in (0, 7) else T_BASE

    def body_side(x, y, w, h):
        if y <= 5:
            return T_SHADE if y == 0 else T_BASE
        if y <= 8:
            return CORSET["base"]
        if y == 9:
            return CORSET["shade"]
        return T_DARK if y == 11 else T_SHADE

    paint(img, body["top"], lambda x, y, w, h: T_BASE)
    paint(img, body["bottom"], lambda x, y, w, h: S_SHADE)
    paint(img, body["front"], body_front)
    paint(img, body["back"], body_back)
    paint(img, body["right"], body_side)
    paint(img, body["left"], body_side)

    # jacket layer: hood folds on the shoulders and tunic panels for depth
    jacket = cube_faces(16, 32, 8, 12, 4)
    paint(img, jacket["front"], lambda x, y, w, h: (T_LIGHT if y == 0 and x in (1, 6) else
                                                    T_BASE if (y == 0 and x in (0, 7)) else
                                                    T_SHADE if (x in (0, 7) and 10 <= y <= 11) else None))
    paint(img, jacket["back"], lambda x, y, w, h: (T_LIGHT if y == 0 else T_BASE if y == 1 else
                                                   T_SHADE if y == 2 and 1 <= x <= 6 else
                                                   T_DARK if y == 3 and 2 <= x <= 5 else None))
    paint(img, jacket["top"], lambda x, y, w, h: T_LIGHT if x in (0, 1, 6, 7) else None)
    paint(img, jacket["right"], lambda x, y, w, h: T_BASE if y <= 1 else None)
    paint(img, jacket["left"], lambda x, y, w, h: T_BASE if y <= 1 else None)

    # ---------------- arms (slim 3x12x4) ----------------
    def arm_face(x, y, w, h):
        if y <= 2:  # short tunic sleeve
            return T_SHADE if y == 2 else T_BASE
        if y <= 6:
            return SKIN["shade"] if y == 3 else SKIN["base"]
        if y == 7:
            return GLOVE["shade"]  # glove cuff
        if y <= 10:
            return GLOVE["base"]
        return SKIN["base"]  # fingertips poke out of the fingerless gloves

    def arm_top(x, y, w, h):
        return T_BASE

    def arm_bottom(x, y, w, h):
        return SKIN["shade"]

    for (u, v) in ((40, 16), (32, 48)):
        faces = cube_faces(u, v, 3, 12, 4)
        paint(img, faces["top"], arm_top)
        paint(img, faces["bottom"], arm_bottom)
        for f in ("front", "back", "right", "left"):
            paint(img, faces[f], arm_face)

    # sleeves (overlay): puffy sleeve edge and glove cuffs
    for (u, v) in ((40, 32), (48, 48)):
        faces = cube_faces(u, v, 3, 12, 4)
        for f in ("front", "back", "right", "left"):
            paint(img, faces[f], lambda x, y, w, h: T_LIGHT if y == 0 else (T_BASE if y == 1 else
                                                    (GLOVE["shade"] if y == 7 else None)))

    # ---------------- legs (4x12x4) ----------------
    def leg_face(x, y, w, h, outer=False):
        if y == 0:
            return S_BASE  # skirt hem
        if y == 1:
            return S_SHADE if x % 2 else S_BASE
        if y == 2:
            return SKIN["base"]
        if y == 3:
            return BOOT["light"]  # boot cuff
        if y == 11:
            return BOOT["dark"]  # sole
        if y == 7:
            return BOOT["dark"] if not outer else GOLD["dark"]  # strap / buckle on the outside
        if y == 10:
            return BOOT["shade"]
        return BOOT["shade"] if x == 0 or x == w - 1 else BOOT["base"]

    for (u, v, outer_face) in ((0, 16, "right"), (16, 48, "left")):
        faces = cube_faces(u, v, 4, 12, 4)
        paint(img, faces["top"], lambda x, y, w, h: S_BASE)
        paint(img, faces["bottom"], lambda x, y, w, h: BOOT["dark"])
        for f in ("front", "back", "right", "left"):
            outer = (f == outer_face)
            paint(img, faces[f], lambda x, y, w, h, o=outer: leg_face(x, y, w, h, o))

    # pants overlay: raised boot cuff (gives the thigh-high boots a visible rim)
    for (u, v) in ((0, 32), (0, 48)):
        faces = cube_faces(u, v, 4, 12, 4)
        for f in ("front", "back", "right", "left"):
            paint(img, faces[f], lambda x, y, w, h: BOOT["light"] if y == 3 else
                  (S_LIGHT if y == 0 and f != "back" else None))

    # ---------------- twin tails (read by the mod's hair layer) ----------------
    # Upper segment: cube 2x5x2 at texOffs(24, 0). Lower segment: cube 2x5x2 at texOffs(32, 0).
    def tail_upper(x, y, w, h):
        if y == 0:
            return T_BASE  # hair tie in tunic colour
        return HAIR["base"] if (x + y) % 2 == 0 else HAIR["shade"]  # braid pattern

    def tail_lower(x, y, w, h):
        if y == h - 1:
            return HAIR["light"]  # brushy tip
        if y == h - 2:
            return T_BASE  # second tie near the end
        return HAIR["base"] if (x + y) % 2 == 1 else HAIR["shade"]

    for (u, fn) in ((24, tail_upper), (32, tail_lower)):
        faces = cube_faces(u, 0, 2, 5, 2)
        paint(img, faces["top"], lambda x, y, w, h: HAIR["base"])
        paint(img, faces["bottom"], lambda x, y, w, h: HAIR["light"])
        for f in ("front", "back", "right", "left"):
            paint(img, faces[f], fn)

    return img


# --------------------------------------------------------------------------
# Previews (only for checking the art; not shipped)
# --------------------------------------------------------------------------

def blit_face(dst, src, rect, dx, dy, scale, mirror=False):
    x0, y0, w, h = rect
    for y in range(h):
        for x in range(w):
            c = src.get(x0 + (w - 1 - x if mirror else x), y0 + y)
            if c is None:
                continue
            for sy in range(scale):
                for sx in range(scale):
                    dst.set(dx + x * scale + sx, dy + y * scale + sy, c)


def preview(skin, scale=8):
    """Flat front and back view with overlay layers, like a paper doll."""
    W, H = 16 * 2 + 6, 34
    out = Image(W * scale, H * scale)
    bg = (60, 64, 72)
    for y in range(out.h):
        for x in range(out.w):
            out.set(x, y, bg)

    def doll(ox, side):
        # side: "front" or "back"
        parts = [  # (base cube, overlay cube, x, y) in pixels
            ((0, 0, 8, 8, 8), (32, 0, 8, 8, 8), 4, 1),
            ((16, 16, 8, 12, 4), (16, 32, 8, 12, 4), 4, 9),
        ]
        if side == "front":
            arms = [((40, 16, 3, 12, 4), (40, 32, 3, 12, 4), 1), ((32, 48, 3, 12, 4), (48, 48, 3, 12, 4), 12)]
            legs = [((0, 16, 4, 12, 4), (0, 32, 4, 12, 4), 4), ((16, 48, 4, 12, 4), (0, 48, 4, 12, 4), 8)]
        else:
            arms = [((32, 48, 3, 12, 4), (48, 48, 3, 12, 4), 1), ((40, 16, 3, 12, 4), (40, 32, 3, 12, 4), 12)]
            legs = [((16, 48, 4, 12, 4), (0, 48, 4, 12, 4), 4), ((0, 16, 4, 12, 4), (0, 32, 4, 12, 4), 8)]
        for base, over, px, py in parts:
            for cube in (base, over):
                blit_face(out, skin, cube_faces(*cube)[side], (ox + px) * scale, py * scale, scale)
        for base, over, px in arms:
            for cube in (base, over):
                blit_face(out, skin, cube_faces(*cube)[side], (ox + px) * scale, 9 * scale, scale)
        for base, over, px in legs:
            for cube in (base, over):
                blit_face(out, skin, cube_faces(*cube)[side], (ox + px) * scale, 21 * scale, scale)
        # tails hanging beside the head (front view shows their front face)
        for tx in (2, 12) if side == "front" else (12, 2):
            blit_face(out, skin, cube_faces(24, 0, 2, 5, 2)[side], (ox + tx) * scale, 4 * scale, scale)
            blit_face(out, skin, cube_faces(32, 0, 2, 5, 2)[side], (ox + tx) * scale, 9 * scale, scale)

    doll(1, "front")
    doll(1 + 16 + 4, "back")
    return out


def downscale(img, factor):
    out = Image(img.w // factor, img.h // factor)
    for y in range(out.h):
        for x in range(out.w):
            out.set(x, y, img.get(x * factor, y * factor))
    return out


def make_icon(skin, scale=12):
    """128x128 icon: the face (plus hat layer) and twin tails, on a soft round badge."""
    size = 128
    icon = Image(size, size)
    cx = cy = size / 2
    for y in range(size):
        for x in range(size):
            d = ((x + 0.5 - cx) ** 2 + (y + 0.5 - cy) ** 2) ** 0.5
            if d < 62:
                t = d / 62
                icon.set(x, y, (int(52 + 40 * (1 - t)), int(110 + 60 * (1 - t)), int(64 + 30 * (1 - t))))
            elif d < 64:
                icon.set(x, y, (28, 70, 36))
    face_px = 8 * scale  # 96
    ox = (size - face_px) // 2
    oy = 18
    # tails first, so the face sits on top
    for tx in (ox - 2 * scale + 4, ox + face_px - 4):
        blit_face(icon, skin, cube_faces(24, 0, 2, 5, 2)["front"], tx, oy + 3 * scale, scale // 2 * 1)
    blit_face(icon, skin, cube_faces(0, 0, 8, 8, 8)["front"], ox, oy, scale)
    blit_face(icon, skin, cube_faces(32, 0, 8, 8, 8)["front"], ox, oy, scale)
    return icon


def main():
    want_preview = "--preview" in sys.argv
    skins = {}
    for name in VARIANTS:
        skins[name] = make_skin(name)
        skins[name].save(os.path.join(SKIN_DIR, name + ".png"))
        print("wrote", os.path.relpath(os.path.join(SKIN_DIR, name + ".png"), ROOT))
    # "custom" is a copy of classic: it exists so resource packs can drop any skin there
    # without replacing the built-in looks.
    skins["classic"].save(os.path.join(SKIN_DIR, "custom.png"))
    print("wrote", os.path.relpath(os.path.join(SKIN_DIR, "custom.png"), ROOT))
    make_icon(skins["classic"]).save(ICON_PATH)
    print("wrote", os.path.relpath(ICON_PATH, ROOT))

    if want_preview:
        for name, skin in skins.items():
            big = preview(skin, 8)
            big.save(os.path.join(PREVIEW_DIR, name + "_preview.png"))
            # roughly how many pixels she covers at ~10 blocks away
            downscale(preview(skin, 2), 1).save(os.path.join(PREVIEW_DIR, name + "_small.png"))
        print("previews in", os.path.relpath(PREVIEW_DIR, ROOT))


if __name__ == "__main__":
    main()
