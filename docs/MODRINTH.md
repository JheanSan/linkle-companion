# Modrinth page (ready to paste)

Create the project at <https://modrinth.com> > **+** (top right) > **Create a project**.

## Project settings
| Field | Value |
|---|---|
| Name | Linkle Companion |
| URL (slug) | `linkle-companion` (the mod's homepage link already points to modrinth.com/mod/linkle-companion) |
| Summary | A dual-crossbow companion who follows you, fights beside you and talks to you. Light, pretty, multiplayer-ready. |
| Project type | Mod |
| Categories | Adventure, Mobs (featured); Game Mechanics (extra) |
| Environment | Client: **required**. Server: **required** |
| License | MIT. Art is CC BY 4.0, stated in the description. |
| Icon | `docs/branding/icon-512.png` |
| Source code | https://github.com/JheanSan/linkle-companion |
| Issue tracker | https://github.com/JheanSan/linkle-companion/issues |
| Monetization | none |

## Description (Markdown, paste everything between the lines)

---

> **Unofficial fan project.** Linkle is a character from Hyrule Warriors, owned by Nintendo and Koei Tecmo.
> Not affiliated with or endorsed by them. All code, textures and dialogue are original.

**Linkle Companion** adds Linkle, a cheerful adventurer with **two crossbows**, as your follower.
Craft a **Wanderer's Compass**, use it, and she's at your side.

### What she does
- **Follows you** at a comfortable distance, teleports when she falls behind or gets stuck, goes through
  portals with you, opens doors, avoids lava and cliffs, and steps aside in tight corridors.
- **Fights smart**: loads one crossbow, then the other, and fires a double shot. Keeps her distance,
  strafes, backs off when enemies get close, and goes for whatever is attacking **you** first.
- **Twin Cyclone**: crowd her and she spins, firing a ring of bolts.
- **No friendly fire**: never hits you, your teammates, anyone's pets or villagers.
- **Three modes**: Follow, Stay (she sits) and Guard (she defends an area).
- **Knocked out, not dead**: she gets back up after a minute, or right away if you feed her. Real death is optional.
- **Inventory**: 4 armor slots + 9 pockets. She uses your arrows, eats when hurt and picks up stray arrows.
- **Personality**: short original lines for nightfall, new biomes, big fights, low health, gifts... in a
  **speech bubble above her head**. Never spammy.
- **Looks**: slim-arm model, swaying twin braids, five outfits, or your own skin via a resource pack.
- **Light**: vanilla AI, throttled checks, nothing heavy while idle, vanilla sounds and particles.

### Getting started
1. Craft the **Wanderer's Compass**: green dye on top; crossbow, compass, crossbow in the middle; gold
   ingot below. It shows up in your recipe book once you hold a compass or a crossbow.
2. Use it. Use it again any time to call her back (it isn't used up).
3. **Right-click** her to change mode, **sneak + right-click** for her inventory.

### Controls
| Key | Action |
|---|---|
| Right-click | Switch mode (Follow / Stay / Guard) |
| Sneak + right-click or **J** | Her inventory |
| **G** | Call her to you |
| **H** | Switch her mode from anywhere |

### Settings
Open them from **Mod Menu** (optional) or the **gear button** in her inventory: speech bubble or corner box,
chattiness, a master on/off switch, follow and teleport distances, damage, the Twin Cyclone, infinite arrows,
real death and more. Every option has a tooltip.

### Install
Works out of the box with the **Prism Launcher** (and forks like Freesm), the **Modrinth App** or the official
launcher with Fabric. Fabric API is fetched automatically by Prism and the Modrinth App. Install on **both**
client and server.

Code: MIT. Art: CC BY 4.0. Source and issues: [GitHub](https://github.com/JheanSan/linkle-companion)

### Tools disclaimer
In the spirit of full transparency, here is every tool used to make this mod:

- A **mouse**. It clicked things. Some of those clicks were important.
- A **keyboard**. The W key did most of the work, out of habit.
- A **monitor**, without which this would have been a very different, much darker project.
- A **chair** for sitting, and a **desk** for holding up all the other tools.
- A **computer**, and electricity, reportedly.
- And yes, **AI**, which was used to write the code, the dialogue and the script that draws the textures.

Ideas, direction and play-testing: me. None of the tools above are credited as contributors. The chair asked. The answer was no.

---

## Version upload
| Field | Value |
|---|---|
| File | `linkle_companion-1.0.0+mc26.3.jar` (NOT the `-sources` jar) |
| Version number | `1.0.0+mc26.3` |
| Version title | Linkle Companion 1.0.0 |
| Release channel | Release |
| Loaders | Fabric |
| Game versions | 26.3 |
| Dependencies | Fabric API: **required**. Mod Menu: **optional** |
| Changelog | Copy the 1.0.0 section of `CHANGELOG.md` |

## Gallery
Ready now, in `docs/images/` (1920x1080 except the two menu shots):
front.png, dual-aim.png, twin-cyclone.png, outfits.png, speech-bubble.png, stay-mode.png, inventory.png, settings.png.
Mark **front.png** (or a nicer hero shot) as **Featured**.

Nicer shots to add later, from a pretty world (shader optional, but keep one vanilla shot):
1. **Hero shot**: Linkle in front of a sunset, both crossbows raised.
2. **Twin Cyclone** mid-spin with the bolt ring (a GIF works great).
3. **Combat** at night: Linkle strafing while skeletons and zombies approach.
4. **Speech bubble** while exploring a new biome.
5. **Stay mode**: Linkle sitting by a campfire.
6. **Knocked out**, then fed back up.
7. The **Wanderer's Compass** recipe in a crafting table.
Tip: `./gradlew runClientGameTest` regenerates the reference shots in `build/run/clientGameTest/screenshots`.
