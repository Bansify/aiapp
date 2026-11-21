"""Static resource/config checks for Bansify AI; deliberately NOT a substitute for Gradle tests.

Adapted from Bansify/mailapp's `tools/check_android_scaffold.py` (mirroring
GothwadTech/accountsapp). Keeps the generic invariants — TOML validity, pinned
wrapper JAR, XML well-formedness, string resources, release-workflow contract —
and replaces the Mail-specific manifest invariants with Bansify AI's own
(single exported launcher activity, no auth/broker components yet).
"""
import hashlib
import re
import sys
from pathlib import Path
import xml.etree.ElementTree as ET

try:
    import tomllib
except ModuleNotFoundError:  # Python < 3.11
    tomllib = None

root = Path(__file__).resolve().parents[1]
errors: list[str] = []

ANDROID_NS = "{http://schemas.android.com/apk/res/android}"

# --- Version catalog and wrapper -------------------------------------------------

catalog_path = root / "gradle/libs.versions.toml"
if tomllib is not None:
    try:
        tomllib.loads(catalog_path.read_text(encoding="utf-8"))
    except Exception as error:  # noqa: BLE001 - any parse failure is a failure
        errors.append(f"gradle/libs.versions.toml is not valid TOML: {error}")

wrapper_jar = root / "gradle/wrapper/gradle-wrapper.jar"
if not wrapper_jar.is_file():
    errors.append("gradle/wrapper/gradle-wrapper.jar is missing")
else:
    # Pinned to the wrapper JAR shipped by Bansify/mailapp (Gradle 9.3.1).
    digest = hashlib.sha256(wrapper_jar.read_bytes()).hexdigest()
    if digest != "81a82aaea5abcc8ff68b3dfcb58b3c3c429378efd98e7433460610fecd7ae45f":
        errors.append(f"Unexpected Gradle wrapper JAR digest: {digest}")

props = root / "gradle/wrapper/gradle-wrapper.properties"
if props.is_file():
    text = props.read_text(encoding="utf-8")
    if "validateDistributionUrl=true" not in text:
        errors.append("gradle-wrapper.properties must keep validateDistributionUrl=true")
    if not re.search(r"distributionUrl=.*gradle-\d+\.\d+(\.\d+)?-bin\.zip", text):
        errors.append("gradle-wrapper.properties must point at a released Gradle distribution")
else:
    errors.append("gradle/wrapper/gradle-wrapper.properties is missing")

# --- XML well-formedness ---------------------------------------------------------

for path in sorted((root / "app/src").rglob("*.xml")):
    try:
        ET.parse(path)
    except ET.ParseError as error:
        errors.append(f"{path.relative_to(root)}: malformed XML: {error}")

# --- String resources ------------------------------------------------------------

def resource_names(path: Path) -> list[str]:
    tree = ET.parse(path)
    return [element.attrib["name"] for element in tree.getroot()]


strings_path = root / "app/src/main/res/values/strings.xml"
if strings_path.is_file():
    names = resource_names(strings_path)
    duplicates = sorted({name for name in names if names.count(name) > 1})
    if duplicates:
        errors.append(f"Duplicate string resources: {', '.join(duplicates)}")
    # Untranslated literals in Kotlin are fine, but a missing app_name breaks the launcher.
    if "app_name" not in names:
        errors.append("strings.xml is missing app_name")
else:
    errors.append("app/src/main/res/values/strings.xml is missing")

# --- Manifest invariants ---------------------------------------------------------

manifest_path = root / "app/src/main/AndroidManifest.xml"
if not manifest_path.is_file():
    errors.append("app/src/main/AndroidManifest.xml is missing")
else:
    manifest = ET.parse(manifest_path).getroot()
    application = manifest.find("application")
    if application is None:
        errors.append("AndroidManifest.xml has no <application>")
        application = ET.Element("application")

    # The app is a UI prototype with mocked data, so it intentionally uses no
    # permissions yet; when the real API layer lands, INTERNET belongs here.

    activities = {
        element.attrib.get(f"{ANDROID_NS}name"): element
        for element in application.findall("activity")
    }
    launcher = activities.get(".MainActivity")
    if launcher is None:
        errors.append("Missing .MainActivity launcher activity")
    else:
        if launcher.attrib.get(f"{ANDROID_NS}exported") != "true":
            errors.append("MainActivity must be exported (it is the launcher)")
        has_launcher_filter = any(
            intent_filter.find("action") is not None
            and intent_filter.find("action").attrib.get(f"{ANDROID_NS}name")
            == "android.intent.action.MAIN"
            for intent_filter in launcher.findall("intent-filter")
        )
        if not has_launcher_filter:
            errors.append("MainActivity must handle android.intent.action.MAIN")

# --- Build script contracts ------------------------------------------------------

build_script = root / "app/build.gradle.kts"
if not build_script.is_file():
    errors.append("app/build.gradle.kts is missing")
else:
    build_text = build_script.read_text(encoding="utf-8")
    checks = {
        'applicationId = "com.bansify.ai"': "applicationId must stay com.bansify.ai",
        "compileSdk = 36": "compileSdk must stay 36",
        "minSdk = 26": "minSdk must stay 26",
        "targetSdk = 35": "targetSdk must stay 35",
    }
    for needle, message in checks.items():
        if needle not in build_text:
            errors.append(f"app/build.gradle.kts: {message}")
    # Release builds are signed by the workflow, never by the debug key.
    if re.search(r"signingConfig\s*=\s*signingConfigs\.getByName\(\"debug\"\)", build_text):
        errors.append("Release builds must not be signed with the debug key")

# --- Release workflow contract ---------------------------------------------------

release_path = root / ".github/workflows/release.yml"
if not release_path.is_file():
    errors.append(".github/workflows/release.yml is missing")
else:
    release_text = release_path.read_text(encoding="utf-8")
    for secret in (
        "secrets.RELEASE_KEYSTORE_BASE64",
        "secrets.KEYSTORE_PASSWORD",
        "secrets.KEY_ALIAS",
        "secrets.KEY_PASSWORD",
    ):
        if secret not in release_text:
            errors.append(f"release.yml must consume {secret}")
    for task in (":app:assembleRelease", ":app:assembleDebug", ":app:bundleRelease"):
        if task not in release_text:
            errors.append(f"release.yml must build {task}")
    if "apksigner" not in release_text or "zipalign" not in release_text:
        errors.append("release.yml must zipalign and apksigner the release APK")
    # The release must be produced from every push, not only from main/tags.
    if not re.search(r"^on:\n  push:\s*$", release_text, re.MULTILINE):
        errors.append("release.yml must run on every push, not only on selected branches")
    for step in ("Publish versioned GitHub Release", "Publish rolling pre-release"):
        if step not in release_text:
            errors.append(f"release.yml is missing the '{step}' step")
    if "steps.signing.outputs.can_sign" not in release_text:
        errors.append("release.yml must degrade gracefully when the signing secrets are missing")

if errors:
    print("\n".join(errors), file=sys.stderr)
    raise SystemExit(1)
print("XML, resources, manifest, version catalog, wrapper and workflow checks OK; Android compilation not checked.")
