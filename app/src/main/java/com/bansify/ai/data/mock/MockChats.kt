package com.bansify.ai.data.mock

enum class MessageRole { USER, ASSISTANT }

enum class MessageFeedback { NONE, UP, DOWN }

enum class ChatMode { CHAT, IMAGE, VIDEO }

data class GeneratedMedia(
    val isVideo: Boolean,
    val prompt: String,
    val aspectRatio: String,
    val durationLabel: String? = null,
    val seedIndex: Int = 0
)

enum class TimeBucket(val label: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    LAST_7_DAYS("Last 7 days"),
    OLDER("Older")
}

data class ChatAttachment(
    val id: String,
    val label: String,
    val type: String // "Photo", "Camera", "File"
)

data class ChatMessage(
    val id: String,
    val role: MessageRole,
    val content: String,
    val timestampLabel: String,
    val dateGroup: String, // "Today", "Yesterday", "17 Nov"
    val modelName: String? = null,
    val isStreaming: Boolean = false,
    val feedback: MessageFeedback = MessageFeedback.NONE,
    val attachments: List<ChatAttachment> = emptyList(),
    val generatedMedia: GeneratedMedia? = null
)

data class Conversation(
    val id: String,
    val title: String,
    val preview: String,
    val relativeTime: String,
    val timeBucket: TimeBucket,
    val modelId: String,
    val modelLabel: String,
    val isPinned: Boolean = false,
    val isShared: Boolean = false,
    val mode: ChatMode = ChatMode.CHAT,
    val messages: List<ChatMessage>
)

object MockChats {
    val imageSuggestions: List<SuggestionCardItem> = listOf(
        SuggestionCardItem(
            id = "img_1",
            title = "Minimal desk setup",
            subtitle = "Warm morning sunlight & plants",
            prompt = "A calm minimal developer desk setup with warm morning sunlight, indoor plants, and soft shadows"
        ),
        SuggestionCardItem(
            id = "img_2",
            title = "App icon concept",
            subtitle = "Clean geometric glass gradient",
            prompt = "Minimalist geometric app icon with blue to violet glass gradient on a clean studio backdrop"
        ),
        SuggestionCardItem(
            id = "img_3",
            title = "Mountain sunrise",
            subtitle = "Golden hour mist over ridges",
            prompt = "Himalayan ridge at golden hour sunrise with soft mist rolling through valleys, cinematic lighting"
        ),
        SuggestionCardItem(
            id = "img_4",
            title = "Product hero shot",
            subtitle = "Matte ceramic studio lighting",
            prompt = "Modern matte ceramic coffee mug on pale stone surface with soft diffused studio lighting"
        )
    )

    val videoSuggestions: List<SuggestionCardItem> = listOf(
        SuggestionCardItem(
            id = "vid_1",
            title = "Rainy window loop",
            subtitle = "8s calm cinematic motion",
            prompt = "Slow cinematic close-up of raindrops sliding down a cozy cafe window at dusk with warm bokeh lights"
        ),
        SuggestionCardItem(
            id = "vid_2",
            title = "UI reveal motion",
            subtitle = "Smooth floating glass cards",
            prompt = "Smooth 3D product teaser showing floating glass UI cards assembling in a clean dark studio"
        ),
        SuggestionCardItem(
            id = "vid_3",
            title = "Aerial coastline",
            subtitle = "Gentle drone glide over waves",
            prompt = "Slow aerial drone glide over turquoise coastal waves meeting dark volcanic sand at sunset"
        ),
        SuggestionCardItem(
            id = "vid_4",
            title = "Time-lapse clouds",
            subtitle = "Golden light over city skyline",
            prompt = "Crisp time-lapse of golden hour clouds drifting across a modern skyline as city lights glow on"
        )
    )

    fun generateMediaCaption(prompt: String, isVideo: Boolean): String {
        val clean = prompt.trim().take(72)
        return if (isVideo) {
            "Generated an 8-second 1080p video clip for \"$clean\". Tap the preview to play or refine your scene."
        } else {
            "Generated a high-resolution image for \"$clean\". Tap the preview to inspect or describe edits."
        }
    }
    val seedIntegrationReply = """
Think of integration as the inverse of differentiation: **differentiation breaks rates down**, while **integration sums continuous pieces** (computing the area under a curve).

### 3 Core Rules You Use Everywhere
- **Power Rule:** `∫ x^n dx = x^(n+1) / (n+1) + C` (where `n ≠ -1`)
- **Substitution:** When a function and its derivative both appear inside, let `u = g(x)`.
- **Integration by Parts (ILATE):** `∫ u·dv = u·v - ∫ v·du` — remember the order *Inverse, Log, Algebraic, Trig, Exponential*.

### Quick Numerical Check in Python
You can verify a definite integral in 6 lines using the Trapezoidal Rule:

```python
def integrate(f, a, b, steps=1000):
    h = (b - a) / steps
    total = 0.5 * (f(a) + f(b))
    for i in range(1, steps):
        total += f(a + i * h)
    return total * h

print(round(integrate(lambda x: x**2, 0, 3), 4))  # Output: 9.0
```

1. Check first whether a standard direct formula applies.
2. For a product of two functions, apply *ILATE*.
3. Exam tip: definite integrals do not include `+ C` — always update bounds when substituting.

Want to walk through `∫ x·e^x dx` step by step? Reference: https://bansify.ai/notes/calculus
    """.trimIndent()

    val seedConversations: List<Conversation> = listOf(
        Conversation(
            id = "conv_integration",
            title = "Integration Basics & Python Check",
            preview = "Think of integration as the inverse of differentiation: differentiation breaks rates down...",
            relativeTime = "10m ago",
            timeBucket = TimeBucket.TODAY,
            modelId = "gemini-2.5-flash-lite",
            modelLabel = "Flash-Lite",
            isPinned = true,
            isShared = false,
            messages = listOf(
                ChatMessage(
                    id = "m_int_1",
                    role = MessageRole.USER,
                    content = "Explain the core rules of integration clearly and include a short Python example.",
                    timestampLabel = "6:12 PM",
                    dateGroup = "Today"
                ),
                ChatMessage(
                    id = "m_int_2",
                    role = MessageRole.ASSISTANT,
                    content = seedIntegrationReply,
                    timestampLabel = "6:12 PM",
                    dateGroup = "Today",
                    modelName = "Gemini 2.5 Flash-Lite"
                )
            )
        ),
        Conversation(
            id = "conv_image_desk",
            title = "Minimal Developer Desk Concept",
            preview = "Generated a high-resolution image for warm morning desk setup.",
            relativeTime = "1h ago",
            timeBucket = TimeBucket.TODAY,
            modelId = "auto",
            modelLabel = "Bansify Image",
            isPinned = false,
            mode = ChatMode.IMAGE,
            messages = listOf(
                ChatMessage(
                    id = "m_img_1",
                    role = MessageRole.USER,
                    content = "A calm minimal developer desk setup with warm morning sunlight and soft shadows",
                    timestampLabel = "9:30 AM",
                    dateGroup = "Today"
                ),
                ChatMessage(
                    id = "m_img_2",
                    role = MessageRole.ASSISTANT,
                    content = "Generated a high-resolution image for \"A calm minimal developer desk setup with warm morning sunlight\". Tap the preview to inspect or describe edits.",
                    timestampLabel = "9:30 AM",
                    dateGroup = "Today",
                    modelName = "Bansify Image",
                    generatedMedia = GeneratedMedia(
                        isVideo = false,
                        prompt = "A calm minimal developer desk setup with warm morning sunlight and soft shadows",
                        aspectRatio = "1:1",
                        seedIndex = 0
                    )
                )
            )
        ),
        Conversation(
            id = "conv_video_rain",
            title = "Rainy Cafe Window Cinematic Loop",
            preview = "Generated an 8-second 1080p video clip for rainy cafe window at dusk.",
            relativeTime = "3h ago",
            timeBucket = TimeBucket.TODAY,
            modelId = "auto",
            modelLabel = "Bansify Video",
            isPinned = false,
            mode = ChatMode.VIDEO,
            messages = listOf(
                ChatMessage(
                    id = "m_vid_1",
                    role = MessageRole.USER,
                    content = "Slow cinematic close-up of raindrops sliding down a cozy cafe window at dusk",
                    timestampLabel = "7:40 AM",
                    dateGroup = "Today"
                ),
                ChatMessage(
                    id = "m_vid_2",
                    role = MessageRole.ASSISTANT,
                    content = "Generated an 8-second 1080p video clip for \"Slow cinematic close-up of raindrops sliding down a cozy cafe window\". Tap the preview to play or refine your scene.",
                    timestampLabel = "7:40 AM",
                    dateGroup = "Today",
                    modelName = "Bansify Video",
                    generatedMedia = GeneratedMedia(
                        isVideo = true,
                        prompt = "Slow cinematic close-up of raindrops sliding down a cozy cafe window at dusk",
                        aspectRatio = "16:9",
                        durationLabel = "0:08 · 1080p",
                        seedIndex = 1
                    )
                )
            )
        ),
        Conversation(
            id = "conv_rdbms",
            title = "RDBMS vs NoSQL Interview Prep",
            preview = "Choose RDBMS when relationships and strict ACID transactions are core...",
            relativeTime = "Yesterday",
            timeBucket = TimeBucket.YESTERDAY,
            modelId = "groq-llama-3.3-70b",
            modelLabel = "Llama 70B",
            isPinned = true,
            isShared = true,
            messages = listOf(
                ChatMessage(
                    id = "m_db_1",
                    role = MessageRole.USER,
                    content = "Give me a crisp interview comparison of RDBMS vs NoSQL.",
                    timestampLabel = "4:40 PM",
                    dateGroup = "Yesterday"
                ),
                ChatMessage(
                    id = "m_db_2",
                    role = MessageRole.ASSISTANT,
                    content = """
Here is a crisp interview-ready breakdown:

- **RDBMS (PostgreSQL, MySQL):** Fixed schema, relational `JOIN` queries, strong **ACID** transactions. Best for banking, billing, and orders.
- **NoSQL (MongoDB, Firestore, Redis):** Flexible document or key-value storage, easy horizontal scaling. Ideal for feeds, catalogs, and real-time chat.

```sql
-- RDBMS example: relational join
SELECT u.name, COUNT(o.id) AS total_orders
FROM users u
JOIN orders o ON o.user_id = u.id
GROUP BY u.name;
```

**One-liner summary:** *"Pick SQL when relationships and strict consistency come first; pick NoSQL when schemas evolve rapidly and read scale dominates."*
                    """.trimIndent(),
                    timestampLabel = "4:41 PM",
                    dateGroup = "Yesterday",
                    modelName = "Llama 3.3 70B"
                )
            )
        ),
        Conversation(
            id = "conv_resume",
            title = "Android Resume Bullet Polish",
            preview = "Frame each project bullet as Action + Tech Stack + Measurable Impact...",
            relativeTime = "3d ago",
            timeBucket = TimeBucket.LAST_7_DAYS,
            modelId = "gemini-2.5-flash",
            modelLabel = "Gemini Flash",
            isPinned = false,
            isShared = false,
            messages = listOf(
                ChatMessage(
                    id = "m_res_1",
                    role = MessageRole.USER,
                    content = "How do I make the projects section stronger on an Android developer resume?",
                    timestampLabel = "17 Nov",
                    dateGroup = "17 Nov"
                ),
                ChatMessage(
                    id = "m_res_2",
                    role = MessageRole.ASSISTANT,
                    content = """
Structure every project bullet using **Action + Stack + Measurable Impact**:

1. **Weak:** *"Built a chat app using Kotlin."*
2. **Strong:** *"Built an offline-first Jetpack Compose chat interface with `StateFlow` and a custom Markdown renderer, keeping cold start under 450ms."*

- Keep your top 2–3 strongest projects first with clickable GitHub links.
- Group skills cleanly under *Languages*, *Android / UI*, and *Tooling*.
                    """.trimIndent(),
                    timestampLabel = "17 Nov",
                    dateGroup = "17 Nov",
                    modelName = "Gemini 2.5 Flash"
                )
            )
        ),
        Conversation(
            id = "conv_react",
            title = "React Hooks: useMemo vs useCallback",
            preview = "Both are memoization hooks—avoid premature optimization until profiling shows a bottleneck...",
            relativeTime = "1w ago",
            timeBucket = TimeBucket.LAST_7_DAYS,
            modelId = "groq-qwen3-32b",
            modelLabel = "Qwen3 32B",
            isPinned = false,
            isShared = true,
            messages = listOf(
                ChatMessage(
                    id = "m_re_1",
                    role = MessageRole.USER,
                    content = "When should I use useMemo vs useCallback?",
                    timestampLabel = "13 Nov",
                    dateGroup = "13 Nov"
                ),
                ChatMessage(
                    id = "m_re_2",
                    role = MessageRole.ASSISTANT,
                    content = """
Both are memoization hooks, so reach for them when you actually need referential stability or expensive computation caching:

- `useMemo` caches a **computed value** (such as filtering a large list).
- `useCallback` caches a **function reference** (useful when passing callbacks to `React.memo` children).

```tsx
const filtered = useMemo(
  () => items.filter((item) => item.active),
  [items]
);
```
                    """.trimIndent(),
                    timestampLabel = "13 Nov",
                    dateGroup = "13 Nov",
                    modelName = "Qwen3 32B"
                )
            )
        ),
        Conversation(
            id = "conv_health",
            title = "Daily Desk Routine & Focus",
            preview = "Sustainable desk routine: 20-20-20 eye breaks, hourly stretches, and consistent sleep...",
            relativeTime = "Older",
            timeBucket = TimeBucket.OLDER,
            modelId = "glm-4.7-flash",
            modelLabel = "GLM-4.7",
            isPinned = false,
            isShared = false,
            messages = listOf(
                ChatMessage(
                    id = "m_he_1",
                    role = MessageRole.USER,
                    content = "What is a simple daily health routine for long desk study sessions?",
                    timestampLabel = "28 Oct",
                    dateGroup = "28 Oct"
                ),
                ChatMessage(
                    id = "m_he_2",
                    role = MessageRole.ASSISTANT,
                    content = """
A lightweight routine that is easy to stick to:

- **20-20-20 eye rule:** Every 20 minutes, look 20 feet away for 20 seconds.
- **Movement breaks:** Stand and stretch for 3 minutes every hour, plus a 30-minute walk daily.
- **Hydration & sleep:** Keep a 1L water bottle on your desk and aim for a steady sleep schedule.

*Note: These are general wellness tips; consult a clinician for medical concerns.*
                    """.trimIndent(),
                    timestampLabel = "28 Oct",
                    dateGroup = "28 Oct",
                    modelName = "GLM-4.7-Flash"
                )
            )
        )
    )

    private val hinglishMarkers = setOf(
        "kaise", "kya", "karo", "samjhao", "banao", "likho", "bhai", "mera", "meri",
        "aaj", "kal", "padhai", "hai", "hain", "nahi", "mujhe", "batao", "hindi"
    )

    private fun looksLikeHinglish(prompt: String): Boolean {
        val words = prompt.lowercase().split(Regex("[^a-z]+")).filter { it.isNotBlank() }
        return words.any { it in hinglishMarkers }
    }

    /**
     * AI replies adapt to the user's language (English by default, Hinglish if user writes in Hinglish).
     */
    fun generateMockReply(prompt: String, style: ResponseStyle): String {
        val lower = prompt.lowercase()
        val useHinglish = looksLikeHinglish(prompt)

        if (useHinglish) {
            val detailSuffix = when (style) {
                ResponseStyle.SHORT -> "\n\n*(Short mode — aur detail chahiye toh poochho.)*"
                ResponseStyle.BALANCED -> ""
                ResponseStyle.DETAILED -> "\n\n### Next Step\nAgar aap apna exact input paste karein, toh main step-by-step walkthrough bhi de sakta hoon."
            }
            return """
Bilkul! Aapke sawal **"${prompt.take(64)}"** ka clear breakdown yahan hai:

- **Core idea:** Sabse pehle problem ko chhote measurable steps me tod lo.
- **Practical approach:** Ek working draft banao, phir edge cases aur clarity polish karo.
- **Quick check:** Concept ko simple words me explain karke verify karo.

```text
Step 1 -> Goal clear karo
Step 2 -> Smallest working version banao
Step 3 -> Review & refine
```

Kya isme kisi specific part par aur detail me baat karein?
            """.trimIndent() + detailSuffix
        }

        val detailSuffix = when (style) {
            ResponseStyle.SHORT -> "\n\n*(Short mode active — ask if you want a deeper dive.)*"
            ResponseStyle.BALANCED -> ""
            ResponseStyle.DETAILED -> "\n\n### Next Step\nShare your exact input or snippet and I can walk through it line by line."
        }
        return when {
            lower.contains("trig") || lower.contains("math") -> """
Remember **Trigonometry** through three core identities:

- **Pythagorean core:** `sin²θ + cos²θ = 1`
- **Sec–Tan pair:** `1 + tan²θ = sec²θ`
- **Cosec–Cot pair:** `1 + cot²θ = cosec²θ`

### Quick Solved Example
Prove: `(1 - cos²θ) · cosec²θ = 1`
1. Substitute `1 - cos²θ` with `sin²θ`.
2. Multiply `sin²θ · (1 / sin²θ) = 1` — done.
            """.trimIndent() + detailSuffix

            lower.contains("code") || lower.contains("debug") || lower.contains("kotlin") || lower.contains("compose") -> """
This pattern prevents the majority of Jetpack Compose state bugs:

- **Single source of truth:** Hold screen state in one immutable `data class`.
- **Keep side effects out of composition:** Trigger coroutines inside `ViewModel` or `LaunchedEffect`.

```kotlin
@Immutable
data class ScreenState(
    val query: String = "",
    val isLoading: Boolean = false
)

fun onQueryChange(next: String) {
    _state.update { it.copy(query = next.trimStart()) }
}
```

This keeps recompositions predictable and makes debugging significantly faster.
            """.trimIndent() + detailSuffix

            lower.contains("plan") || lower.contains("study") -> """
Here is a calm, focused **3-Block Study Plan**:

1. **Block 1 (50 min):** Tackle the hardest topic first — notifications off, core concept + 3 examples.
2. **Break (10 min):** Step away from the screen, hydrate, and stretch.
3. **Block 2 (50 min):** Solve practice problems without peeking at solutions.
4. **Block 3 (40 min):** Active recall — summarize everything learned into 5 concise bullets.

*Tip:* Starting the first 25-minute sprint beats spending an hour perfecting a schedule.
            """.trimIndent() + detailSuffix

            else -> """
Here is a clear breakdown for **"${prompt.take(64)}"**:

- **Core idea:** Break the problem into small, verifiable milestones before optimizing.
- **Practical approach:** Build a working first pass, then tighten edge cases and readability.
- **Quick check:** If you can summarize the solution in two plain sentences, the structure is solid.

```text
Step 1 -> Clarify the goal
Step 2 -> Build the smallest working version
Step 3 -> Review and refine
```

Would you like to go deeper on any specific step?
            """.trimIndent() + detailSuffix
        }
    }
}
