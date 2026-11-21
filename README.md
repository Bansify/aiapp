# Bansify AI

Bansify AI — a calm, roomy Android AI chat and developer Studio **UI prototype**,
with Hinglish-flavoured UX, a multi-provider model catalog, and mock streaming.
Built with Kotlin + Jetpack Compose (Material 3).

Originally prototyped in Google AI Studio, now maintained in this repository.

All data is **mock data** (`data/mock/`) — there is no backend wired up yet.

## Screens & features

- **Onboarding** — welcome, name, profession, goals
- **Chat** — composer with mock streaming, markdown-lite rendering, model picker sheet, add-to-chat sheet
- **History** — past conversations
- **Models** — multi-provider model catalog (grouped by provider)
- **Studio** — repo browsing, file viewer with code/preview panes, integrations sheet, zip upload
- **Workspace** — library, scheduled items
- **Drawer** — navigation drawer
- **Settings** — appearance, memory, persona, data, about
- **Splash**

## Project structure

```
app/src/main/java/com/bansify/ai/
├── MainActivity.kt
├── core/            # design system (colors, type, shapes, theme, components) + shared UI
├── data/            # mock data + repositories (Chat, Model, Studio, UserPreferences)
├── feature/         # chat, history, models, onboarding, profile, settings,
│                    # splash, studio, workspace, drawer
├── nav/             # NavGraph + routes
└── state/           # shared UI state
public/assets/       # design reference screenshots
.github/workflows/   # CI, planning checks and release pipeline
tools/               # static checks run by CI (docs + scaffold)
```

## Requirements

- Android Studio (Ladybug or newer) or Gradle 9.x with JDK 17+ (CI uses Temurin 21)
- Android SDK with platform **36** (compileSdk), minSdk 26
- The Gradle wrapper is committed (`./gradlew`), no local Gradle install needed

## Build and test

```bash
./gradlew :app:assembleDebug   # debug APK -> app/build/outputs/apk/debug/
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

The project intentionally has **no committed keystore** — debug builds use the
default `~/.android/debug.keystore`, and release builds are unsigned in-repo
(the release workflow signs them; see below).

## CI & releases

Three GitHub Actions workflows run on every push:

- **Android CI** — `tools/` checks, unit tests, lint and a debug APK (uploaded as an artifact).
- **Planning checks** — documentation/scaffold consistency.
- **Build Signed Release APK & Publish GitHub Release** — runs tests, builds the release
  APK, debug APK and Play Store AAB, signs them when the secrets below are set, and
  publishes to the Releases page: pushes to `main`/`master` and `v*` tags get a versioned
  release (`v1.0.<build>`); every other branch refreshes a single rolling pre-release
  tagged `latest`. Version code/name are injected via `-PversionCode`/`-PversionName`.

## Release secrets

The release workflow signs nothing without these four repository secrets
(Settings → Secrets and variables → Actions):

- `RELEASE_KEYSTORE_BASE64` — base64 of the release keystore (`base64 -w0 release.jks`)
- `KEYSTORE_PASSWORD` — keystore password
- `KEY_ALIAS` — key alias (auto-detected from the keystore if left empty)
- `KEY_PASSWORD` — key password (defaults to `KEYSTORE_PASSWORD` if empty)

If they are missing the build still succeeds and publishes the **debug APK and an
unsigned release APK/AAB** instead — it never falls back to the debug key.

## Manual device checks

There are no automated UI tests yet (UI prototype), so before shipping a build verify on
an emulator/device: cold start → onboarding flow (name, profession, goals) → drawer
navigation → new chat with mock streaming response → stop streaming → models sheet →
Studio repo/file screens render → Settings screens open.

## Notes

- `metadata.json` describes the AI Studio prototype this was exported from.
- `public/assets/` holds reference screenshots from the design phase.
