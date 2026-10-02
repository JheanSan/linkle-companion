# Releasing (step by step)

Nothing has been pushed or published yet. These are the steps for you to do it.

## 0. Before the first release
1. Do the manual checks in `docs/TESTING.md` (at least the "Before release" section).
2. Decide your GitHub repository URL, e.g. `https://github.com/<you>/linkle-companion`.
3. Add it to `src/main/resources/fabric.mod.json` (after `"license"`):
   ```json
   "contact": {
     "homepage": "https://modrinth.com/mod/linkle-companion",
     "sources": "https://github.com/<you>/linkle-companion",
     "issues": "https://github.com/<you>/linkle-companion/issues"
   },
   ```
   and replace "(add your GitHub link here)" in `docs/MODRINTH.md`.
4. Optional: rename the Java package `dev.linklecompanion` to your own (e.g. `io.github.<you>.linkle`).
5. Run `./gradlew build` and commit.

## 1. Push to GitHub
1. Create an empty repository on GitHub (no README, no license: the repo already has them).
2. In this folder:
   ```
   git remote add origin https://github.com/<you>/linkle-companion.git
   git push -u origin main
   ```
3. Open the **Actions** tab: the "build" workflow should go green (it builds and runs the game tests).

## 2. Make a release on GitHub
1. Make sure `mod_version` in `gradle.properties` is right (e.g. `1.0.0`) and `CHANGELOG.md` is updated.
2. Tag and push:
   ```
   git tag v1.0.0
   git push origin v1.0.0
   ```
3. The "release" workflow builds the jar and creates a GitHub release with
   `linkle_companion-1.0.0.jar` attached.

## 3. Publish on Modrinth
1. Sign in at <https://modrinth.com>, click **Create a project**, type **Mod**, name **Linkle Companion**,
   URL `linkle-companion`.
2. Fill the page from `docs/MODRINTH.md` (summary, description, categories, license MIT, links).
3. Upload the icon: `src/main/resources/assets/linkle_companion/icon.png`.
4. Add gallery images (shot list in `docs/MODRINTH.md`).
5. **Versions** > **Create a version**:
   - File: `build/libs/linkle_companion-1.0.0.jar` (not the `-sources` jar)
   - Version number: `1.0.0`, name: `Linkle Companion 1.0.0`, channel: Release
   - Loaders: Fabric. Game versions: 26.3
   - Dependencies: add **Fabric API** as **Required**
   - Environment: client required, server required
   - Changelog: copy from `CHANGELOG.md`
6. Submit the project for review. Modrinth reviews new projects before they go public.

## 4. Later versions
1. Update `mod_version`, `CHANGELOG.md`; build and test.
2. Commit, tag `vX.Y.Z`, push the tag.
3. Upload the new jar as a new version on Modrinth.
4. For a new Minecraft version: update the versions in `gradle.properties` (check
   <https://fabricmc.net/develop>), the `depends` block in `fabric.mod.json`, fix compile errors,
   run all tests, and release.
