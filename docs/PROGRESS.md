# Progress

Honest status of the work. Updated after every feature.

## Task list
- [ ] 1. Project scaffold: Gradle/Loom, fabric.mod.json, git, docs skeleton
- [ ] 2. Core entity: LinkleEntity (owned, saved, one per player), registration, attributes, summon item + recipe, /linkle command
- [ ] 3. Movement: follow, distance, teleport (range + dimension change), avoid lava/fire/cliffs, open doors, don't block player
- [ ] 4. Modes: Follow / Stay / Guard via right-click with message
- [ ] 5. Inventory: 9 slots + armor, sneak+right-click screen, eats food when hurt, arrows from inventory, infinite arrows config
- [ ] 6. Knocked-out state instead of death, recovery timer, feed to revive, real-death config
- [ ] 7. Combat: dual crossbows, kiting/strafing/retreat, throttled target selection prioritizing owner's attackers, no friendly fire
- [ ] 8. Signature move: spinning volley ring of bolts, cooldown, vanilla particles + sound
- [ ] 9. Personality: event dialogue with cooldowns, HUD box, lang file; idle behaviors (look at owner, glance, check crossbows, sit in Stay)
- [ ] 10. Look: slim player model renderer, dual crossbow poses + charge anim, twin-tail hair layer with sway + config toggle
- [ ] 11. Skins: generator script, original 64x64 skin + variants, name-tag/config selection, resource-pack override path
- [ ] 12. Config: JSON config (follow distance, teleport range, damage, infinite arrows, hair sway, real death, ...)
- [ ] 13. Datagen: recipes, lang, tags, advancements
- [ ] 14. Game tests: ownership, teleport, mode switching, knocked-out cycle
- [ ] 15. Dedicated server run (runServer) with no crash
- [ ] 16. Launcher dev instance `dev-linkle_companion`, play-test as DevTester
- [ ] 17. Clean-install test instance (Fabric Loader + Fabric API + jar), then delete
- [ ] 18. Compatibility test instance with popular mods; docs/COMPATIBILITY.md
- [ ] 19. Repo polish: README, LICENSE, ASSETS.md, CHANGELOG, CONTRIBUTING, issue templates, GitHub Actions, icon
- [ ] 20. docs/MODRINTH.md, docs/RELEASE.md, docs/ARCHITECTURE.md, docs/TESTING.md
- [ ] 21. Final report

## Done
(nothing yet)

## In progress
- 1. Project scaffold

## Next step
- Finish the first successful `./gradlew build`, then inspect Minecraft 26.3 sources for the entity/renderer APIs.

## Needs a human (launcher clicks etc.)
(none yet)
