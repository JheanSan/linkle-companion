# Testing

## Automated (run by me, results in PROGRESS.md)

| What | How to run | Covers |
|---|---|---|
| Server game tests (7) | `./gradlew build` | Ownership + save/load (owner, mode, guard point, inventory, skin); right-click mode cycle (and strangers can't change it); recall teleport and automatic follow-teleport; knockout + food revive; knockout timer recovery; real-death option; no friendly fire on villagers while monsters still take damage |
| Showcase client test | `./gradlew runClientGameTest` | Screenshots: front/back/side, dual aim, charging, inspect, volley, sitting, knocked out, 5 skins, 10-block distance, dialogue box, inventory screen |
| Combat client test | `./gradlew runClientGameTest` | Survival world: clears a zombie wave using inventory arrows, villager near the line of fire unharmed, volley fires when crowded, knockout + revive |
| Language client test | `./gradlew runClientGameTest` | Mod language ja_jp changes Linkle's text while vanilla stays English; "Same as game" restores English; game in es_mx borrows es_es; game de_de + mod ko_kr keep their own languages; survives a resource reload; screenshots of the settings in Japanese and the picker |
| Translation check | `./gradlew build` (`checkTranslations`) | Every lang file has exactly the en_us keys and the same number of `%s` placeholders |
| Every Minecraft version | `./gradlew build -Pmc=<mc>` / `runClientGameTest -Pmc=<mc>` | The server game tests run for each file in `versions/` (locally and in CI); the client play-tests were run on 26.1, 26.2 and 26.3 for 1.2.0 |
| Dedicated server | `./gradlew runServer`, and a real Fabric server in `testserver/` | Starts without client classes, summon/inspect over RCON |
| Multiplayer | `testserver/` + `./gradlew runJoinTestServer` (dev client as DevTester) | `/linkle summon`, ownership, modes, follow-teleport, Nether and back, real fight |
| Clean install (client) | `testserver/` + `./gradlew cleanInstallClient` | Built jar + Fabric API only, fresh folder, joins the server |

## Manual checklist (things I could not test, please try by hand)

### Needs the launcher
- [ ] In Freesm: add the offline account **DevTester** (Settings > Accounts > Add Offline), then launch the
      prepared `dev-linkle_companion` instance with it (`freesmlauncher.exe -l dev-linkle_companion -a DevTester`).
- [ ] Clean-install in the launcher: new instance with Fabric (26.3), add only Fabric API + the jar, launch,
      craft the compass in survival, summon her. Delete the instance afterwards.
- [ ] Prism / Modrinth App auto-download of Fabric API (only possible once the mod is on Modrinth).

### Settings menu and hotkeys
- [ ] Mod Menu > Linkle Companion > Configure opens the settings; the gear button in her inventory does too.
- [ ] Turn **Linkle enabled** off: she sits and stops fighting/talking; the compass refuses. Turn it on: she resumes.
- [ ] Change box position / time, chattiness quiet and chatty, braids off. Reset to defaults works.
- [ ] Press **G** far from her: she comes to you. Press **H**: her mode changes with a message. Press **J** within 16 blocks: her inventory opens.
- [ ] Her lines appear as a speech bubble above her head; a friend nearby sees them too.
- [ ] Trap her in a hole or behind water while you walk off: within ~3 seconds she teleports to you.
- [ ] On a server you don't host: gameplay options are greyed out with a tooltip.

### Gameplay by hand (single player, survival)
- [ ] Crafting: the Wanderer's Compass recipe appears in the recipe book after picking up a compass or crossbow.
- [ ] Use the compass: she appears with 32 arrows; use again far away: she comes back; sneak-use when she is
      in an unloaded area: a new one comes, the old one leaves (her items drop) when that area loads.
- [ ] Walk through a 1-block-wide corridor with her behind/in front of you: she steps out of the way.
- [ ] Doors: walk through a wooden door; she follows and closes it.
- [ ] Cliffs and lava: walk along a ravine edge and past lava; she doesn't fall or walk in.
- [ ] Nether portal: walk through with her following; she arrives with you. In Stay mode she does NOT follow.
- [ ] Night: she says a night line once; new biome lines appear but don't spam.
- [ ] Fight skeletons at night: she strafes, backs off, prioritizes the mob hitting you.
- [ ] Hit her yourself: no damage. Your dog/cat/villager next to the fight: never hit.
- [ ] Guard mode: put her at a door, walk away; she defends ~12 blocks around and walks back.
- [ ] Stay mode: she sits; hit her with a mob: she shoots back without moving.
- [ ] Knockout: let her get beaten; she lies down, mobs ignore her; feed bread: she gets up.
- [ ] Inventory: sneak + right-click; put armor on her (it renders); shift-click items in and out.
- [ ] Name tag "Azure": outfit changes. Resource pack skin in `custom.png` + name tag "Custom": shows your skin.
- [ ] Config: set `dialogueDisplay` to `actionbar` and `off`; `hairEnabled` false; `infiniteArrows` true.
- [ ] Listen: sounds are vanilla crossbow/eat sounds; nothing missing.

### Multiplayer by hand
- [ ] Two players on a server, each summons a Linkle: each only obeys her owner; Linkles don't fight each other.
- [ ] PvP off: she never targets the other player even if they hit you. PvP on + they hit you: she defends.
- [ ] A player **without** the mod tries to join a server **with** the mod: should be refused with a message
      about missing registry entries (Fabric's standard behavior). Not tested (needs a client without the mod).
- [ ] Claim mod (e.g. Flan) protecting an area: she doesn't pick up arrows or damage things there that the
      claim forbids for mobs.

### Stability
- [ ] Launch the game 10+ times with the mod on your own PC (and a friend's with a different GPU) and join a
      world where Linkle is visible. Report any instant game exit without a crash report (see
      COMPATIBILITY.md, "Intermittent native game crash").

### Before release
- [ ] All boxes above that you care about.
- [ ] `./gradlew build` green, GitHub Actions green.
- [ ] Screenshots for the Modrinth gallery.
