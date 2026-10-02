# Contributing

Thanks for helping! A few short rules:

1. **Everything must be original.** No textures, models, sounds, text or code taken from Nintendo or
   Koei Tecmo games, and no fan art or fan skins you don't own. Art changes go through the scripts
   in `scripts/art/` so they stay reproducible.
2. **Keep it light and compatible.** No mixins unless there is truly no Fabric API alternative
   (and then a small `@Inject`, documented in `docs/ARCHITECTURE.md`). Never replace vanilla data.
   Keep everything under the `linkle_companion` namespace. Client-only code goes in `src/client`.
3. **Test before you open a pull request:**
   - `./gradlew build` (compiles and runs the server game tests)
   - for visual changes, `./gradlew runClientGameTest` and look at the screenshots in
     `build/run/clientGameTest/screenshots`
4. **Text** belongs in the language file: edit `dialogue/Topic.java` or
   `datagen/ModLanguageProvider.java`, then run `./gradlew runDatagen`. Translations are welcome as
   new files in `src/main/resources/assets/linkle_companion/lang/`.
5. Small, focused pull requests with a clear description are easiest to review.

By contributing you agree that code is licensed MIT and art CC BY 4.0, like the rest of the project.
