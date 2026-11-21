"""Check local Markdown file links and required documentation sections.

Adapted from Bansify/mailapp's `tools/check_docs.py` (itself mirroring
GothwadTech/accountsapp) for the single-module Bansify AI app. The app is a UI
prototype whose data layer is mocked, so there is no endpoint contract to keep
in sync yet — instead the docs must always describe the build, the release
secrets the workflow needs, and the on-device checks.
"""
from pathlib import Path
import re
import sys

root = Path(__file__).resolve().parents[1]
errors: list[str] = []

required_dirs = ("app", "gradle", ".github/workflows")
for folder in required_dirs:
    if not (root / folder).is_dir():
        errors.append(f"Missing scaffold directory: {folder}")

required_files = (
    "README.md",
    ".github/workflows/android-ci.yml",
    ".github/workflows/planning.yml",
    ".github/workflows/release.yml",
    "app/build.gradle.kts",
    "app/src/main/AndroidManifest.xml",
    "gradle/libs.versions.toml",
    "gradle/wrapper/gradle-wrapper.jar",
    "gradlew",
)
for name in required_files:
    if not (root / name).exists():
        errors.append(f"Missing required file: {name}")

readme = root / "README.md"
if not readme.is_file():
    errors.append("README.md is missing")
else:
    text = readme.read_text(encoding="utf-8")

    for file in [readme, *sorted(root.glob("docs/*.md"))]:
        body = file.read_text(encoding="utf-8")
        for target in re.findall(r"\[[^\]]*\]\(([^)]+)\)", body):
            if "://" in target or target.startswith("#") or target.startswith("mailto:"):
                continue
            if not (file.parent / target.split("#", 1)[0]).exists():
                errors.append(f"{file.relative_to(root)}: broken link {target}")

    required_sections = (
        "## Project structure",
        "## Build and test",
        "## Release secrets",
        "## Manual device checks",
    )
    for section in required_sections:
        if section not in text:
            errors.append(f"README.md: missing section {section!r}")

    # The release workflow cannot sign anything without these four repository secrets, so the
    # README has to keep naming them exactly.
    for secret in (
        "RELEASE_KEYSTORE_BASE64",
        "KEYSTORE_PASSWORD",
        "KEY_ALIAS",
        "KEY_PASSWORD",
    ):
        if secret not in text:
            errors.append(f"README.md: release secret {secret} is undocumented")

    # The app must keep telling readers (and contributors) that there is no live backend yet.
    if "mock" not in text.lower():
        errors.append("README.md: must state that app data is mocked")

if errors:
    print("\n".join(errors), file=sys.stderr)
    raise SystemExit(1)
print("Documentation links, required files and README sections OK (not an Android build).")
