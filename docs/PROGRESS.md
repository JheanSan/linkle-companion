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

## Next step
- Author: play in the open `dev-linkle_companion` instance and go through docs/TESTING.md.
- Then follow docs/RELEASE.md.
