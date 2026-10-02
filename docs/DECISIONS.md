# Decisions log

One line per decision: what was decided and why.

## Versions and tooling
- Minecraft **26.3** (latest stable on meta.fabricmc.net on 2026-09-30; 26.4 is only snapshots) is the stable target.
- Fabric Loader **0.19.5**: newest stable loader on meta.fabricmc.net, and the one the official example mod uses.
- Fabric API **0.161.0+26.3**: newest Fabric API build for 26.3 on maven.fabricmc.net (0.161.1+/0.161.2 target 26.4 snapshots).
- Loom **1.18-SNAPSHOT** plugin `net.fabricmc.fabric-loom` and Gradle **9.7.1**, copied from the official fabric-example-mod template.
- Java **25**: required by Minecraft 26.x and the template.
- Mojang official names (no Yarn): 26.x ships unobfuscated, and the template has no mappings line.
- Java package `dev.linklecompanion`: the author has no fixed domain or GitHub user yet, and this is neutral and easy to rename.
- `fabric.mod.json` uses `"minecraft": "~26.3"` so any 26.3.x patch works and other versions get a clear loader error.

## Installs and downloads on this machine
- No system installs were needed: Temurin JDK 25.0.4, Git 2.55.0 and Python 3.14.7 were already installed.
- Gradle 9.7.1 (via the wrapper, into `~/.gradle`), plus Minecraft 26.3, Loom and Fabric API dependencies (via Gradle): needed to build.
- Fabric server launcher `testserver/fabric-server-launch.jar` (MC 26.3, Loader 0.19.5, installer 1.1.2) from meta.fabricmc.net: a real dedicated server for multiplayer and install tests. `testserver/` is git-ignored.
- Fabric API 0.161.0+26.3 for test instances is copied from the Gradle cache (downloaded from maven.fabricmc.net, the same file Modrinth hosts), not downloaded again.
- Python scripts use only the standard library (no pip packages).

## Launcher
- Using the existing portable **Freesm Launcher 2.3.1** at `C:\Users\Admin\Downloads\Games\FreesmLauncher-Windows-MinGW-w64-Portable-2.3.1` (found in Downloads/Games). It is portable mode, so instances live in its `instances/` folder. Only `dev-linkle_companion*` instances are created or touched.
- Tried the launcher's `-o DevTester` flag (offline mode with a name). Result: Freesm 2.3.1 ignored the name and joined the local test server as the default account's profile name (no real credentials: offline mode sends a dummy token, auth returned 401). The game was closed right away, the test server's user cache was cleared, and `-o` is no longer used. Lesson: in this Freesm version the offline *name* is only honored through an offline *account*.
- An offline account "DevTester" must be added in the launcher UI (Settings > Accounts > Add Offline). The launcher is a portable Qt app that the desktop-automation tools can't drive, and editing `accounts.json` is off limits, so this is listed in PROGRESS.md under "Needs a human". After that, tests launch with `freesmlauncher.exe -l <instance> -a DevTester`.
- Meanwhile, game play-tests run in the Loom dev client with `--username DevTester` (no launcher account involved) joining the real Fabric test server, plus Fabric client game tests for screenshots.
- The launcher's shared `libraries/` cache was missing 33 vanilla/LWJGL files from an earlier interrupted download; they were fetched from the official Mojang/LWJGL URLs listed in the launcher's own metadata and SHA-1 verified (script kept in the session scratchpad, not the repo). Assets were already complete.
- Minecraft 26.3 servers default to `white-list=true`; the test server whitelists only DevTester.

- Later the author allowed using their own account ("Jean") for launcher testing. The instance is launched with
  `freesmlauncher.exe -l dev-linkle_companion` (default account). Instance memory is 2 GB because the PC
  often has under 3 GB free and the launcher otherwise stops at a "Low free memory" dialog.

## Settings menu and hotkeys
- Settings screen built from vanilla `OptionsSubScreen` + `OptionInstance`: looks and scrolls like vanilla settings, no extra library.
- Mod Menu integration is compile-only (`clientCompileOnly` from maven.terraformersmc.com) plus a `modmenu` entrypoint and a `suggests` entry: players without Mod Menu are unaffected. A gear button in Linkle's inventory opens the same screen without Mod Menu.
- "Mod on/off": a mod can't unload while the game runs, so the master switch `enabled` stops summoning and pauses existing Linkles (they sit, don't fight, don't talk) without touching their saved mode.
- Gameplay options are owned by whoever runs the world; on someone else's server they are greyed out with a tooltip instead of silently doing nothing.
- Hotkeys default to G (call her) and H (switch mode): both unused by vanilla 26.3; rebindable.

- Speech bubbles above her head are the default way her lines appear (author's request). They use the vanilla
  name-tag drawing, are sent to every player tracking her, and are wrapped once on arrival. The corner box and
  action bar remain options and show only your own Linkle's lines. Config version 2 moves old files from the
  old default (`hud`) to `bubble` once.
- Unstuck: once a second, if she is in Follow mode, not fighting, farther than follow range and hasn't moved
  half a block for 3 checks, she teleports to a safe spot next to her owner (respects "teleport when far").
- Inventory hotkey J works within 16 blocks; the menu stays open within the same range.

## Character design
- Hair is **blonde** braided twin tails (not brown as in the brief): every public description consulted says blonde, and recognizability matters most.
- No crest or emblem on the hood: the Hylian crest is a trademarked symbol.

## Architecture
- Base class `TamableAnimal`: vanilla owner storage, sitting, teams and the owner-defense target goals for free; claim and pet mods already treat tamed animals as pets.
- One Linkle per player is tracked with a Fabric data attachment on the player (`linkle_companion:companion`: Linkle UUID + "generation"). A replaced Linkle leaves (dropping her items) the next time her chunk loads and her owner is online.
- Knockout is done by overriding `die()` in her own class (no mixin, no event): it covers every damage path. `/kill` and the void (`#bypasses_invulnerability`) still really kill her, so admins keep control.
- Owner damage to Linkle is blocked (accidental hits are common with a melee owner); her bolts never damage friends (Fabric `ALLOW_DAMAGE` event) and she doesn't shoot when a friend is in the line of fire.
- Crossbow loading uses vanilla item use (`startUsingItem`) so the crossbow's own pull/charged visuals and sounds are real; firing is custom so the damage config, pickup rules and per-hand origin can apply.
- Her crossbows get Quick Charge II, Unbreakable and no enchant glint: Linkle's style is fast double shots, and the glint looked noisy on a character. If a data pack removes Quick Charge, she logs one warning and reloads at normal speed.
- Starting arrows: a new Linkle brings 32 arrows (config `startingArrows`) because `infiniteArrows` defaults to false; without that she would be useless out of the box.
- Default `infiniteArrows = false`: keeps her balanced in survival; the brief says infinite only "if the config says so".
- Vanilla `mobGriefing` gamerule gates her picking up arrow items, like vanilla mob looting.
- Renderer uses a `HumanoidRenderState`, not the player's `AvatarRenderState`: Minecraft 26.3 sends every AvatarRenderState to the player renderer (it drew Steve). This also keeps player-skin mods from touching her.
- Twin tails are part of her model (4 cubes under the head) instead of a separate render layer: same look, one draw call; the config toggle hides them. Their texture comes from the unused skin area (24,0)-(40,8) (the same area vanilla uses for the "deadmau5 ears"), so any custom skin works.
- Stuck-arrow layer dropped: the vanilla `ArrowLayer` only works with player render states.
- Signature move is named "Twin Cyclone" (original name).
- Summon item "Wanderer's Compass": original name; not consumed, so it doubles as a recall whistle. Recipe: compass + 2 crossbows + green dye + gold ingot using `c:` tags.
- Wild Linkle: vanilla `/summon linkle_companion:linkle` makes an ownerless Linkle you can befriend by right-clicking (if you don't already have one). `/linkle summon` (op level 2 by default) gives you one directly.
- Commands use Fabric's permission API (`linkle_companion.command.<name>` nodes) with vanilla op levels as the fallback.
- Datagen and game-test code: datagen classes live in the main source set (Fabric docs default, a few KB, never run in game); game tests live in their own `src/gametest` source set and are not shipped.
- `fabric.mod.json` has no `contact` URLs yet: the GitHub repo doesn't exist; RELEASE.md tells the author to add them.
- No mixins at all so far: everything uses Fabric API events or overrides inside the mod's own classes.

## Textures
- All shipped PNGs are re-saved with Java's standard ImageIO encoder (`scripts/art/ReencodePng.java`, called by both art scripts). Reason: production launches with the hand-written encoder's PNGs crashed natively (0xC0000005, during resource loading or when Linkle first came into view) in 5 of 13 launches; with ImageIO-encoded files, 1 crash in 20; with no mod at all, 0 in 14. The old files were structurally valid, so the exact cause inside the native decoder is unknown; the re-encode is the evidence-based fix.
- Bolt aiming uses the real gravity drop for her 3.15-speed bolts (`0.0028 * distance^2`) instead of vanilla's `0.2 * distance`, which is tuned for slow mob arrows and made her overshoot at range.
