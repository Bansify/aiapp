package com.bansify.ai.data.mock

import com.bansify.ai.core.designsystem.ThemeMode

enum class ResponseStyle(val label: String) {
    SHORT("Short"),
    BALANCED("Balanced"),
    DETAILED("Detailed")
}

data class UserProfile(
    val name: String = "Pawan",
    val profession: String = "Student",
    val goals: List<String> = listOf("Study", "Coding"),
    val aboutYou: String = "Computer science student interested in Android, Kotlin, and clean UI design. Prefers concise examples.",
    val isGuest: Boolean = false,
    val email: String = "pawan@bansify.ai",
    val onboardingCompleted: Boolean = false
)

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val fontScale: Float = 1.0f,
    val dynamicColor: Boolean = false,
    val defaultModelId: String = "auto",
    val autoFallback: Boolean = true,
    val responseStyle: ResponseStyle = ResponseStyle.BALANCED,
    val streamingEnabled: Boolean = true,
    val sendOnEnter: Boolean = false,
    val offlineDemoMode: Boolean = false
)

data class SuggestionCardItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val prompt: String
)

data class MemoryFact(
    val id: String,
    val category: String,
    val text: String
)

object MockUser {
    val professionOptions = listOf(
        "Student", "Doctor", "Engineer", "Teacher", "Developer", "Designer",
        "Business owner", "Homemaker", "Lawyer", "CA", "Freelancer", "Other"
    )

    val goalOptions = listOf(
        "Study", "Coding", "Writing", "Health", "Career",
        "Business", "Daily life", "Creative"
    )

    fun greetingFor(profile: UserProfile): String {
        val cleanName = profile.name.trim()
        if (profile.isGuest || cleanName.isEmpty()) {
            return "Hello. What would you like to explore?"
        }
        return when {
            profile.profession.equals("Doctor", ignoreCase = true) ->
                "Hello Dr. $cleanName. What are you reviewing today?"
            profile.profession.equals("Developer", ignoreCase = true) ||
                profile.profession.equals("Engineer", ignoreCase = true) ->
                "Hello $cleanName. What are we building today?"
            profile.profession.equals("Student", ignoreCase = true) &&
                profile.goals.any { it.equals("Study", ignoreCase = true) } ->
                "Hello $cleanName. Which subject today, or jump straight to a question?"
            else ->
                "Hello $cleanName. How can I help today?"
        }
    }

    fun postOnboardOpeningMessage(profile: UserProfile): String {
        val greeting = greetingFor(profile)
        val goalsText = if (profile.goals.isNotEmpty()) {
            "I've noted that you'd like help with **${profile.goals.joinToString(", ")}**."
        } else {
            "You can update your profile and preferences anytime in Settings → Customise."
        }
        return buildString {
            appendLine(greeting)
            appendLine()
            appendLine(goalsText)
            appendLine()
            appendLine("Easy ways to get started:")
            appendLine("- Ask a question in any language — I'll reply in the same language")
            appendLine("- Paste code to debug, refactor, or review")
            appendLine("- Tap the model chip in the top bar to switch between **free and pro models**")
        }.trim()
    }

    fun suggestionsFor(profile: UserProfile): List<SuggestionCardItem> {
        return when {
            profile.profession.equals("Doctor", ignoreCase = true) -> listOf(
                SuggestionCardItem("s1", "Draft case summary", "Clean structured OPD notes", "Create a concise OPD case summary template with structured sections."),
                SuggestionCardItem("s2", "Patient checklist", "Simple daily care guide", "Write a clear, easy-to-follow lifestyle checklist for hypertension management."),
                SuggestionCardItem("s3", "Pharmacology recap", "Quick beta-blocker table", "Summarize the main classes of beta-blockers and common side effects in bullets."),
                SuggestionCardItem("s4", "Paper breakdown", "Extract 5 key takeaways", "Give me a 5-bullet framework for summarizing a clinical trial abstract.")
            )
            profile.profession.equals("Developer", ignoreCase = true) ||
                profile.profession.equals("Engineer", ignoreCase = true) -> listOf(
                SuggestionCardItem("s1", "Debug this code", "StateFlow & Compose tips", "How do I debug and prevent unnecessary recompositions in Jetpack Compose? Show an example."),
                SuggestionCardItem("s2", "Kotlin coroutines", "Flow vs StateFlow", "Explain the difference between Kotlin Flow and StateFlow with a practical Android example."),
                SuggestionCardItem("s3", "Optimize SQL query", "Indexing & query plans", "How do I speed up a slow PostgreSQL JOIN query using proper indexes?"),
                SuggestionCardItem("s4", "PR review checklist", "Clean Android standards", "Write a concise 6-point pull request review checklist for Jetpack Compose apps.")
            )
            profile.profession.equals("Student", ignoreCase = true) -> listOf(
                SuggestionCardItem("s1", "Explain trigonometry", "Core identities & tricks", "Explain the core trigonometric identities with memory tricks and a solved example."),
                SuggestionCardItem("s2", "Debug my code", "Kotlin or Python errors", "Show a common Compose state management bug and how to fix it cleanly."),
                SuggestionCardItem("s3", "Structure an essay", "Clear outline & arguments", "Create a well-structured short essay outline on AI in modern education."),
                SuggestionCardItem("s4", "Build a study plan", "Focused 3-hour blocks", "Build a focused 3-block Pomodoro study plan for math and data structures today.")
            )
            else -> listOf(
                SuggestionCardItem("s1", "Plan my day", "Clear top 3 priorities", "Help me organize today's tasks into 3 focused priority blocks."),
                SuggestionCardItem("s2", "Polish an email", "Crisp professional tone", "Draft a polite, concise client follow-up email for a project milestone."),
                SuggestionCardItem("s3", "Learn a new topic", "5-minute fundamentals", "Explain how compound interest and index investing work in plain English."),
                SuggestionCardItem("s4", "Brainstorm ideas", "Fresh product angles", "Suggest 5 practical launch ideas for an indie productivity mobile app.")
            )
        }
    }

    val initialMemoryFacts = listOf(
        MemoryFact("m1", "Style", "Prefers concise bullet points and practical examples over long intros."),
        MemoryFact("m2", "Stack", "Works with Android (Kotlin, Jetpack Compose, Material 3) and Python."),
        MemoryFact("m3", "Learning", "Likes a quick follow-up practice question after concept explanations."),
        MemoryFact("m4", "Code", "Prefers clean idiomatic code blocks with minimal boilerplate.")
    )
}
