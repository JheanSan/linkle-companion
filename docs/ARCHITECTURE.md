# Architecture

A plain-language tour of the code. Minecraft 26.3 is unobfuscated, so all class and method names
are Mojang's official names.

## Big picture

```
src/main      runs on both the client and the server (entity, AI, items, commands, data)
src/client    only on the client (rendering, inventory screen, dialogue box)
src/gametest  automated tests, never shipped in the jar
src/main/generated   files written by data generation (recipe, lang, tags, advancements)
scripts/art   Python scripts that paint the original textures
```

Linkle is a normal entity built on vanilla's `TamableAnimal` (the base class of wolves and cats).
That gives her owner saving, sitting, team handling and "defend my owner" targeting for free, and
other mods that respect tamed pets (claim mods, pet mods) treat her as a pet.

**Mixins: none.** Everything is done with Fabric API events, Fabric registries, and overrides inside
the mod's own classes. Nothing in vanilla is replaced.

## src/main (common)

| File | What it does |
|---|---|
| `LinkleCompanion.java` | Entry point. Loads the config, registers the entity, item, menu, data attachment, network payload and events. |
| `LinkleEvents.java` | Fabric event hooks: blocks damage from Linkle's bolts to her friends (and owner hits to her), brings a following Linkle through portals, registers `/linkle`. |
| `config/LinkleConfig.java` | The JSON config. Reads, validates (bad values fall back to defaults with a log line) and writes `config/linkle_companion.json`. |
| `registry/ModEntities.java` | Registers the `linkle_companion:linkle` entity type and its attributes. |
| `registry/ModItems.java` | Registers the Wanderer's Compass and adds it to the Tools & Utilities creative tab. |
| `registry/ModMenus.java` | Registers Linkle's inventory menu type (opening data = her entity id). |
| `registry/ModAttachments.java` | Data saved on each player: their Linkle's UUID and a "generation" counter (one Linkle per player). |
| `registry/ModTags.java` | The mod's entity tags: `never_target` (villagers, golems...) and `ignored_targets` (endermen, breezes, creakings). |
| `entity/LinkleEntity.java` | Linkle herself: synced state (mode, skin, knocked out, aiming...), goals, interaction (feeding, arrows, modes, inventory), friend/foe rules, knockout instead of death, inventory and saving. |
| `entity/LinkleMode.java` | Follow / Stay / Guard. |
| `entity/LinkleVariant.java` | The skins (classic, crimson, azure, violet, snow, custom) and name-tag matching. |
| `entity/LinkleSummoning.java` | Summon, recall, befriend a wild Linkle, safe teleport spots, cross-dimension teleport. |
| `entity/CrossbowShooting.java` | Fires a loaded crossbow from the correct hand, and the volley bolts. Applies damage config and pickup rules. |
| `entity/goal/KnockedOutGoal.java` | While knocked out, holds all movement so nothing else runs. |
| `entity/goal/VolleyGoal.java` | The Twin Cyclone: when crowded, a 1-second spin firing 12 bolts in a ring, skipping directions with friends. |
| `entity/goal/LinkleCrossbowAttackGoal.java` | Combat: load right, load left, aim, fire both; keep 7-12 blocks away, strafe, retreat, close in; never fire through a friend. |
| `entity/goal/LinkleFollowOwnerGoal.java` | Follow mode movement and teleporting when too far behind (distances from the config). |
| `entity/goal/GuardPositionGoal.java` | Guard mode: walk back to the guard point. |
| `entity/goal/GiveWayGoal.java` | Steps aside when the owner walks into her (checked twice a second). |
| `entity/goal/LinkleStrollGoal.java` | Small wanders when idle near the owner (or when wild). |
| `entity/goal/InspectCrossbowsGoal.java` | Idle animation: looks over a crossbow now and then. |
| `entity/goal/LookAtOwnerGoal.java` | Idle animation: glances at the owner. |
| `entity/goal/LinkleThreatTargetGoal.java` | Once a second, picks the hostile mob to shoot (mobs attacking the owner first). Vanilla goals handle "who hurt my owner / me". |
| `dialogue/Topic.java` | Every dialogue topic with its English lines, cooldown and priority. The single source for the lang file. |
| `dialogue/LinkleDialogue.java` | Decides when she speaks (night, biome, fights, health, idle) with per-topic cooldowns and a global gap; sends the line to the owner. |
| `network/DialoguePayload.java` | The tiny server-to-client packet: skin id + lang key. |
| `menu/LinkleMenu.java` | Inventory menu: 4 armor slots, 9 pockets, player inventory, shift-click rules. |
| `item/WanderersCompassItem.java` | The summon item (summon, recall, sneak to replace a lost Linkle). |
| `command/LinkleCommand.java` | `/linkle summon|recall|mode|skin|info|dismiss`, each with a Fabric permission node. |
| `datagen/*` | Data generation: recipe, English lang file, tags and advancements. Runs only with `./gradlew runDatagen`. |

## src/client

| File | What it does |
|---|---|
| `LinkleCompanionClient.java` | Registers the model layer, renderer, inventory screen, HUD element and packet receiver. |
| `render/LinkleRenderState.java` | Per-frame data for drawing (texture, pose flags, volley progress, hair settings). Deliberately not the player's render state (see DECISIONS.md). |
| `render/LinkleModel.java` | The slim player mesh plus four cubes of twin braids; extra poses (dual aim, inspect, volley, knocked out) and the hair sway. |
| `render/LinkleRenderer.java` | Picks the texture, arm poses, spin and knocked-out rotation; adds vanilla armor, held item, head item and elytra layers. |
| `screen/LinkleScreen.java` | The inventory screen with a live preview of her and a status line. |
| `hud/DialogueHud.java` | The speech box (her face from her own skin, name, line), or the action bar, or off. |

## Performance notes
- AI uses vanilla goals. Scans that look at other entities (threat search, arrow pickup, owner
  checks, dialogue checks) run once a second, staggered by entity id.
- No per-tick allocations in her tick, goals or renderer (textures are looked up in a prebuilt map).
- When she has nothing to do, only cheap boolean checks run.

## Compatibility notes
- Everything lives under the `linkle_companion` namespace (ids, lang keys, config file, payload, tags,
  attachment, permission nodes).
- Recipes use `c:` tags (`c:tools/crossbow`, `c:ingots/gold`, `c:dyes/green`); food uses the item's
  food data and skips `c:foods/food_poisoning`; arrows use `#minecraft:arrows`.
- World changes go through vanilla/Fabric paths that claim mods already hook: item use, entity
  damage, the `mobGriefing` gamerule for picking up items.
- If a data pack removes the Quick Charge enchantment, she logs one warning and loads at normal speed.
