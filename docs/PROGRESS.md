# Progress

Honest status of the work. Updated after every feature.

## Task list
- [x] 1. Project scaffold: Gradle/Loom, fabric.mod.json, git, docs skeleton
- [x] 2. Core entity: LinkleEntity (owned, saved, one per player), registration, attributes, summon item + recipe, /linkle command
- [x] 3. Movement: follow, distance, teleport (range + dimension change), avoid lava/fire/cliffs, open doors, give way to owner
- [x] 4. Modes: Follow / Stay / Guard via right-click with message
- [x] 5. Inventory: 9 slots + armor, sneak+right-click screen, eats food when hurt, arrows from inventory, infinite arrows config
- [x] 6. Knocked-out state instead of death, recovery timer, feed to revive, real-death config
- [x] 7. Combat: dual crossbows, kiting/strafing/retreat, throttled target selection prioritizing owner's attackers, no friendly fire
- [x] 8. Signature move: Twin Cyclone ring of bolts, cooldown, vanilla particles + sound
- [x] 9. Personality: event dialogue with cooldowns, HUD box / action bar, lang file; idle behaviors
- [x] 10. Look: slim player model renderer, dual crossbow poses + charge anim, twin-tail hair with sway + toggle
- [x] 11. Skins: generator script, original 64x64 skin + 4 variants + custom slot, name-tag/command/config selection
- [x] 12. Config: JSON config with validation
- [x] 13. Datagen: recipe, lang, tags, advancements
- [x] 14. Game tests: ownership, teleport, mode switching, knockout cycle, real death, friendly fire (7 server tests) + 2 client tests
- [x] 15. Dedicated server run (runServer and a real Fabric server) with no crash
- [ ] 16. Launcher play-test as DevTester — BLOCKED, needs a human (see below). Play-tested instead with the dev client as DevTester on the real test server.
- [ ] 17. Clean-install test instance — launcher part blocked (same reason); server side done
- [ ] 18. Compatibility test with popular mods; docs/COMPATIBILITY.md
- [ ] 19. Repo polish: README, CHANGELOG, CONTRIBUTING, issue templates, GitHub Actions
- [ ] 20. docs/MODRINTH.md, docs/RELEASE.md, docs/ARCHITECTURE.md, docs/TESTING.md
- [ ] 21. Final report

## Done (with evidence)
- `./gradlew build`: BUILD SUCCESSFUL, "All 8 required tests passed" (7 mod tests + Fabric's own).
- `./gradlew runServer`: server reached "Done", Linkle summoned/inspected/killed over RCON, no mod errors.
- Real Fabric dedicated server (`testserver/`, built jar + Fabric API only) with the dev client as DevTester:
  summon via `/linkle summon`, owner UUID = DevTester's, mode commands, follow-teleport after a 40-block jump,
  Nether trip and back, fight vs 2 zombies + 1 skeleton (all dead in 14 s, Linkle 30/30 HP, 12 arrows used).
- Client with the mod joins a server *without* the mod: works (world just warns about the missing data pack).
- `./gradlew runClientGameTest`: showcase screenshots (poses, skins, HUD, inventory) and combat test PASSED
  (wave cleared, arrows used from inventory, villager unharmed, volley fired, knockout + food revive).

## In progress
- 18. Compatibility test.

## Next step
- Compatibility test: download popular Fabric mods for 26.3 from Modrinth into the dev run folders, launch, read logs.
- Then docs (README, ARCHITECTURE, TESTING, MODRINTH, RELEASE, COMPATIBILITY), CI, polish, final report.

## Needs a human (launcher clicks etc.)
- **Add the offline account "DevTester" in Freesm Launcher**: Settings > Accounts > Add Offline > name `DevTester`.
  The launcher is a portable Qt app that the automation tools can't drive, and `accounts.json` must not be edited.
  After that: `freesmlauncher.exe -l dev-linkle_companion -a DevTester` launches the prepared instance.
  Do not use `-o DevTester`: Freesm 2.3.1 ignores the name and uses your default profile name (see DECISIONS.md).
- The instance `dev-linkle_companion` already exists (Fabric Loader 0.19.5, Fabric API, the mod jar).
