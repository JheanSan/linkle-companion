#!/usr/bin/env python3
"""
Generates the original item texture and the inventory GUI background.

    python scripts/art/generate_items.py

Pure Python standard library (shares the tiny PNG writer in generate_skins.py).
Outputs (CC BY 4.0, see ASSETS.md):
- textures/item/wanderers_compass.png   16x16 item icon
- textures/gui/linkle_inventory.png     256x256 sheet, panel in the top-left 176x186
The GUI slot positions must match dev.linklecompanion.menu.LinkleMenu.
"""

import os
import sys

sys.path.insert(0, os.path.dirname(__file__))
from generate_skins import Image, ROOT  # noqa: E402

ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "linkle_companion", "textures")


# --------------------------------------------------------------------------
# Item: Wanderer's Compass (gold compass on a green cord)
# --------------------------------------------------------------------------

def make_compass():
    img = Image(16, 16)
    rim_light = (255, 222, 120)
    rim = (224, 178, 64)
    rim_dark = (150, 104, 36)
    face = (244, 232, 202)
    face_shade = (214, 200, 168)
    needle_red = (214, 52, 48)
    needle_steel = (176, 184, 200)
    cord = (66, 150, 62)
    cord_dark = (40, 104, 44)
    cord_light = (104, 186, 82)

    cx, cy, r = 7.5, 9.0, 5.6
    for y in range(16):
        for x in range(16):
            d = ((x + 0.5 - cx - 0.5) ** 2 + (y + 0.5 - cy - 0.5) ** 2) ** 0.5
            if d <= r - 1.6:
                img.set(x, y, face if (x + y) % 7 else face_shade)
            elif d <= r - 0.6:
                # rim: lighter at top-left, darker at bottom-right
                img.set(x, y, rim_light if (x < cx and y < cy) else (rim_dark if (x > cx + 1 and y > cy + 1) else rim))
            elif d <= r + 0.2:
                img.set(x, y, rim_dark)

    # needle: red half points up-right, steel half down-left, gold pin in the middle
    for x, y, c in [(10, 6, needle_red), (9, 7, needle_red), (8, 8, needle_red),
                    (7, 10, needle_steel), (6, 11, needle_steel), (5, 12, needle_steel)]:
        img.set(x, y, c)
    img.set(8, 9, rim_dark)
    img.set(7, 9, rim)
    # tick marks
    for x, y in [(8, 4), (8, 14), (3, 9), (13, 9)]:
        img.set(x, y, rim_dark)

    # green cord loop on top
    for x, y, c in [(6, 0, cord_light), (7, 0, cord_light), (8, 0, cord), (9, 0, cord),
                    (5, 1, cord), (10, 1, cord_dark), (5, 2, cord_dark), (10, 2, cord_dark),
                    (6, 3, cord), (9, 3, cord), (7, 3, rim), (8, 3, rim)]:
        img.set(x, y, c)
    # little cord tassel hanging off the side
    for x, y, c in [(13, 11, cord), (14, 12, cord_light), (14, 13, cord), (13, 14, cord_dark)]:
        img.set(x, y, c)
    return img


# --------------------------------------------------------------------------
# GUI: vanilla-style panel with armor column, preview window, 3x3 pockets
# --------------------------------------------------------------------------

BG = (198, 198, 198)
WHITE = (255, 255, 255)
DARK = (85, 85, 85)
BLACK = (0, 0, 0)
SLOT_DARK = (55, 55, 55)
SLOT_FILL = (139, 139, 139)

PANEL_W, PANEL_H = 176, 186


def rect(img, x0, y0, x1, y1, c):
    for y in range(y0, y1):
        for x in range(x0, x1):
            img.set(x, y, c)


def panel(img):
    rect(img, 0, 0, PANEL_W, PANEL_H, BG)
    w, h = PANEL_W, PANEL_H
    # black outline with cut corners
    rect(img, 3, 0, w - 3, 1, BLACK)
    rect(img, 3, h - 1, w - 3, h, BLACK)
    rect(img, 0, 3, 1, h - 3, BLACK)
    rect(img, w - 1, 3, w, h - 3, BLACK)
    for x, y in [(1, 1), (2, 1), (1, 2), (w - 2, 1), (w - 3, 1), (w - 2, 2),
                 (1, h - 2), (1, h - 3), (2, h - 2), (w - 2, h - 2), (w - 3, h - 2), (w - 2, h - 3)]:
        img.set(x, y, BLACK)
    for x, y in [(0, 0), (1, 0), (2, 0), (0, 1), (0, 2), (w - 1, 0), (w - 2, 0), (w - 3, 0), (w - 1, 1), (w - 1, 2),
                 (0, h - 1), (1, h - 1), (2, h - 1), (0, h - 2), (0, h - 3),
                 (w - 1, h - 1), (w - 2, h - 1), (w - 3, h - 1), (w - 1, h - 2), (w - 1, h - 3)]:
        img.set(x, y, None)
    # bevel: white top/left, dark bottom/right
    rect(img, 3, 1, w - 3, 3, WHITE)
    rect(img, 1, 3, 3, h - 3, WHITE)
    rect(img, 3, h - 3, w - 3, h - 1, DARK)
    rect(img, w - 3, 3, w - 1, h - 3, DARK)
    img.set(3, 3, WHITE)
    img.set(w - 4, h - 4, DARK)


def slot(img, item_x, item_y):
    """18x18 vanilla slot frame around the 16x16 item position."""
    x, y = item_x - 1, item_y - 1
    rect(img, x, y, x + 18, y + 18, SLOT_FILL)
    rect(img, x, y, x + 17, y + 1, SLOT_DARK)
    rect(img, x, y, x + 1, y + 17, SLOT_DARK)
    rect(img, x + 1, y + 17, x + 18, y + 18, WHITE)
    rect(img, x + 17, y + 1, x + 18, y + 18, WHITE)


def window(img, x0, y0, x1, y1):
    """Recessed black window for the entity preview."""
    rect(img, x0, y0, x1, y1, BLACK)
    rect(img, x0 - 1, y0 - 1, x1, y0, SLOT_DARK)
    rect(img, x0 - 1, y0 - 1, x0, y1, SLOT_DARK)
    rect(img, x0, y1, x1 + 1, y1 + 1, WHITE)
    rect(img, x1, y0, x1 + 1, y1 + 1, WHITE)


def make_gui():
    img = Image(256, 256)
    panel(img)
    # armor column (matches LinkleMenu.ARMOR_X/Y)
    for i in range(4):
        slot(img, 8, 18 + i * 18)
    # entity preview window
    window(img, 28, 18, 92, 89)
    # 3x3 pockets (matches LinkleMenu.POCKET_X/Y)
    for row in range(3):
        for col in range(3):
            slot(img, 98 + col * 18, 18 + row * 18)
    # player inventory (matches LinkleMenu.PLAYER_INV_Y) and hotbar
    for row in range(3):
        for col in range(9):
            slot(img, 8 + col * 18, 104 + row * 18)
    for col in range(9):
        slot(img, 8 + col * 18, 162)
    return img


def main():
    item_path = os.path.join(ASSETS, "item", "wanderers_compass.png")
    make_compass().save(item_path)
    print("wrote", os.path.relpath(item_path, ROOT))
    gui_path = os.path.join(ASSETS, "gui", "linkle_inventory.png")
    make_gui().save(gui_path)
    print("wrote", os.path.relpath(gui_path, ROOT))


if __name__ == "__main__":
    main()
