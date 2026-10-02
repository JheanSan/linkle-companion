#!/usr/bin/env python3
"""
Builds the mod (with game tests) and gathers everything needed for a release upload into
dist/linkle_companion-<version>/ :

    python scripts/release/make_release_kit.py

Nothing is uploaded; see docs/RELEASE.md for the steps.
"""
import os
import re
import shutil
import subprocess
import sys

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))


def prop(name):
    with open(os.path.join(ROOT, "gradle.properties"), encoding="utf-8") as f:
        for line in f:
            if line.startswith(name + "="):
                return line.split("=", 1)[1].strip()
    sys.exit("missing " + name + " in gradle.properties")


def main():
    version = f"{prop('mod_version')}+mc{prop('minecraft_version')}"
    gradlew = os.path.join(ROOT, "gradlew.bat" if os.name == "nt" else "gradlew")
    print("Building and testing", version, "...")
    subprocess.run([gradlew, "build", "--no-configuration-cache", "-q"], cwd=ROOT, check=True)

    jar = os.path.join(ROOT, "build", "libs", f"linkle_companion-{version}.jar")
    sources = os.path.join(ROOT, "build", "libs", f"linkle_companion-{version}-sources.jar")
    if not os.path.exists(jar):
        sys.exit("jar not found: " + jar)

    out = os.path.join(ROOT, "dist", f"linkle_companion-{version}")
    if os.path.exists(out):
        shutil.rmtree(out)
    os.makedirs(os.path.join(out, "gallery"))

    shutil.copy2(jar, out)
    if os.path.exists(sources):
        os.makedirs(os.path.join(out, "extra"), exist_ok=True)
        shutil.copy2(sources, os.path.join(out, "extra"))
    shutil.copy2(os.path.join(ROOT, "docs", "branding", "icon-512.png"), out)
    for name in sorted(os.listdir(os.path.join(ROOT, "docs", "images"))):
        shutil.copy2(os.path.join(ROOT, "docs", "images", name), os.path.join(out, "gallery"))
    for doc in ("MODRINTH.md", "CURSEFORGE.md", "RELEASE.md"):
        shutil.copy2(os.path.join(ROOT, "docs", doc), out)

    # Only this version's changelog section, ready to paste.
    with open(os.path.join(ROOT, "CHANGELOG.md"), encoding="utf-8") as f:
        text = f.read()
    match = re.search(r"^## " + re.escape(prop("mod_version")) + r".*?(?=^## |\Z)", text, re.S | re.M)
    with open(os.path.join(out, "CHANGELOG-" + prop("mod_version") + ".md"), "w", encoding="utf-8") as f:
        f.write((match.group(0) if match else text).strip() + "\n")

    with open(os.path.join(out, "README.txt"), "w", encoding="utf-8") as f:
        f.write(f"""Linkle Companion {version} - upload kit

UPLOAD THIS FILE to Modrinth and CurseForge:  linkle_companion-{version}.jar
(extra/ holds the sources jar: optional, GitHub only)

icon-512.png       project icon (Modrinth icon, CurseForge avatar)
gallery/           screenshots for the gallery (front.png = featured)
MODRINTH.md        everything to paste on Modrinth
CURSEFORGE.md      everything to paste on CurseForge
CHANGELOG-*.md     this version's changelog
RELEASE.md         step-by-step guide (GitHub, then Modrinth, then CurseForge)
""")
    print("Release kit ready:", os.path.relpath(out, ROOT))
    for name in sorted(os.listdir(out)):
        print("  ", name)


if __name__ == "__main__":
    main()
