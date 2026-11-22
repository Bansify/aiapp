# Bansify AI

Bansify AI — a calm, roomy Android AI chat and developer Studio app for the Gothwad
ecosystem. Kotlin + Jetpack Compose (Material 3).

Originally prototyped in Google AI Studio. The chat side is now **live**: replies stream
from a real provider, and sign-in uses the same Gothwad account as Mail, Store and Drive.
Studio and the workspace screens are still a **mock** prototype (`data/mock/`), and so are
image/video generation, export and sharing.

See [PLAN.md](PLAN.md) for the provider list, free tiers and the remaining gaps.

## Screens & features

- **Sign-in** — native Gothwad account sign-in, sign-up, authenticator 2FA, forgot password,
  or continue as guest
- **Accounts** — up to six signed-in accounts, switch, add another, manage in the browser,
  sign out of one or all
- **Onboarding** — welcome, name, profession, goals
- **Chat** — live streaming replies, markdown-lite rendering, model picker sheet, per-message
  regenerate and feedback, history saved on the device
- **History** — past conversations, pinned and searchable
- **Models** — provider catalogue with free-tier notes and key status
- **AI providers & keys** — Bansify Cloud is built in; add your own free key for Gemini,
  Groq, OpenRouter, Cloudflare Workers AI, Z.ai or Mistral
- **Studio** — repo browsing, file viewer with code/preview panes, integrations sheet, zip
  upload (mock data)
- **Workspace** — library, scheduled items (mock data)
- **Drawer** — navigation drawer
- **Settings** — appearance, memory, persona, data, about
- **Splash**

## How chat works

`POST /v1/chat` on the Bansify Cloud Worker (`https://bansifyai.bansify.workers.dev`) with
`{messages:[{role,content}], model:"auto"}`; the Worker ranks a free provider, streams
`{"delta": ...}` SSE frames and enforces a daily quota. The app needs no key for that route.
Adding your own provider key in Settings bypasses the Worker quota and calls the provider
directly. Keys are stored on the device only.

## Project structure

```
app/src/main/java/com/bansify/ai/
├── MainActivity.kt
├── core/            # design system (colors, type, shapes, theme, components), shared UI, HTTP
├── data/            # ai providers, auth, chat store, mock data, repositories
├── feature/         # auth, account, chat, history, models, onboarding, profile,
│                    # settings, splash, studio, workspace, drawer
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

There are no automated UI tests yet, so before shipping a build verify on an
emulator/device:

1. Cold start → sign-in screen → sign in with a Gothwad account (or continue as guest).
2. Onboarding (name, profession, goals) → chat.
3. Send a message and confirm a real streamed reply, then Stop mid-stream.
4. Kill and reopen the app: the conversation is still in History.
5. Settings → AI providers & keys: add a key, remove it, and check that Auto still routes.
6. Accounts: add a second account, switch, sign out one, sign out all.
7. Models sheet, Studio repo/file screens and the Settings screens render.

## Notes

- `metadata.json` describes the AI Studio prototype this was exported from.
- `public/assets/` holds reference screenshots from the design phase.
- Sign-in uses the first-party client `gothwad-accounts-android` against
  `https://accounts.gothwadtech.com/api/mobile/auth/*`; no Worker or D1 change is required.
- API keys are stored in DataStore, not EncryptedSharedPreferences or the Keystore. Move
  them before a production release.
