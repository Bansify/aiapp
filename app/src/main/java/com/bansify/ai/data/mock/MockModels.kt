package com.bansify.ai.data.mock

import com.bansify.ai.core.designsystem.HealthStatus

data class ModelItem(
    val id: String,
    val providerId: String,
    val name: String,
    val shortName: String,
    val isFree: Boolean,
    val contextLength: String,
    val speedRating: Int, // 1..3 dots
    val status: HealthStatus,
    val description: String,
    val bestFor: List<String>,
    val rateLimitNote: String
)

data class ProviderGroup(
    val id: String,
    val name: String,
    val monogram: String,
    val isCardFreeTier: Boolean,
    val setupNeeded: Boolean,
    val status: HealthStatus,
    val freeTierSummary: String,
    val quotaChips: List<String>,
    val resetHint: String?,
    val models: List<ModelItem>
)

object MockModels {
    val autoModel = ModelItem(
        id = "auto",
        providerId = "auto",
        name = "Auto (recommended)",
        shortName = "Auto ⚡",
        isFree = true,
        contextLength = "Smart routing",
        speedRating = 3,
        status = HealthStatus.UP,
        description = "Automatically picks the fastest healthy free model available.",
        bestFor = listOf("Daily chat", "Low latency", "Auto fallback"),
        rateLimitNote = "Automatically switches if a provider hits RPM limits."
    )

    val initialProviders: List<ProviderGroup> = listOf(
        // ─── 9 CARD-FREE PROVIDERS ───
        ProviderGroup(
            id = "google_gemini",
            name = "Google Gemini",
            monogram = "G",
            isCardFreeTier = true,
            setupNeeded = false,
            status = HealthStatus.UP,
            freeTierSummary = "Card-free · 1M ctx",
            quotaChips = listOf("30 RPM", "1,000/day"),
            resetHint = "Resets tomorrow",
            models = listOf(
                ModelItem(
                    id = "gemini-2.5-flash-lite",
                    providerId = "google_gemini",
                    name = "Gemini 2.5 Flash-Lite",
                    shortName = "Flash-Lite",
                    isFree = true,
                    contextLength = "1M",
                    speedRating = 3,
                    status = HealthStatus.UP,
                    description = "Ultra-fast everyday assistant with a huge 1M context window.",
                    bestFor = listOf("Quick Q&A", "Notes", "Long PDFs"),
                    rateLimitNote = "30 requests/min · 1,000 requests/day free tier"
                ),
                ModelItem(
                    id = "gemini-2.5-flash",
                    providerId = "google_gemini",
                    name = "Gemini 2.5 Flash",
                    shortName = "Gemini Flash",
                    isFree = true,
                    contextLength = "1M",
                    speedRating = 3,
                    status = HealthStatus.UP,
                    description = "Balanced reasoning and coding speed with strong multilingual fluency.",
                    bestFor = listOf("Coding", "Study", "Reasoning"),
                    rateLimitNote = "15 requests/min · 500 requests/day free tier"
                ),
                ModelItem(
                    id = "gemini-2.5-pro",
                    providerId = "google_gemini",
                    name = "Gemini 2.5 Pro",
                    shortName = "Gemini Pro",
                    isFree = true,
                    contextLength = "2M",
                    speedRating = 2,
                    status = HealthStatus.UP,
                    description = "Deep problem solver for architecture, math proofs, and large codebases.",
                    bestFor = listOf("Complex math", "Architecture", "Repo review"),
                    rateLimitNote = "5 requests/min · 100 requests/day free tier"
                )
            )
        ),
        ProviderGroup(
            id = "groq",
            name = "Groq",
            monogram = "Gq",
            isCardFreeTier = true,
            setupNeeded = false,
            status = HealthStatus.UP,
            freeTierSummary = "LPU instant speed",
            quotaChips = listOf("30 RPM", "14,400/day"),
            resetHint = "Resets tomorrow",
            models = listOf(
                ModelItem(
                    id = "groq-llama-3.3-70b",
                    providerId = "groq",
                    name = "Llama 3.3 70B",
                    shortName = "Llama 70B",
                    isFree = true,
                    contextLength = "128K",
                    speedRating = 3,
                    status = HealthStatus.UP,
                    description = "Blazing-fast open weights model on Groq LPU hardware.",
                    bestFor = listOf("Fast chat", "Drafting", "Summaries"),
                    rateLimitNote = "30 RPM · 12K tokens/min on free tier"
                ),
                ModelItem(
                    id = "groq-qwen3-32b",
                    providerId = "groq",
                    name = "Qwen3 32B",
                    shortName = "Qwen3 32B",
                    isFree = true,
                    contextLength = "131K",
                    speedRating = 3,
                    status = HealthStatus.UP,
                    description = "Sharp coding and math reasoning in a compact 32B footprint.",
                    bestFor = listOf("DSA", "Coding", "Step-by-step"),
                    rateLimitNote = "60 RPM · 6K tokens/min on free tier"
                ),
                ModelItem(
                    id = "groq-gpt-oss-120b",
                    providerId = "groq",
                    name = "gpt-oss-120b",
                    shortName = "gpt-oss-120b",
                    isFree = true,
                    contextLength = "131K",
                    speedRating = 2,
                    status = HealthStatus.SLOW,
                    description = "High-capacity open reasoning model experiencing elevated queue latency.",
                    bestFor = listOf("Deep reasoning", "Code generation"),
                    rateLimitNote = "Currently slow (~4.2s TTFT) · 20 RPM"
                )
            )
        ),
        ProviderGroup(
            id = "openrouter",
            name = "OpenRouter",
            monogram = "OR",
            isCardFreeTier = true,
            setupNeeded = false,
            status = HealthStatus.UP,
            freeTierSummary = "Free :free routes",
            quotaChips = listOf("20 RPM", "50/day"),
            resetHint = "Resets tomorrow",
            models = listOf(
                ModelItem(
                    id = "or-deepseek-r1",
                    providerId = "openrouter",
                    name = "DeepSeek R1 (free)",
                    shortName = "DeepSeek R1",
                    isFree = true,
                    contextLength = "128K",
                    speedRating = 2,
                    status = HealthStatus.UP,
                    description = "Chain-of-thought reasoning model routed via free community pools.",
                    bestFor = listOf("Math", "Logic", "Debugging"),
                    rateLimitNote = "20 RPM · 50 free requests/day"
                ),
                ModelItem(
                    id = "or-llama-4-maverick",
                    providerId = "openrouter",
                    name = "Llama 4 Maverick (free)",
                    shortName = "Llama 4",
                    isFree = true,
                    contextLength = "256K",
                    speedRating = 2,
                    status = HealthStatus.UP,
                    description = "Large MoE model with strong creative writing and broad knowledge.",
                    bestFor = listOf("Creative", "General chat"),
                    rateLimitNote = "20 RPM · shared free pool"
                )
            )
        ),
        ProviderGroup(
            id = "cloudflare",
            name = "Cloudflare Workers AI",
            monogram = "CF",
            isCardFreeTier = true,
            setupNeeded = false,
            status = HealthStatus.UP,
            freeTierSummary = "Edge serverless",
            quotaChips = listOf("10K neurons/day"),
            resetHint = "Resets tomorrow",
            models = listOf(
                ModelItem(
                    id = "cf-llama-3.1-8b",
                    providerId = "cloudflare",
                    name = "Llama 3.1 8B Fast",
                    shortName = "Llama 8B",
                    isFree = true,
                    contextLength = "128K",
                    speedRating = 3,
                    status = HealthStatus.UP,
                    description = "Low-latency edge inference close to global PoPs.",
                    bestFor = listOf("Quick notes", "Rewriting"),
                    rateLimitNote = "10,000 free neurons/day"
                ),
                ModelItem(
                    id = "cf-qwen-coder-32b",
                    providerId = "cloudflare",
                    name = "Qwen 2.5 Coder 32B",
                    shortName = "Qwen Coder",
                    isFree = true,
                    contextLength = "32K",
                    speedRating = 2,
                    status = HealthStatus.UP,
                    description = "Dedicated code assistant tuned for Kotlin, TypeScript, and Python.",
                    bestFor = listOf("Code completion", "Refactoring"),
                    rateLimitNote = "10,000 free neurons/day"
                )
            )
        ),
        ProviderGroup(
            id = "mistral",
            name = "Mistral",
            monogram = "Mi",
            isCardFreeTier = true,
            setupNeeded = false,
            status = HealthStatus.UP,
            freeTierSummary = "Experiment free",
            quotaChips = listOf("1 RPS", "1B tok/mo"),
            resetHint = "Monthly reset",
            models = listOf(
                ModelItem(
                    id = "mistral-small-3.1",
                    providerId = "mistral",
                    name = "Mistral Small 3.1",
                    shortName = "Mistral Small",
                    isFree = true,
                    contextLength = "128K",
                    speedRating = 3,
                    status = HealthStatus.UP,
                    description = "Clean, crisp model great for concise structured responses.",
                    bestFor = listOf("Structured output", "Summaries"),
                    rateLimitNote = "1 req/sec · 1B tokens/month free tier"
                ),
                ModelItem(
                    id = "mistral-codestral",
                    providerId = "mistral",
                    name = "Codestral 25.01",
                    shortName = "Codestral",
                    isFree = true,
                    contextLength = "256K",
                    speedRating = 2,
                    status = HealthStatus.DOWN,
                    description = "Specialized coding model currently undergoing mock maintenance.",
                    bestFor = listOf("Fill-in-middle", "Unit tests"),
                    rateLimitNote = "Endpoint unreachable (mock down state)"
                )
            )
        ),
        ProviderGroup(
            id = "cohere",
            name = "Cohere",
            monogram = "Co",
            isCardFreeTier = true,
            setupNeeded = false,
            status = HealthStatus.UP,
            freeTierSummary = "Trial key free",
            quotaChips = listOf("20 RPM", "1,000/mo"),
            resetHint = "Monthly reset",
            models = listOf(
                ModelItem(
                    id = "cohere-command-a",
                    providerId = "cohere",
                    name = "Command A",
                    shortName = "Command A",
                    isFree = true,
                    contextLength = "256K",
                    speedRating = 2,
                    status = HealthStatus.UP,
                    description = "Enterprise RAG and grounded citation specialist.",
                    bestFor = listOf("Citations", "Document Q&A"),
                    rateLimitNote = "20 RPM · 1,000 trial calls/month"
                ),
                ModelItem(
                    id = "cohere-command-r7b",
                    providerId = "cohere",
                    name = "Command R7B",
                    shortName = "Command R7B",
                    isFree = true,
                    contextLength = "128K",
                    speedRating = 3,
                    status = HealthStatus.UP,
                    description = "Lightweight multilingual model for crisp everyday answers.",
                    bestFor = listOf("Fast lookup", "Multilingual"),
                    rateLimitNote = "20 RPM · trial tier"
                )
            )
        ),
        ProviderGroup(
            id = "nvidia_nim",
            name = "NVIDIA NIM",
            monogram = "NV",
            isCardFreeTier = true,
            setupNeeded = false,
            status = HealthStatus.SLOW,
            freeTierSummary = "Free dev credits",
            quotaChips = listOf("40 RPM", "1,000 credits"),
            resetHint = null,
            models = listOf(
                ModelItem(
                    id = "nim-nemotron-253b",
                    providerId = "nvidia_nim",
                    name = "Nemotron Ultra 253B",
                    shortName = "Nemotron",
                    isFree = true,
                    contextLength = "128K",
                    speedRating = 1,
                    status = HealthStatus.SLOW,
                    description = "Heavyweight reasoning model hosted on NVIDIA NIM clusters.",
                    bestFor = listOf("Science", "Hard math"),
                    rateLimitNote = "High load — response may take 5–8s"
                ),
                ModelItem(
                    id = "nim-deepseek-v3",
                    providerId = "nvidia_nim",
                    name = "DeepSeek V3 (NIM)",
                    shortName = "DeepSeek V3",
                    isFree = true,
                    contextLength = "128K",
                    speedRating = 2,
                    status = HealthStatus.UP,
                    description = "Balanced coding and multilingual fluency on NVIDIA hosted GPUs.",
                    bestFor = listOf("Full-stack code", "Writing"),
                    rateLimitNote = "40 RPM · Free developer pool"
                )
            )
        ),
        ProviderGroup(
            id = "github_models",
            name = "GitHub Models",
            monogram = "GH",
            isCardFreeTier = true,
            setupNeeded = false,
            status = HealthStatus.UP,
            freeTierSummary = "Free with GitHub",
            quotaChips = listOf("15 RPM", "150/day"),
            resetHint = "Resets tomorrow",
            models = listOf(
                ModelItem(
                    id = "gh-gpt-4o-mini",
                    providerId = "github_models",
                    name = "GPT-4o mini (GitHub)",
                    shortName = "4o mini",
                    isFree = true,
                    contextLength = "128K",
                    speedRating = 3,
                    status = HealthStatus.UP,
                    description = "Reliable compact model accessible free via GitHub PAT.",
                    bestFor = listOf("General chat", "Code snippets"),
                    rateLimitNote = "15 RPM · 150 requests/day"
                ),
                ModelItem(
                    id = "gh-phi-4-reasoning",
                    providerId = "github_models",
                    name = "Phi-4 Reasoning",
                    shortName = "Phi-4",
                    isFree = true,
                    contextLength = "128K",
                    speedRating = 2,
                    status = HealthStatus.DOWN,
                    description = "Compact STEM reasoning model temporarily paused on demo cluster.",
                    bestFor = listOf("Math", "Logic puzzles"),
                    rateLimitNote = "Temporarily unavailable (mock down state)"
                )
            )
        ),
        ProviderGroup(
            id = "zai_glm",
            name = "Z.ai (GLM)",
            monogram = "Z",
            isCardFreeTier = true,
            setupNeeded = false,
            status = HealthStatus.UP,
            freeTierSummary = "Generous Flash free",
            quotaChips = listOf("60 RPM", "Unlimited Flash"),
            resetHint = null,
            models = listOf(
                ModelItem(
                    id = "glm-4.7-flash",
                    providerId = "zai_glm",
                    name = "GLM-4.7-Flash",
                    shortName = "GLM-4.7",
                    isFree = true,
                    contextLength = "128K",
                    speedRating = 3,
                    status = HealthStatus.UP,
                    description = "Fast, generous free-tier model with solid coding & multilingual grasp.",
                    bestFor = listOf("Unlimited chat", "Coding"),
                    rateLimitNote = "60 RPM · Free flash tier"
                ),
                ModelItem(
                    id = "glm-4.5-air",
                    providerId = "zai_glm",
                    name = "GLM-4.5-Air",
                    shortName = "GLM-Air",
                    isFree = true,
                    contextLength = "128K",
                    speedRating = 3,
                    status = HealthStatus.UP,
                    description = "Light conversational model optimized for quick mobile replies.",
                    bestFor = listOf("Daily life", "Short answers"),
                    rateLimitNote = "60 RPM · Free tier"
                )
            )
        ),

        // ─── 6 PAID / CARD REQUIRED PROVIDERS ───
        ProviderGroup(
            id = "openai",
            name = "OpenAI",
            monogram = "OA",
            isCardFreeTier = false,
            setupNeeded = true,
            status = HealthStatus.UP,
            freeTierSummary = "Setup needed",
            quotaChips = listOf("Pay-as-you-go", "Tier 1"),
            resetHint = null,
            models = listOf(
                ModelItem(
                    id = "openai-gpt-4.1",
                    providerId = "openai",
                    name = "GPT-4.1",
                    shortName = "GPT-4.1",
                    isFree = false,
                    contextLength = "1M",
                    speedRating = 2,
                    status = HealthStatus.UP,
                    description = "Flagship instruction-following and long-context coding model.",
                    bestFor = listOf("Production code", "Instruction following"),
                    rateLimitNote = "Requires OpenAI API billing"
                ),
                ModelItem(
                    id = "openai-o4-mini",
                    providerId = "openai",
                    name = "o4-mini",
                    shortName = "o4-mini",
                    isFree = false,
                    contextLength = "200K",
                    speedRating = 2,
                    status = HealthStatus.UP,
                    description = "Fast reasoning model tuned for competitive math and debugging.",
                    bestFor = listOf("Math", "Algorithms"),
                    rateLimitNote = "Requires OpenAI API billing"
                )
            )
        ),
        ProviderGroup(
            id = "anthropic",
            name = "Anthropic",
            monogram = "An",
            isCardFreeTier = false,
            setupNeeded = true,
            status = HealthStatus.UP,
            freeTierSummary = "Setup needed",
            quotaChips = listOf("50 RPM", "Card required"),
            resetHint = null,
            models = listOf(
                ModelItem(
                    id = "claude-sonnet-4",
                    providerId = "anthropic",
                    name = "Claude Sonnet 4",
                    shortName = "Sonnet 4",
                    isFree = false,
                    contextLength = "200K",
                    speedRating = 2,
                    status = HealthStatus.UP,
                    description = "Nuanced writing and top-tier software engineering collaborator.",
                    bestFor = listOf("Refactors", "Long-form writing"),
                    rateLimitNote = "Requires Anthropic console credits"
                ),
                ModelItem(
                    id = "claude-haiku-3.5",
                    providerId = "anthropic",
                    name = "Claude Haiku 3.5",
                    shortName = "Haiku 3.5",
                    isFree = false,
                    contextLength = "200K",
                    speedRating = 3,
                    status = HealthStatus.UP,
                    description = "Compact and quick Claude model for snappy iterations.",
                    bestFor = listOf("Fast edits", "Summaries"),
                    rateLimitNote = "Requires Anthropic console credits"
                )
            )
        ),
        ProviderGroup(
            id = "together",
            name = "Together AI",
            monogram = "Tg",
            isCardFreeTier = false,
            setupNeeded = true,
            status = HealthStatus.UP,
            freeTierSummary = "Setup needed",
            quotaChips = listOf("60 RPM", "$5 min"),
            resetHint = null,
            models = listOf(
                ModelItem(
                    id = "together-r1-70b",
                    providerId = "together",
                    name = "DeepSeek R1 Distill 70B",
                    shortName = "R1 70B",
                    isFree = false,
                    contextLength = "128K",
                    speedRating = 3,
                    status = HealthStatus.UP,
                    description = "High-throughput distilled reasoning model on Together clusters.",
                    bestFor = listOf("Reasoning", "Batch evals"),
                    rateLimitNote = "Card required · pay per token"
                ),
                ModelItem(
                    id = "together-qwen3-235b",
                    providerId = "together",
                    name = "Qwen3 235B A22B",
                    shortName = "Qwen3 235B",
                    isFree = false,
                    contextLength = "128K",
                    speedRating = 2,
                    status = HealthStatus.DOWN,
                    description = "Frontier MoE model temporarily offline in this mock snapshot.",
                    bestFor = listOf("Research", "Complex code"),
                    rateLimitNote = "Endpoint maintenance (mock down state)"
                )
            )
        ),
        ProviderGroup(
            id = "cerebras",
            name = "Cerebras",
            monogram = "Cb",
            isCardFreeTier = false,
            setupNeeded = true,
            status = HealthStatus.UP,
            freeTierSummary = "Setup needed",
            quotaChips = listOf("30 RPM", "Wafer-scale"),
            resetHint = null,
            models = listOf(
                ModelItem(
                    id = "cerebras-llama-3.3-70b",
                    providerId = "cerebras",
                    name = "Llama 3.3 70B Instant",
                    shortName = "CB Llama 70B",
                    isFree = false,
                    contextLength = "128K",
                    speedRating = 3,
                    status = HealthStatus.UP,
                    description = "2,000+ tok/sec wafer-scale streaming speed.",
                    bestFor = listOf("Instant code", "Real-time voice"),
                    rateLimitNote = "Paid tier key required"
                ),
                ModelItem(
                    id = "cerebras-qwen3-32b",
                    providerId = "cerebras",
                    name = "Qwen3 32B Ultra",
                    shortName = "CB Qwen3",
                    isFree = false,
                    contextLength = "128K",
                    speedRating = 3,
                    status = HealthStatus.UP,
                    description = "Ultra-fast reasoning iterations for developer loops.",
                    bestFor = listOf("Fast debugging", "Autocomplete"),
                    rateLimitNote = "Paid tier key required"
                )
            )
        ),
        ProviderGroup(
            id = "sambanova",
            name = "SambaNova",
            monogram = "SN",
            isCardFreeTier = false,
            setupNeeded = true,
            status = HealthStatus.UP,
            freeTierSummary = "Setup needed",
            quotaChips = listOf("20 RPM", "Paid tier"),
            resetHint = null,
            models = listOf(
                ModelItem(
                    id = "sambanova-llama-405b",
                    providerId = "sambanova",
                    name = "Llama 3.1 405B Fast",
                    shortName = "Llama 405B",
                    isFree = false,
                    contextLength = "128K",
                    speedRating = 3,
                    status = HealthStatus.UP,
                    description = "Full-precision 405B open frontier model on RDU hardware.",
                    bestFor = listOf("Deep knowledge", "Synthesis"),
                    rateLimitNote = "SambaNova Cloud billing required"
                ),
                ModelItem(
                    id = "sambanova-deepseek-v3",
                    providerId = "sambanova",
                    name = "DeepSeek V3 Fast",
                    shortName = "SN DeepSeek",
                    isFree = false,
                    contextLength = "64K",
                    speedRating = 3,
                    status = HealthStatus.DOWN,
                    description = "High-speed DeepSeek deployment currently down in mock health check.",
                    bestFor = listOf("Code gen", "Reasoning"),
                    rateLimitNote = "Currently down (mock status)"
                )
            )
        ),
        ProviderGroup(
            id = "huggingface",
            name = "Hugging Face",
            monogram = "HF",
            isCardFreeTier = false,
            setupNeeded = true,
            status = HealthStatus.UP,
            freeTierSummary = "Setup needed",
            quotaChips = listOf("Pro $9/mo", "Serverless"),
            resetHint = "Monthly reset",
            models = listOf(
                ModelItem(
                    id = "hf-inference-router",
                    providerId = "huggingface",
                    name = "HF Inference Pro Router",
                    shortName = "HF Router",
                    isFree = false,
                    contextLength = "128K",
                    speedRating = 2,
                    status = HealthStatus.UP,
                    description = "Routes across open-source checkpoints with Hugging Face Pro token.",
                    bestFor = listOf("Open models", "Experiments"),
                    rateLimitNote = "Requires Hugging Face Pro subscription"
                ),
                ModelItem(
                    id = "hf-smollm3-3b",
                    providerId = "huggingface",
                    name = "SmolLM3 3B Instruct",
                    shortName = "SmolLM3",
                    isFree = false,
                    contextLength = "64K",
                    speedRating = 3,
                    status = HealthStatus.UP,
                    description = "Tiny open model for quick prompt testing and edge benchmarks.",
                    bestFor = listOf("Testing", "Lightweight tasks"),
                    rateLimitNote = "Pro token recommended for steady RPM"
                )
            )
        )
    )
}
