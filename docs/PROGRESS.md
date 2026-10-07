# Progress

Honest status of the work. Updated after every feature.

## Task list
- [x] 1. Project scaffold: Gradle/Loom, fabric.mod.json, git, docs skeleton
- [x] 2. Core entity: LinkleEntity (owned, saved, one per player), summon item + recipe, /linkle command
- [x] 3. Movement: follow, teleport (range + dimension change), avoid lava/fire/cliffs, doors, give way
- [x] 4. Modes: Follow / Stay / Guard via right-click with message
- [x] 5. Inventory: 9 slots + armor screen, eats when hurt, arrows from inventory, infinite-arrows option
- [x] 6. Knocked out instead of dying, timer, feed to revive, real-death option
- [x] 7. Combat: dual crossbows, kiting/strafing/retreat, throttled targeting (owner's attackers first), no friendly fire
- [x] 8. Twin Cyclone volley with cooldown, vanilla particles + sounds
- [x] 9. Dialogue (HUD box / action bar / off, cooldowns, lang file) + idle behaviors
- [x] 10. Look: slim player model, crossbow poses + vanilla charge animation, swaying twin braids + toggle
- [x] 11. Skins: generator scripts, original skin + 4 outfits + custom slot, name tag / command / config
- [x] 12. JSON config with validation
- [x] 13. Datagen: recipe, lang, tags, advancements
- [x] 14. Game tests: 9 server tests + 2 client tests (showcase screenshots, combat play-test)
- [x] 15. Dedicated server (runServer and a real Fabric server): no crash
- [x] 16. Launcher: the `dev-linkle_companion` instance starts in Freesm with the author's account (title screen,
       Linkle + Mod Menu loaded). In-world play-testing was done with the dev client and the production launch
       on a real server; hands-on play in the launcher is on the TESTING.md checklist.
- [~] 17. Clean-install test: done with Loom's production launch (built jar + Fabric API only, fresh folder,
       joins a real server). A launcher clean-install instance is still a manual step (TESTING.md).
- [x] 22. In-game settings menu (Mod Menu + gear button), master switch, QoL toggles, hotkeys G/H
- [x] 18. Compatibility test (13 mods) + docs/COMPATIBILITY.md
- [x] 19. Repo polish: README, CHANGELOG, CONTRIBUTING, issue templates, GitHub Actions
- [x] 20. docs: ARCHITECTURE, TESTING, MODRINTH, RELEASE, COMPATIBILITY, DECISIONS
- [x] 21. Final report (in the conversation; summary below)
- [x] 23. 1.1.0: 15 translations + "Mod language" setting and picker, translation check in the build,
       language client test (see "1.1.0" below)

## Evidence (all run on 2026-10-01)
- `./gradlew build`: BUILD SUCCESSFUL, "All 11 required tests passed" (10 mod tests + Fabric's own).
- `./gradlew runClientGameTest`: showcase screenshots + combat test PASSED (still husk at 12 blocks: 6 arrows;
  wave cleared; villager unharmed; volley fired; knockout + revive).
- Real Fabric server + DevTester: summon, owner UUID, modes, follow-teleport after 40 blocks, Nether and back,
  fights, no damage to another player's wolf or a villager.
- Compatibility: Sodium, Iris, Entity Culling, Mod Menu, Lithium, FerriteCore, Jade, AppleSkin, Terralith,
  Flan, Better Pets: loaded together, no Linkle errors, renders under Sodium + Iris.

## Problems found by play-testing and fixed
- Rendered as Steve (26.3 routes player render states to the player renderer) -> own render state.
- Flat yellow back of the head -> hair strands and braid roots in the skin.
- Dual-aim crossbows overlapped -> arms angle outward.
- Summoning granted the vanilla "tame an animal" advancement -> no longer.
- Bolts overshot at range (vanilla mob aim formula) -> real gravity drop; about half the arrows per kill.
- Native game crashes in production launches correlated with the hand-encoded PNGs -> all textures re-encoded
  with ImageIO (5/13 crashes -> 1/20; no mod: 0/14).
- Test clients were noisy -> all test runs start muted.

## Known rough edges
- One unexplained native crash remained in 20 production launches (see COMPATIBILITY.md).
- Idle crossbows point forward from her hands (vanilla item pose); in the knocked-out pose one crossbow sticks up.
- Stuck-arrow layer (arrows stuck in her body) is not shown.
- Gameplay items that need hands-on play (corridor give-way, doors, cliffs, guard mode, claims) are on the
  manual list in TESTING.md.

## Release prep (done, nothing uploaded)
- Author JheanSan, links to github.com/JheanSan/linkle-companion and modrinth.com/mod/linkle-companion.
- Store texts: docs/MODRINTH.md, docs/CURSEFORGE.md; gallery in docs/images, icon in docs/branding.
- `python scripts/release/make_release_kit.py` builds, tests and fills `dist/` (jar verified: strict JSON
  fabric.mod.json, version 1.0.0+mc26.3, no gametest classes).
- Tag-triggered release workflow publishes to GitHub Releases, and to Modrinth/CurseForge once ids and tokens are set.

## Release status (2026-10-02)
- GitHub: public at github.com/JheanSan/linkle-companion (history uses the noreply email, no AI co-author trailers).
- CurseForge: project 1721991 created, 1.0.0 file uploaded and waiting for moderation.
- Modrinth: complete draft (linkle-companion), NOT submitted. Modrinth rule 6.2 forbids publishing projects whose
  contents are primarily AI output, which applies here; the author decides whether to ask Modrinth staff first.

## 1.1.0: languages (2026-10-07)
- Languages: en_us + de_de, es_es, fr_fr, it_it, ja_jp, ko_kr, pl_pl, pt_br, ru_ru, tr_tr, uk_ua, vi_vn,
  id_id, zh_cn, zh_tw. Every key translated (191 per file). Written with AI help; corrections welcome.
- "Mod language" button in the settings opens a picker ("Same as game" + all found languages). Changes only
  Linkle's text, instantly, and survives resource reloads. Regional game languages borrow a close file.
- `./gradlew build`: checkTranslations OK (15 files x 191 keys) and all server game tests passed.
- `./gradlew runClientGameTest`: BUILD SUCCESSFUL; showcase + combat tests still pass; language test passed
  (16 languages found; ja_jp override with vanilla still English; auto back to English; es_mx -> es_es;
  game de_de + mod ko_kr; survives a resource reload). Screenshot of the picker: docs/images/language-picker.png.

## Release status 1.1.0 (2026-10-07)
- GitHub: v1.1.0 tag pushed; release workflow passed (Linux build + game tests) and published
  github.com/JheanSan/linkle-companion/releases/tag/v1.1.0 with the jar. Contributors list: JheanSan only.
- CurseForge: 1.1.0 file uploaded through the author dashboard (auto-publish once approved; status was
  "Uploading" right after upload). 1.0.0 was approved. Description got a "What's new in 1.1.0" section.
- Modrinth: still not public (API 404), skipped as the author asked.

## Next step
- Wait for CurseForge moderation of 1.1.0; answer any moderator message from the CurseForge author dashboard.
