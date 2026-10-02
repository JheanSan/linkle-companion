# Compatibility

Only what was actually tested is listed here. Tested on 2026-10-01 with Minecraft 26.3,
Fabric Loader 0.19.5, Fabric API 0.161.0+26.3, Linkle Companion 1.0.0, Java 25, Windows 11.

## Test setup
- **Server**: a real Fabric dedicated server (`testserver/`), fresh *normal* world (so Terralith generated
  the terrain), with Linkle Companion plus the server-side mods below.
- **Client**: Loom's production launch of the built jar (`./gradlew cleanInstallClient -Pcompat`) as the
  offline player DevTester, plus the client-side mods below, joining that server.
- **Also**: the automated client tests (screenshots + combat play-test) with the same client mods.
  Note: that dev-environment run did not actually load the extra mods (the dev game ignores its
  `mods` folder), so only the production-client results below count.

## Mods tested together

| Mod | Version | Where it was loaded |
|---|---|---|
| Sodium | mc26.3-0.9.3-alpha.1 | client |
| Iris Shaders | 1.11.7+26.3 (no shader pack enabled) | client |
| Entity Culling | 1.11.2 | client |
| Mod Menu | 21.0.0 (+ Text Placeholder API 3.2.0+26.3) | client |
| Lithium | mc26.3-0.26.2 | client + server |
| FerriteCore | 9.0.0 | client + server |
| Jade | 26.3.3 | client + server |
| AppleSkin | 3.0.10+mc26.3 | client + server |
| Terralith (worldgen) | 2.6.5+26.3 (+ Lithostitched 2.0.4) | server |
| Flan (land claims) | 26.3-1.12.8.b | server |
| Better Pets (pet mod) | v4.3.6 | server (+ client jar present) |

55 mods loaded on the server, all 13 above plus Fabric API modules and Linkle on the client.

## What worked
- Server and client both started and the client joined; no warnings or errors from or about Linkle
  Companion in either log.
- `/linkle summon`, `recall`, `mode` worked; she spawned and followed on Terralith terrain.
- **Rendering** with Sodium + Iris + Entity Culling: she renders correctly (skin, braids, crossbows,
  idle crossbow-check pose). Screenshot taken from the game window.
- **Combat**: at midnight she killed zombies near the player; a still husk 12 blocks away died in
  8 seconds using 7 arrows.
- **No friendly fire with other mods present**: a wolf owned by another player (8/8 HP) and a villager
  (20/20 HP) standing next to the fight were never hit.
- Earlier, without extra mods: a client with Linkle joins a server **without** Linkle fine.

## Known issues / notes
- **Not a Linkle issue, seen in the logs**: Flan logs errors for its built-in data about mods that weren't
  installed (Mekanism, AE2, Create...); Better Pets logs a failed data function; AppleSkin's Mod Menu page
  needs Cloth Config; some mod probes for JEI classes. All harmless for this test.
- **Intermittent game crash in the production launch on this PC**: the game process sometimes exits
  with a native access violation (0xC0000005) during startup or right after joining, with no Java crash
  log (Windows Error Reporting is disabled on this machine, so no dump either). Seen 5 times in about
  20 production launches with Linkle; not seen in the dev client (~15 launches), the client game tests
  or the server, and not reproducible on demand (11 clean runs in a row before the compat run).
  Control runs without Linkle: see PROGRESS.md for the final tally. Linkle contains no native code.
  Please report it if you see it on another machine.
- A zombie stuck in terrain 19 blocks away was left alone: Linkle only engages threats within 14 blocks
  of her owner (by design, so she doesn't wander off hunting).

## Not tested
- Other mods in this series (Skyloft, Beetle): they don't exist yet.
- Iris **with a shader pack** enabled (no shader pack was installed; she uses standard entity render
  types, which shader packs support).
- Flan claim *rules* against Linkle (e.g. a claim that forbids mob item pickup); Flan was only loaded
  together with her.
- Skin mods (e.g. Skin Layers 3D): none was installed. Linkle doesn't use the player renderer, so
  player-skin mods shouldn't touch her, but this is unverified.
- macOS / Linux.
