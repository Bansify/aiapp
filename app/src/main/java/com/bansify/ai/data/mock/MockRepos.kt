package com.bansify.ai.data.mock

data class RepoFileNode(
    val path: String,
    val name: String,
    val folder: String, // "" for root
    val language: String,
    val sizeLabel: String,
    val content: String
)

data class MockCommit(
    val hash: String,
    val message: String,
    val author: String,
    val relativeTime: String
)

data class MockRepo(
    val id: String,
    val fullName: String,
    val projectName: String,
    val description: String,
    val framework: String,
    val previewUrl: String,
    val primaryLanguage: String,
    val languageColorHex: Long,
    val lastUpdated: String,
    val stars: Int,
    val branches: List<String>,
    val activeBranch: String,
    val connectedIntegrationIds: Set<String>,
    val files: List<RepoFileNode>,
    val commits: List<MockCommit>,
    val chatMessages: List<ChatMessage>
)

data class ZipConflictItem(
    val id: String,
    val filePath: String,
    val resolution: String? = null // "Keep", "Theirs", "Reviewed"
)

object MockRepos {
    val seedRepos: List<MockRepo> = listOf(
        MockRepo(
            id = "repo_ai",
            fullName = "bansify/ai",
            projectName = "Bansify Mobile App",
            description = "Android Compose client + local streaming engine with calm Material 3 tokens.",
            framework = "Jetpack Compose",
            previewUrl = "https://bansify-ai.preview.dev",
            primaryLanguage = "Kotlin",
            languageColorHex = 0xFF7A5CFF,
            lastUpdated = "2 hours ago",
            stars = 148,
            branches = listOf("main", "feat/studio-ui", "feat/model-sheet", "fix/rtl-drawer"),
            activeBranch = "main",
            connectedIntegrationIds = setOf("github", "vercel", "figma", "firebase"),
            commits = listOf(
                MockCommit("a94f2c1", "feat(chat): blinking cursor streaming & markdown blocks", "Pawan", "2h ago"),
                MockCommit("7e2b910", "feat(models): add 9 card-free providers + quota chips", "Pawan", "Yesterday"),
                MockCommit("3d10c88", "chore(theme): calibrate dark surface tokens", "Pawan", "3d ago"),
                MockCommit("1c8e0f4", "init: Bansify AI Compose prototype scaffold", "Pawan", "5d ago")
            ),
            files = listOf(
                RepoFileNode(
                    path = "README.md",
                    name = "README.md",
                    folder = "",
                    language = "Markdown",
                    sizeLabel = "1.8 KB",
                    content = "# Bansify AI\n\nCalm, roomy Android AI assistant + developer Studio.\n\n## Highlights\n- Single-accent Material 3 design system (`#2F6BFF`)\n- Multi-provider model router with free-tier visibility\n- Offline-ready mock streaming engine for rapid UI iteration"
                ),
                RepoFileNode(
                    path = "app/MainActivity.kt",
                    name = "MainActivity.kt",
                    folder = "app",
                    language = "Kotlin",
                    sizeLabel = "2.1 KB",
                    content = "package com.bansify.ai\n\nimport android.os.Bundle\nimport androidx.activity.ComponentActivity\nimport androidx.activity.compose.setContent\nimport androidx.activity.enableEdgeToEdge\n\nclass MainActivity : ComponentActivity() {\n    override fun onCreate(savedInstanceState: Bundle?) {\n        super.onCreate(savedInstanceState)\n        enableEdgeToEdge()\n        setContent { BansifyRoot() }\n    }\n}"
                ),
                RepoFileNode(
                    path = "app/nav/NavGraph.kt",
                    name = "NavGraph.kt",
                    folder = "app/nav",
                    language = "Kotlin",
                    sizeLabel = "3.4 KB",
                    content = "package com.bansify.ai.nav\n\n// Type-safe routes connecting Splash, Onboarding, Chat, Studio, Models, Settings.\nobject Routes {\n    const val SPLASH = \"splash\"\n    const val CHAT = \"chat\"\n    const val STUDIO = \"studio\"\n}"
                ),
                RepoFileNode(
                    path = "core/designsystem/Theme.kt",
                    name = "Theme.kt",
                    folder = "core/designsystem",
                    language = "Kotlin",
                    sizeLabel = "2.6 KB",
                    content = "package com.bansify.ai.core.designsystem\n\n// Calm palette tokens: Light #FFFFFF / Dark #0F1115, Accent #2F6BFF\nenum class ThemeMode { SYSTEM, LIGHT, DARK }"
                ),
                RepoFileNode(
                    path = "feature/chat/ChatViewModel.kt",
                    name = "ChatViewModel.kt",
                    folder = "feature/chat",
                    language = "Kotlin",
                    sizeLabel = "4.8 KB",
                    content = "package com.bansify.ai.feature.chat\n\n// Streams mock assistant tokens every 40ms with cooperative cancellation.\nclass ChatViewModel {\n    fun stopStreaming() {\n        // Cancels active streaming job cleanly\n    }\n}"
                ),
                RepoFileNode(
                    path = "docs/DESIGN_TOKENS.md",
                    name = "DESIGN_TOKENS.md",
                    folder = "docs",
                    language = "Markdown",
                    sizeLabel = "1.2 KB",
                    content = "# Design Tokens\n\n- Accent: `#2F6BFF`\n- Brand gradient: `#2F6BFF` -> `#7A5CFF`\n- Motion: 150ms micro, 220ms screen"
                )
            ),
            chatMessages = listOf(
                ChatMessage(
                    id = "st_ai_1",
                    role = MessageRole.USER,
                    content = "Connect bansify/ai main branch and summarize what we should polish before release.",
                    timestampLabel = "10:15 AM",
                    dateGroup = "Today"
                ),
                ChatMessage(
                    id = "st_ai_2",
                    role = MessageRole.ASSISTANT,
                    content = """
Workspace **bansify/ai** (`main`) is synced with **GitHub**, **Vercel**, **Figma**, and **Firebase**.

### Quick Release Checklist
- **UI Polish:** All Compose screens use 8dp spacing tokens and `48dp` touch targets.
- **Preview:** Switch to **Preview** in the top header to inspect the interactive app frame.
- **Code:** Open **Code** to browse `app/MainActivity.kt`, switch branches, or edit files.

Ask me to generate a new screen, refactor a component, or deploy a preview build.
                    """.trimIndent(),
                    timestampLabel = "10:15 AM",
                    dateGroup = "Today",
                    modelName = "Studio Agent"
                )
            )
        ),
        MockRepo(
            id = "repo_mail",
            fullName = "bansify/mail",
            projectName = "Bansify Mail Templates",
            description = "Clean transactional email templates and Cloudflare Worker delivery pipeline.",
            framework = "React Email · Workers",
            previewUrl = "https://mail.bansify.preview.dev",
            primaryLanguage = "TypeScript",
            languageColorHex = 0xFF2F6BFF,
            lastUpdated = "Yesterday",
            stars = 64,
            branches = listOf("main", "feat/digest-template", "chore/dkim"),
            activeBranch = "main",
            connectedIntegrationIds = setOf("github", "cloudflare", "figma", "stripe"),
            commits = listOf(
                MockCommit("f219e0a", "feat: add weekly study recap template", "Pawan", "Yesterday"),
                MockCommit("881d4a2", "fix: dark-mode contrast in welcome email", "Pawan", "4d ago"),
                MockCommit("450a11b", "chore: setup local preview server", "Pawan", "1w ago")
            ),
            files = listOf(
                RepoFileNode("README.md", "README.md", "", "Markdown", "1.1 KB", "# Bansify Mail\n\nMinimalist email templates built with TypeScript."),
                RepoFileNode("src/index.ts", "index.ts", "src", "TypeScript", "1.4 KB", "export function renderWelcome(name: string): string {\n  return `Welcome to Bansify AI, \${name}.`;\n}"),
                RepoFileNode("src/templates/welcome.tsx", "welcome.tsx", "src/templates", "TSX", "2.2 KB", "export const WelcomeEmail = ({ name }: { name: string }) => (\n  <main><h1>Welcome, {name}</h1></main>\n);"),
                RepoFileNode("src/templates/digest.tsx", "digest.tsx", "src/templates", "TSX", "2.7 KB", "export const DigestEmail = () => <section>Weekly Summary</section>;"),
                RepoFileNode("config/dns.json", "dns.json", "config", "JSON", "0.6 KB", "{\n  \"spf\": \"v=spf1 include:_spf.bansify.ai ~all\",\n  \"dmarc\": \"v=DMARC1; p=quarantine\"\n}"),
                RepoFileNode("package.json", "package.json", "", "JSON", "0.8 KB", "{\n  \"name\": \"@bansify/mail\",\n  \"version\": \"0.4.0\"\n}")
            ),
            chatMessages = listOf(
                ChatMessage(
                    id = "st_mail_1",
                    role = MessageRole.ASSISTANT,
                    content = "Ready on **bansify/mail** (`main`). Connected to **GitHub**, **Cloudflare**, **Figma**, and **Stripe**. Describe a template update or switch to **Preview** to inspect the rendered email.",
                    timestampLabel = "Yesterday",
                    dateGroup = "Yesterday",
                    modelName = "Studio Agent"
                )
            )
        ),
        MockRepo(
            id = "repo_accounts",
            fullName = "bansify/accounts",
            projectName = "Bansify Accounts API",
            description = "Unified identity, session policies, and Supabase/Clerk auth bridge.",
            framework = "Go · REST",
            previewUrl = "https://accounts.bansify.preview.dev",
            primaryLanguage = "Go",
            languageColorHex = 0xFF12A150,
            lastUpdated = "4 days ago",
            stars = 91,
            branches = listOf("main", "feat/guest-upgrade", "security/passkeys"),
            activeBranch = "main",
            connectedIntegrationIds = setOf("github", "supabase", "clerk", "sentry"),
            commits = listOf(
                MockCommit("c7742e1", "feat: guest-to-google account merge spec", "Pawan", "4d ago"),
                MockCommit("9a301f9", "feat: add JSON/Markdown data export schema", "Pawan", "1w ago"),
                MockCommit("2b098d4", "docs: privacy retention policy notes", "Pawan", "2w ago")
            ),
            files = listOf(
                RepoFileNode("README.md", "README.md", "", "Markdown", "1.3 KB", "# Bansify Accounts\n\nPrivacy-first identity & data portability service."),
                RepoFileNode("cmd/server/main.go", "main.go", "cmd/server", "Go", "1.5 KB", "package main\n\nimport \"fmt\"\n\nfunc main() {\n\tfmt.Println(\"bansify-accounts mock server\")\n}"),
                RepoFileNode("internal/auth/session.go", "session.go", "internal/auth", "Go", "2.1 KB", "package auth\n\ntype Session struct {\n\tUserID  string\n\tIsGuest bool\n}"),
                RepoFileNode("schemas/profile.json", "profile.json", "schemas", "JSON", "0.7 KB", "{\n  \"name\": \"string\",\n  \"profession\": \"string\",\n  \"goals\": [\"string\"]\n}"),
                RepoFileNode("go.mod", "go.mod", "", "Go", "0.3 KB", "module github.com/bansify/accounts\n\ngo 1.24")
            ),
            chatMessages = listOf(
                ChatMessage(
                    id = "st_acc_1",
                    role = MessageRole.ASSISTANT,
                    content = "Connected to **bansify/accounts** (`main`) with **GitHub**, **Supabase**, **Clerk**, and **Sentry**. Ask me to add an endpoint, inspect `session.go`, or test auth flows.",
                    timestampLabel = "4 days ago",
                    dateGroup = "Last 7 days",
                    modelName = "Studio Agent"
                )
            )
        )
    )

    fun generateStudioReply(repo: MockRepo, prompt: String): String {
        val connectedNames = MockIntegrations.catalog
            .filter { it.id in repo.connectedIntegrationIds }
            .take(4)
            .joinToString(", ") { it.name }
            .ifEmpty { "Local workspace" }

        return """
Updated **${repo.projectName}** (`${repo.fullName}` · `${repo.activeBranch}`) for: *"${prompt.take(64)}"*

### Planned Changes
- **UI & Components:** Refined layout structure inside `${repo.files.firstOrNull { it.folder.isNotEmpty() }?.path ?: "README.md"}`
- **Connected Stack:** Synced context with **$connectedNames**
- **Build Check:** Preview bundle rebuilt cleanly with zero errors.

```kotlin
// Studio patch applied to ${repo.activeBranch}
fun applyStudioUpdate() {
    // Switch to Preview or Code in the header to inspect changes
}
```

Tap **Preview** in the top header to test the updated UI, or **Code** to review the file diff.
        """.trimIndent()
    }
}
