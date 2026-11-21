package com.bansify.ai.data.repo

import com.bansify.ai.data.mock.ChatMessage
import com.bansify.ai.data.mock.MessageRole
import com.bansify.ai.data.mock.MockCommit
import com.bansify.ai.data.mock.MockRepo
import com.bansify.ai.data.mock.MockRepos
import com.bansify.ai.data.mock.RepoFileNode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class StudioRepository {
    private val _repos = MutableStateFlow(MockRepos.seedRepos)
    val repos: StateFlow<List<MockRepo>> = _repos.asStateFlow()

    suspend fun loadRepos(simulateError: Boolean = false): Result<List<MockRepo>> {
        delay(680L)
        return if (simulateError) {
            Result.failure(IllegalStateException("Studio projects failed to sync (mock)."))
        } else {
            Result.success(_repos.value)
        }
    }

    suspend fun syncRepos(): Result<List<MockRepo>> {
        delay(1000L)
        _repos.update { list ->
            list.mapIndexed { idx, repo ->
                if (idx == 0) repo.copy(lastUpdated = "Just synced") else repo
            }
        }
        return Result.success(_repos.value)
    }

    suspend fun loadRepoById(repoId: String, simulateError: Boolean = false): Result<MockRepo?> {
        delay(600L)
        return if (simulateError) {
            Result.failure(IllegalStateException("Unable to load project workspace (mock)."))
        } else {
            Result.success(_repos.value.firstOrNull { it.id == repoId })
        }
    }

    fun createProject(name: String, prompt: String? = null): MockRepo {
        val cleanName = name.trim().ifEmpty { "Untitled Studio App" }
        val slug = cleanName.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-').ifEmpty { "studio-app" }
        val newId = "repo_${System.currentTimeMillis()}"
        val initialMessages = buildList {
            if (!prompt.isNullOrBlank()) {
                add(
                    ChatMessage(
                        id = "st_u_${System.currentTimeMillis()}",
                        role = MessageRole.USER,
                        content = prompt.trim(),
                        timestampLabel = "Just now",
                        dateGroup = "Today"
                    )
                )
            }
            add(
                ChatMessage(
                    id = "st_a_${System.currentTimeMillis() + 1}",
                    role = MessageRole.ASSISTANT,
                    content = "Created **$cleanName** (`bansify/$slug` · `main`). Connected **GitHub** and **Vercel** by default — tap **Connections** anytime to add Cloudflare, Figma, Supabase, or 25+ more tools.",
                    timestampLabel = "Just now",
                    dateGroup = "Today",
                    modelName = "Studio Agent"
                )
            )
        }

        val repo = MockRepo(
            id = newId,
            fullName = "bansify/$slug",
            projectName = cleanName,
            description = prompt?.trim()?.take(90) ?: "New Bansify Studio workspace with live preview and code editor.",
            framework = "Next.js · TypeScript",
            previewUrl = "https://$slug.preview.dev",
            primaryLanguage = "TypeScript",
            languageColorHex = 0xFF2F6BFF,
            lastUpdated = "Just now",
            stars = 1,
            branches = listOf("main", "feat/initial-ui"),
            activeBranch = "main",
            connectedIntegrationIds = setOf("github", "vercel"),
            files = listOf(
                RepoFileNode("README.md", "README.md", "", "Markdown", "0.9 KB", "# $cleanName\n\nBuilt with Bansify Studio."),
                RepoFileNode("src/App.tsx", "App.tsx", "src", "TSX", "1.4 KB", "export default function App() {\n  return <main><h1>$cleanName</h1></main>;\n}")
            ),
            commits = listOf(
                MockCommit("e10f9a2", "init: scaffold $cleanName workspace", "Pawan", "Just now")
            ),
            chatMessages = initialMessages
        )
        _repos.update { listOf(repo) + it }
        return repo
    }

    fun getFile(repoId: String, filePath: String): RepoFileNode? {
        return _repos.value
            .firstOrNull { it.id == repoId }
            ?.files
            ?.firstOrNull { it.path == filePath }
    }

    fun switchBranch(repoId: String, branch: String) {
        _repos.update { list ->
            list.map { repo -> if (repo.id == repoId) repo.copy(activeBranch = branch) else repo }
        }
    }

    fun toggleIntegration(repoId: String, integrationId: String) {
        _repos.update { list ->
            list.map { repo ->
                if (repo.id == repoId) {
                    val updated = if (integrationId in repo.connectedIntegrationIds) {
                        repo.connectedIntegrationIds - integrationId
                    } else {
                        repo.connectedIntegrationIds + integrationId
                    }
                    repo.copy(connectedIntegrationIds = updated)
                } else repo
            }
        }
    }

    fun appendProjectMessage(repoId: String, message: ChatMessage) {
        _repos.update { list ->
            list.map { repo ->
                if (repo.id == repoId) {
                    repo.copy(
                        chatMessages = repo.chatMessages + message,
                        lastUpdated = "Just now"
                    )
                } else repo
            }
        }
    }

    fun updateProjectMessage(repoId: String, messageId: String, transform: (ChatMessage) -> ChatMessage) {
        _repos.update { list ->
            list.map { repo ->
                if (repo.id == repoId) {
                    repo.copy(
                        chatMessages = repo.chatMessages.map { msg ->
                            if (msg.id == messageId) transform(msg) else msg
                        }
                    )
                } else repo
            }
        }
    }

    fun updateFileContent(repoId: String, filePath: String, newContent: String) {
        _repos.update { list ->
            list.map { repo ->
                if (repo.id == repoId) {
                    repo.copy(
                        files = repo.files.map { file ->
                            if (file.path == filePath) file.copy(content = newContent) else file
                        }
                    )
                } else repo
            }
        }
    }
}
