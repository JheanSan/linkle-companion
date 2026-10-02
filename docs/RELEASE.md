# Releasing Linkle Companion (GitHub, Modrinth, CurseForge)

Everything is prepared; nothing has been uploaded yet. Run this first to collect all upload files in one folder:

```
python scripts/release/make_release_kit.py
```

It builds the mod, runs the game tests, and fills `dist/linkle_companion-1.0.0+mc26.3/` with:
the jar to upload, the sources jar, the 512x512 icon, the gallery images, the page texts for Modrinth and
CurseForge, and the changelog.

---

## 1. GitHub (do this first: both store pages link to it)
1. Go to <https://github.com/new>.
   - Owner: **JheanSan**, Repository name: **linkle-companion**
   - Description: `Linkle, a dual-crossbow companion mod for Minecraft (Fabric 26.3). Unofficial fan project.`
   - **Public**. Do NOT add a README, .gitignore or license (the project already has them).
   - Click **Create repository**.
2. In this folder, run:
   ```
   git remote add origin https://github.com/JheanSan/linkle-companion.git
   git push -u origin main
   ```
   (Git will ask you to sign in to GitHub the first time.)
3. On the repo page, open the **Actions** tab: the "build" workflow should turn green in a few minutes
   (it compiles the mod and runs the game tests).
4. Optional polish on the repo page: click the gear next to **About** and add the website
   `https://modrinth.com/mod/linkle-companion` and topics: `minecraft`, `fabric`, `minecraft-mod`, `companion`.
   Under **Settings > General > Social preview** upload `docs/images/front.png`.

## 2. Modrinth
1. Sign in at <https://modrinth.com>, click **+** > **Create a project**.
   Name **Linkle Companion**, URL **linkle-companion**, visibility **Public**, summary from `docs/MODRINTH.md`.
2. Fill the page from `docs/MODRINTH.md`: description, icon (`docs/branding/icon-512.png`), categories,
   environment (client + server required), license **MIT**, links (source + issues).
3. **Gallery**: upload the images from `docs/images/` and mark `front.png` as featured.
4. **Versions > Create a version**: upload `linkle_companion-1.0.0+mc26.3.jar` (not `-sources`), version
   `1.0.0+mc26.3`, channel Release, loader Fabric, game version 26.3, dependency **Fabric API (required)**,
   optional **Mod Menu**, paste the changelog.
5. **Submit for review**. Modrinth checks new projects by hand (usually within a few days). You'll get a
   notification when it's approved.

## 3. CurseForge
1. Sign in at <https://authors.curseforge.com>, **Create Project** > Minecraft > **Mods**.
2. Fill it from `docs/CURSEFORGE.md`: name, summary, description (switch the editor to Markdown), categories,
   license MIT, avatar `docs/branding/icon-512.png`, source and issues links.
3. **Upload file**: `linkle_companion-1.0.0+mc26.3.jar`, release type Release, game version **26.3**,
   loader **Fabric**, Java 25, related projects: **Fabric API = Required**, **Mod Menu = Optional**,
   paste the changelog.
4. Add the gallery images from `docs/images/`.
5. Submit. CurseForge reviews new projects (a few hours to 2 days).

---

## Later releases: one command does all three
After the first manual upload, the "release" GitHub workflow can publish new versions everywhere:

1. One-time setup in the GitHub repo, **Settings > Secrets and variables > Actions**:
   - **Variables** tab: `MODRINTH_ID` = the project id shown on the Modrinth project page (Settings > General,
     "Project ID"), `CURSEFORGE_ID` = the number shown on the CurseForge project page ("Project ID").
   - **Secrets** tab: `MODRINTH_TOKEN` = a personal access token from modrinth.com > Settings > PATs with
     "Create versions"; `CURSEFORGE_TOKEN` = an API token from curseforge.com > My API Tokens.
2. For each new version:
   - Raise `mod_version` in `gradle.properties` (e.g. `1.0.1`) and add a section to `CHANGELOG.md`.
   - Run `./gradlew build` and the manual checks you care about from `docs/TESTING.md`.
   - Commit, then:
     ```
     git tag v1.0.1
     git push origin main --tags
     ```
   - The workflow builds, tests, and publishes the jar to GitHub Releases, Modrinth and CurseForge.
     A platform without its token is simply skipped.

## New Minecraft version
Update the versions in `gradle.properties` (check <https://fabricmc.net/develop>), the `depends` block and the
`game-versions` in `.github/workflows/release.yml`, fix any compile errors, run all tests, then release as above.
