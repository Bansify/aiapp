package com.bansify.ai.feature.studio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bansify.ai.data.mock.ChatMessage
import com.bansify.ai.data.mock.MessageRole
import com.bansify.ai.data.mock.MockRepo
import com.bansify.ai.data.mock.MockRepos
import com.bansify.ai.data.repo.StudioRepository
import com.bansify.ai.state.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class StudioHomeData(
    val repos: List<MockRepo>,
    val query: String,
    val isSyncing: Boolean,
    val syncedRecently: Boolean
)

class StudioViewModel(
    private val studioRepo: StudioRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)
    private val _query = MutableStateFlow("")
    private val _isSyncing = MutableStateFlow(false)
    private val _syncedRecently = MutableStateFlow(false)
    private val _isProjectStreaming = MutableStateFlow(false)

    private var projectStreamJob: Job? = null

    private val _homeState = MutableStateFlow<UiState<StudioHomeData>>(UiState.Loading)
    val homeState: StateFlow<UiState<StudioHomeData>> = _homeState.asStateFlow()

    val repos: StateFlow<List<MockRepo>> = studioRepo.repos
    val isProjectStreaming: StateFlow<Boolean> = _isProjectStreaming.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                studioRepo.repos,
                _isLoading,
                _error,
                _query,
                _isSyncing,
                _syncedRecently
            ) { values ->
                @Suppress("UNCHECKED_CAST")
                val allRepos = values[0] as List<MockRepo>
                val loading = values[1] as Boolean
                val error = values[2] as String?
                val query = values[3] as String
                val syncing = values[4] as Boolean
                val synced = values[5] as Boolean

                when {
                    loading -> UiState.Loading
                    error != null -> UiState.Error(error)
                    allRepos.isEmpty() -> UiState.Empty
                    else -> {
                        val q = query.trim().lowercase()
                        val filtered = if (q.isEmpty()) {
                            allRepos
                        } else {
                            allRepos.filter {
                                it.projectName.lowercase().contains(q) ||
                                    it.fullName.lowercase().contains(q) ||
                                    it.description.lowercase().contains(q) ||
                                    it.primaryLanguage.lowercase().contains(q)
                            }
                        }
                        UiState.Success(
                            StudioHomeData(
                                repos = filtered,
                                query = query,
                                isSyncing = syncing,
                                syncedRecently = synced
                            )
                        )
                    }
                }
            }.collect { _homeState.value = it }
        }
        refreshHome(false)
    }

    fun refreshHome(simulateError: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val res = studioRepo.loadRepos(simulateError)
            _isLoading.value = false
            res.onFailure { _error.value = it.message ?: "Studio failed to load" }
        }
    }

    fun onQueryChange(q: String) {
        _query.value = q
    }

    fun syncNow() {
        if (_isSyncing.value) return
        viewModelScope.launch {
            _isSyncing.value = true
            _syncedRecently.value = false
            studioRepo.syncRepos()
            _isSyncing.value = false
            _syncedRecently.value = true
            delay(2600L)
            _syncedRecently.value = false
        }
    }

    fun createProject(name: String, prompt: String? = null): MockRepo {
        return studioRepo.createProject(name, prompt)
    }

    suspend fun loadRepo(repoId: String, simulateError: Boolean = false): Result<MockRepo?> {
        return studioRepo.loadRepoById(repoId, simulateError)
    }

    fun switchBranch(repoId: String, branch: String) {
        studioRepo.switchBranch(repoId, branch)
    }

    fun toggleIntegration(repoId: String, integrationId: String) {
        studioRepo.toggleIntegration(repoId, integrationId)
    }

    fun sendProjectPrompt(repoId: String, prompt: String) {
        val clean = prompt.trim()
        if (clean.isEmpty() || _isProjectStreaming.value) return
        val repo = studioRepo.repos.value.firstOrNull { it.id == repoId } ?: return

        val userMsg = ChatMessage(
            id = "st_u_${System.currentTimeMillis()}",
            role = MessageRole.USER,
            content = clean,
            timestampLabel = "Just now",
            dateGroup = "Today"
        )
        studioRepo.appendProjectMessage(repoId, userMsg)

        val assistantId = "st_a_${System.currentTimeMillis() + 1}"
        val fullReply = MockRepos.generateStudioReply(repo, clean)
        val placeholder = ChatMessage(
            id = assistantId,
            role = MessageRole.ASSISTANT,
            content = "",
            timestampLabel = "Just now",
            dateGroup = "Today",
            modelName = "Studio Agent",
            isStreaming = true
        )
        studioRepo.appendProjectMessage(repoId, placeholder)

        projectStreamJob = viewModelScope.launch {
            _isProjectStreaming.value = true
            delay(600L)
            val builder = StringBuilder()
            var i = 0
            while (i < fullReply.length && isActive) {
                val end = (i + 8).coerceAtMost(fullReply.length)
                builder.append(fullReply.substring(i, end))
                val snap = builder.toString()
                studioRepo.updateProjectMessage(repoId, assistantId) {
                    it.copy(content = snap, isStreaming = true)
                }
                i = end
                delay(36L)
            }
            studioRepo.updateProjectMessage(repoId, assistantId) {
                it.copy(isStreaming = false)
            }
            _isProjectStreaming.value = false
        }
    }

    fun stopProjectStreaming(repoId: String) {
        projectStreamJob?.cancel()
        projectStreamJob = null
        _isProjectStreaming.value = false
        val repo = studioRepo.repos.value.firstOrNull { it.id == repoId } ?: return
        repo.chatMessages.filter { it.isStreaming }.forEach { msg ->
            studioRepo.updateProjectMessage(repoId, msg.id) { it.copy(isStreaming = false) }
        }
    }

    fun getFile(repoId: String, filePath: String) = studioRepo.getFile(repoId, filePath)

    fun saveMockFileEdit(repoId: String, filePath: String, content: String) {
        studioRepo.updateFileContent(repoId, filePath, content)
    }
}
