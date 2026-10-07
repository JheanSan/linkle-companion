# Changelog

## 1.2.0: More Minecraft versions

- **Now available for Minecraft 26.1, 26.1.1, 26.1.2, 26.2 and 26.3** (Fabric). Each Minecraft version
  has its own download: pick the file whose name ends in your version (`+mc26.1` covers 26.1 to 26.1.2).
- Same features and the same 16 languages on every version. Every version is built and passes the
  automated game tests before release, and the in-game play-tests (combat, looks, menus, languages)
  were run on each one.
- On 26.1.x, `/linkle` permissions use the vanilla operator levels only: Fabric API for 26.1 has no
  permission API, so permission mods can't change them there.
- Nothing changes for players already on 26.3.

## 1.1.0: Linkle speaks your language

- **16 languages**: English, Deutsch, Español, Français, Italiano, 日本語, 한국어, Polski,
  Português (Brasil), Русский, Türkçe, Українська, Tiếng Việt, Bahasa Indonesia, 简体中文 and 繁體中文.
  Everything is translated: her dialogue, messages, settings and tooltips, hotkeys, advancements and item names.
- **Mod language button**: at the top of the settings (Mod Menu or the ⚙ button in her inventory).
  Pick any language, or "Same as game". It only changes Linkle's text; the rest of Minecraft keeps its
  own language. Takes effect right away, no restart.
- Regional game languages without their own file use the closest one instead of English
  (for example Español (México) uses Español, Português (Portugal) uses Português (Brasil),
  繁體中文 (香港) uses 繁體中文).
- Resource packs can add or fix translations: any `assets/linkle_companion/lang/<code>.json` shows up
  in the language list by itself.
- New config option `language` (`auto` by default). Worlds and settings from 1.0.0 work unchanged.

## 1.0.0

First release, for Minecraft 26.3 (Fabric Loader 0.19.5+, Fabric API 0.161.0+).

- Linkle, a dual-crossbow companion: one per player, saved with the world, works in multiplayer.
- Wanderer's Compass to summon and recall her; `/linkle` command; befriend a wild `/summon`ed Linkle.
- Follow, Stay and Guard modes (right-click). Teleports when left behind and follows through portals.
- Dual crossbow combat with strafing and retreating; targets what attacks you first; no friendly fire.
- Twin Cyclone signature volley.
- Knocked out instead of dying; feed her to wake her up. Optional real death.
- 9-slot inventory plus armor; eats food when hurt; uses and picks up arrows.
- Original dialogue lines for night, biomes, fights, low health, gifts and more.
- Original slim-arm skin with swaying twin braids and five outfits; resource-pack skin swapping.
- In-game settings menu (Mod Menu "Configure" button or the ⚙ button in her inventory) with
  tooltips, a master on/off switch, chattiness, dialogue box position and time, and toggles for
  teleporting, portals, the volley, auto-eat and arrow pickup. Reset to defaults.
- Hotkeys: G calls Linkle, H switches her mode, J opens her inventory from up to 16 blocks (rebindable).
- Speech bubbles above her head (default); everyone nearby sees them.
- She teleports to you when she's stuck (no progress for ~3 seconds) instead of standing there.
- Cuter skin: white eyes with lash corners, rosier cheeks, softer mouth; new portrait icon.
- JSON config.
