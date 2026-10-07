# Supported Minecraft versions

One codebase builds one jar per Minecraft version. Today: **26.3** (primary), **26.2**, **26.1.x**.

| File | Minecraft | Fabric API | Compat family |
|---|---|---|---|
| `versions/26.3.properties` | 26.3 | 0.161.0+26.3 | `mc26_2` |
| `versions/26.2.properties` | 26.2 | 0.161.0+26.2 | `mc26_2` |
| `versions/26.1.properties` | 26.1, 26.1.1, 26.1.2 (built against 26.1.2) | 0.145.1+26.1 | `mc26_1` |

## How it works
- `versions/<mc>.properties` holds everything version-specific for the build: the Minecraft and Fabric API
  versions, the compat family, the `minecraft` range written into `fabric.mod.json`, the minimum Fabric API
  version, and the store `game_versions`.
- `./gradlew build` builds the primary version (`primary_mc` in `gradle.properties`).
  `./gradlew build -Pmc=26.1` builds another one. Jars are named `linkle_companion-<mod>+mc<target>.jar`.
- Almost all code is shared. The few calls that changed between versions sit behind two small classes with
  the same methods in every family:
  - `src/compat/<family>/java/.../compat/VersionCompat.java` (command permissions, advancement trigger)
  - `src/compat/<family>/client/java/.../client/compat/ClientCompat.java` (opening screens, action bar,
    HUD toggle, wide settings button, name-tag text)
- Data generation and the generated files (lang, recipe, advancements, tags) come from the primary version;
  every version ships the same generated files.
- CI (`.github/workflows/build.yml`) builds and runs the server game tests for every file in `versions/`.
  The release workflow does the same and attaches one jar per version to the GitHub release.

## Adding a Minecraft version
1. Add `versions/<mc>.properties` (copy the closest one; Fabric API versions are listed at
   <https://modrinth.com/mod/fabric-api/versions>).
2. `./gradlew build -Pmc=<mc>`. If it compiles and the game tests pass, you're done.
3. If some call changed, add a new compat family (copy the closest `src/compat/<family>`), change only what
   differs, and point the properties file at it. Keep the method names identical.
4. Run the client play-tests once: `./gradlew runClientGameTest -Pmc=<mc>`.
5. Add the version to the tables in this file, the README and the store texts.

## Not supported
Minecraft 1.21.x and older are obfuscated (they need Loom's remapping plugin and Mojang mappings) and differ
much more: a trial compile against 1.21.11 gave 39 errors in the shared code before even reaching the client
code. Supporting them would be a separate port, not a properties file.
