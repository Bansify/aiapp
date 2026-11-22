# Bansify AI — working plan

Bansify AI is the Android chat client for the Gothwad ecosystem. This file records what is
real in the app today, which free AI providers it supports, and what is still missing.

## What works now

- **Real chat**: messages stream from a provider you choose, token by token, and are saved
  to `chats.json` on the device, so history survives a restart.
- **Bring your own key**: one card-free API key per provider, stored on the device
  (DataStore). Keys are sent only to that provider.
- **Gothwad sign-in**: native JSON sign-in against the live Accounts Worker
  (`https://accounts.gothwadtech.com/api/mobile/auth/*`). Same account, sessions and
  sign-out as Mail, Store and Drive. Password sign-in, sign-up, authenticator 2FA and
  forgot-password are implemented.
- **Account control**: an account screen and sheet that list every signed-in account,
  switch between them, add another, open the browser account page, and sign out one or all.
- **Guest mode**: works without an account; chats stay local.

## Bansify Cloud (built in)

The `bansifyai` Worker (`https://bansifyai.bansify.workers.dev`) is already deployed and is the
default route. It holds the provider keys as Worker secrets, ranks free providers, caches
answers in KV, records daily usage in D1 and enforces a per-IP daily quota. The app calls it
with `POST /v1/chat` (`{messages:[{role,content}], model:"auto"}`) and reads `{"delta": ...}`
SSE frames, so **no key is needed in the app**. `GET /v1/health`, `/v1/models` and
`/v1/providers` are available for status and quota display.

## Free providers (all card-free)

| Provider | Wire format | Free tier | Notes |
|---|---|---|---|
| Google Gemini | native `generateContent` | per-project limits in AI Studio | Start here. `gemini-3.5-flash-lite` and `gemini-3.8-flash` are on the free tier. |
| Groq | OpenAI-compatible | 30 req/min, 1,000 req/day per model | `openai/gpt-oss-120b`, `openai/gpt-oss-20b`, `qwen/qwen3.8-27b`. |
| OpenRouter | OpenAI-compatible | 20 req/min, 50 req/day (1,000 after 10 credits) | Free IDs end in `:free`; `openrouter/free` picks one for you. |
| Cloudflare Workers AI | OpenAI-compatible | 10,000 Neurons/day | Paste `ACCOUNT_ID:API_TOKEN`. Good fit because the ecosystem is already on Cloudflare. |
| Z.ai (GLM) | OpenAI-compatible | Flash models free | `glm-5.3-flash`, `glm-4.7-flash`. |
| Mistral | OpenAI-compatible | experiment tier | Check the console for current limits. |

**Recommended setup:** use Bansify Cloud as-is. Add your own Gemini key only if you want to
bypass the Worker's daily quota. Auto mode tries Bansify Cloud first, then the first provider
that has a key, in the order above.

**Dropped from the old prototype catalogue:** GitHub Models was retired on 30 July 2026 and
its inference API no longer exists. Groq retired the Llama model IDs, so
`llama-3.3-70b-versatile` and friends now return 404. The prototype also listed Cohere,
NVIDIA NIM, Together, SambaNova, Cerebras and Hugging Face; those are not wired yet.

## Not built yet

- Image and video generation (the composer shows a clear message instead of a fake render).
- Studio (repos, preview, integrations) is still a local prototype with sample data.
- Markdown export, share links and public chat sharing.
- Voice input uses a placeholder transcript.
- Keys are stored in DataStore, not EncryptedSharedPreferences or the Keystore. Move them
  before a release build.

## Accounts integration notes

- The app signs in with the already-approved first-party client
  `gothwad-accounts-android` and the scope `profile email offline_access`. No Worker or D1
  change is needed for sign-in to work today.
- A dedicated `gothwad-ai` client (package `com.bansify.ai`, its own allowed scopes) is the
  cleaner long-term setup. That needs a migration and a Worker deploy in `GothwadTech/accounts`.
- The Accounts Worker does not check the Android package name at sign-in; it checks that the
  client is first-party and active. Package and certificate checks belong in a later
  attestation step, not in this app.

## Build and test

```bash
./gradlew assembleDebug
```

The sandbox used to prepare this change had no JDK, Android SDK or Gradle, so the project was
not compiled there. Build it locally and send back any compiler output.
