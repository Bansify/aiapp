package com.bansify.ai.data.ai

/**
 * Wire format used by a provider. Only two shapes are needed today:
 * Google's native generateContent API and the OpenAI-compatible chat completions API.
 */
enum class Wire { GEMINI, OPENAI, GATEWAY }

data class AiModel(
    val id: String,
    val label: String,
    val context: String,
    val note: String,
    val free: Boolean = true
)

data class AiProvider(
    val id: String,
    val name: String,
    val monogram: String,
    val wire: Wire,
    /** Base URL without a trailing slash. `{account_id}` is replaced for Cloudflare. */
    val baseUrl: String,
    val keyLabel: String,
    val keyUrl: String,
    val freeTier: String,
    val models: List<AiModel>,
    /** False for the Bansify Cloud gateway, which holds the provider keys itself. */
    val requiresKey: Boolean = true
) {
    fun endpoint(model: String, accountId: String? = null): String {
        val base = baseUrl.replace("{account_id}", accountId.orEmpty())
        return when (wire) {
            Wire.GEMINI -> "$base/models/$model:streamGenerateContent?alt=sse"
            Wire.OPENAI -> "$base/chat/completions"
            Wire.GATEWAY -> "$base/v1/chat"
        }
    }
}

/**
 * Card-free provider catalogue.
 *
 * Model IDs were checked against the providers' own documentation on 22 November 2026.
 * Free tiers change without notice, so this list is the single place to update them.
 */
object AiProviders {

    val gemini = AiProvider(
        id = "google_gemini",
        name = "Google Gemini",
        monogram = "G",
        wire = Wire.GEMINI,
        baseUrl = "https://generativelanguage.googleapis.com/v1beta",
        keyLabel = "Gemini API key",
        keyUrl = "https://aistudio.google.com/apikey",
        freeTier = "Free tier, no card. Limits are per project and shown in AI Studio.",
        models = listOf(
            AiModel("gemini-3.5-flash-lite", "Gemini 3.5 Flash-Lite", "1M", "Fastest free Gemini model."),
            AiModel("gemini-3.8-flash", "Gemini 3.8 Flash", "1M", "Strong reasoning, still free tier."),
            AiModel("gemini-3.1-flash-lite", "Gemini 3.1 Flash-Lite", "1M", "Older lightweight model.")
        )
    )

    val groq = AiProvider(
        id = "groq",
        name = "Groq",
        monogram = "Gq",
        wire = Wire.OPENAI,
        baseUrl = "https://api.groq.com/openai/v1",
        keyLabel = "Groq API key",
        keyUrl = "https://console.groq.com/keys",
        freeTier = "Free plan: 30 requests/min, 1,000 requests/day per model.",
        models = listOf(
            AiModel("openai/gpt-oss-120b", "GPT-OSS 120B", "131K", "Free-plan flagship, ~500 tokens/sec."),
            AiModel("openai/gpt-oss-20b", "GPT-OSS 20B", "131K", "Fastest free-plan model."),
            AiModel("qwen/qwen3.8-27b", "Qwen3.8 27B", "131K", "Coding and math on the free plan.")
        )
    )

    val openRouter = AiProvider(
        id = "openrouter",
        name = "OpenRouter",
        monogram = "OR",
        wire = Wire.OPENAI,
        baseUrl = "https://openrouter.ai/api/v1",
        keyLabel = "OpenRouter API key",
        keyUrl = "https://openrouter.ai/keys",
        freeTier = "Free models: 20 requests/min, 50/day (1,000/day after 10 credits bought).",
        models = listOf(
            AiModel("openrouter/free", "Auto free router", "varies", "OpenRouter picks a free model for you."),
            AiModel("google/gemma-4-31b-it:free", "Gemma 4 31B (free)", "262K", "General chat with image input."),
            AiModel("nvidia/nemotron-3-ultra-550b-a55b:free", "Nemotron 3 Ultra (free)", "1M", "Largest free model on the router.")
        )
    )

    val cloudflare = AiProvider(
        id = "cloudflare",
        name = "Cloudflare Workers AI",
        monogram = "CF",
        wire = Wire.OPENAI,
        baseUrl = "https://api.cloudflare.com/client/v4/accounts/{account_id}/ai/v1",
        keyLabel = "Account ID:API token",
        keyUrl = "https://dash.cloudflare.com/profile/api-tokens",
        freeTier = "10,000 Neurons per day free, no card. Paste ACCOUNT_ID:API_TOKEN.",
        models = listOf(
            AiModel("@cf/openai/gpt-oss-120b", "GPT-OSS 120B", "128K", "Open-weight reasoning model on the edge."),
            AiModel("@cf/meta/llama-3.1-8b-instruct-fp8-fast", "Llama 3.1 8B Fast", "128K", "Cheapest Neurons, good for short replies.")
        )
    )

    val zai = AiProvider(
        id = "zai_glm",
        name = "Z.ai (GLM)",
        monogram = "Z",
        wire = Wire.OPENAI,
        baseUrl = "https://api.z.ai/api/paas/v4",
        keyLabel = "Z.ai API key",
        keyUrl = "https://z.ai/manage-apikey/apikey-list",
        freeTier = "Flash models are free on Z.ai. Limits are shown in the console.",
        models = listOf(
            AiModel("glm-5.3-flash", "GLM-5.3-Flash", "128K", "Fast multimodal Flash model."),
            AiModel("glm-4.7-flash", "GLM-4.7-Flash", "128K", "Older free Flash model.")
        )
    )

    val mistral = AiProvider(
        id = "mistral",
        name = "Mistral",
        monogram = "Mi",
        wire = Wire.OPENAI,
        baseUrl = "https://api.mistral.ai/v1",
        keyLabel = "Mistral API key",
        keyUrl = "https://console.mistral.ai/api-keys",
        freeTier = "Free experiment tier. Check your console for current limits.",
        models = listOf(
            AiModel("mistral-small-latest", "Mistral Small", "128K", "Concise, structured answers."),
            AiModel("codestral-latest", "Codestral", "256K", "Coding specialist.")
        )
    )

    /**
     * The Bansify Cloud gateway: your own Cloudflare Worker (`bansifyai`) that holds the
     * provider keys, ranks free providers, caches answers and enforces a daily quota.
     * It needs no key in the app, so it comes first in Auto routing.
     */
    val bansifyCloud = AiProvider(
        id = "bansify_cloud",
        name = "Bansify Cloud (your worker)",
        monogram = "B",
        wire = Wire.GATEWAY,
        baseUrl = "https://bansifyai.bansify.workers.dev",
        keyLabel = "No key needed",
        keyUrl = "https://bansifyai.bansify.workers.dev/v1/health",
        freeTier = "Your Worker routes to Gemini, Groq or OpenRouter with its own secrets.",
        models = listOf(
            AiModel("auto", "Auto (worker routing)", "varies", "The Worker picks a healthy free provider.")
        ),
        requiresKey = false
    )

    /** Order matters: Auto routing tries these from top to bottom. */
    val all: List<AiProvider> = listOf(bansifyCloud, gemini, groq, openRouter, cloudflare, zai, mistral)

    fun byId(id: String): AiProvider? = all.firstOrNull { it.id == id }

    fun modelKey(providerId: String, modelId: String): String = "$providerId::$modelId"

    fun parseModelKey(key: String): Pair<String, String>? {
        val index = key.indexOf("::")
        if (index <= 0) return null
        return key.substring(0, index) to key.substring(index + 2)
    }
}
