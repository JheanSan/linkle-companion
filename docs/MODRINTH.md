# Modrinth page (ready to paste)

## Title
Linkle Companion

## Summary (max ~120 characters)
A dual-crossbow companion who follows you, fights beside you and talks to you. Light, pretty, multiplayer-ready.

## Project type, loaders, versions
- Type: Mod
- Loader: Fabric
- Minecraft: 26.3
- Environment: Client **required**, Server **required** (install on both; players need it to join a modded server)
- Dependency: Fabric API (required)
- License: MIT (code), CC BY 4.0 (art) -> choose "MIT" on Modrinth and mention CC BY 4.0 in the description
- Monetization: none

## Categories / tags
Primary: **Adventure**, **Mobs**
Additional: **Game Mechanics**, **Utility** (optional)

## Description (Markdown)

> **Unofficial fan project.** Linkle is a character from Hyrule Warriors, owned by Nintendo and Koei Tecmo.
> Not affiliated with or endorsed by them. All code, textures and dialogue are original.

**Linkle Companion** adds Linkle, a cheerful adventurer with **two crossbows**, as your follower.
Craft a **Wanderer's Compass**, use it, and she's by your side.

### What she does
- **Follows you** at a comfortable distance, teleports when she falls behind, goes through portals
  with you, opens doors, avoids lava and cliffs, and steps aside in tight corridors.
- **Fights smart**: loads one crossbow, then the other, and fires a double shot. Keeps her distance,
  strafes, backs off when enemies get close, and goes for whatever is attacking **you** first.
- **Twin Cyclone**: crowd her and she spins, firing a ring of bolts.
- **No friendly fire**: never hits you, your teammates, anyone's pets or villagers.
- **Three modes**: Follow, Stay (she sits) and Guard (she defends an area). Right-click to switch.
- **Knocked out, not dead**: she gets back up after a minute, or right away if you feed her.
- **Inventory**: 9 pockets + armor. She uses your arrows, eats when hurt, and picks up stray arrows.
- **Personality**: short original lines for nightfall, new biomes, big fights, low health, gifts...
  in a small box with her face. Never spammy.
- **Looks**: slim-arm model, swaying twin braids, five outfits, and your own skin via resource pack.
- **Light**: vanilla AI, throttled checks, nothing heavy while idle, vanilla sounds and particles.

### Getting started
1. Craft the **Wanderer's Compass**: green dye on top, crossbow - compass - crossbow in the middle,
   gold ingot below. It shows up in your recipe book once you have a compass or crossbow.
2. Use it. Use it again any time to call her back.
3. Right-click her to change mode, sneak + right-click for her inventory.

### Install
Works out of the box with the **Prism Launcher** (and forks like Freesm), the **Modrinth App** or the
official launcher with Fabric. Fabric API is fetched automatically by Prism and the Modrinth App.
Install on **both** client and server.

### Config
`config/linkle_companion.json` (optional): follow distance, teleport range, damage, infinite arrows,
volley cooldown, real death, braids on/off, dialogue display, and more.

Art: CC BY 4.0. Code: MIT. Source and issue tracker: (add your GitHub link here)

## Gallery shot list
Take these in a nice-looking world (shader optional, but also one vanilla shot):
1. **Hero shot**: Linkle in front of a sunset, both crossbows raised (dual aim pose).
2. **Twin Cyclone** mid-spin with the bolt ring and sweep particles (GIF if possible).
3. **Combat**: Linkle strafing while skeletons and zombies approach at night.
4. **Outfits**: the five skins side by side (classic, crimson, azure, violet, snow).
5. **Dialogue box**: a line in the top-left box while exploring a new biome.
6. **Inventory screen** with armor and arrows.
7. **Stay mode**: Linkle sitting by a campfire.
8. **Knocked out** and then being fed back up.
9. **Recipe** of the Wanderer's Compass in the crafting table.
Tip: `./gradlew runClientGameTest` produces quick reference screenshots in
`build/run/clientGameTest/screenshots`.
