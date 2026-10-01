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

## Installs on this machine
- None needed so far: Temurin JDK 25.0.4, Git 2.55.0 and Python 3.14.7 were already installed; Gradle comes from the wrapper (9.7.1, downloaded into the Gradle user home).

## Launcher
- Using the existing portable **Freesm Launcher 2.3.1** at `C:\Users\Admin\Downloads\Games\FreesmLauncher-Windows-MinGW-w64-Portable-2.3.1` (found in Downloads/Games). It is portable mode, so instances live in its `instances/` folder. Only `dev-linkle_companion*` instances are created or touched.
- Offline testing uses the launcher's `-o DevTester` CLI flag (launch offline with a given player name). This avoids reading or editing `accounts.json`.

## Character design
- Hair is **blonde** braided twin tails (not brown as in the brief): every public description consulted says blonde, and recognizability matters most.
- No crest or emblem on the hood: the Hylian crest is a trademarked symbol.
