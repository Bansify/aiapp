package com.bansify.ai.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bansify.ai.data.mock.Conversation
import com.bansify.ai.data.repo.ChatRepository
import com.bansify.ai.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class HistoryFilter(val label: String) {
    ALL("All"),
    PINNED("Pinned"),
    SHARED("Shared")
}

data class HistoryUiData(
    val conversations: List<Conversation>,
    val query: String,
    val filter: HistoryFilter,
    val selectedIds: Set<String>
)

class HistoryViewModel(
    private val chatRepo: ChatRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)
    private val _query = MutableStateFlow("")
    private val _filter = MutableStateFlow(HistoryFilter.ALL)
    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())

    private val _uiState = MutableStateFlow<UiState<HistoryUiData>>(UiState.Loading)
    val uiState: StateFlow<UiState<HistoryUiData>> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                chatRepo.conversations,
                _isLoading,
                _error,
                _query,
                _filter,
                _selectedIds
            ) { values ->
                @Suppress("UNCHECKED_CAST")
                val all = values[0] as List<Conversation>
                val loading = values[1] as Boolean
                val error = values[2] as String?
                val query = values[3] as String
                val filter = values[4] as HistoryFilter
                @Suppress("UNCHECKED_CAST")
                val selected = values[5] as Set<String>

                when {
                    loading -> UiState.Loading
                    error != null -> UiState.Error(error)
                    else -> {
                        val q = query.trim().lowercase()
                        val filtered = all.filter { conv ->
                            val matchesFilter = when (filter) {
                                HistoryFilter.ALL -> true
                                HistoryFilter.PINNED -> conv.isPinned
                                HistoryFilter.SHARED -> conv.isShared
                            }
                            val matchesQuery = q.isEmpty() ||
                                conv.title.lowercase().contains(q) ||
                                conv.preview.lowercase().contains(q)
                            matchesFilter && matchesQuery
                        }
                        if (filtered.isEmpty() && all.isEmpty()) {
                            UiState.Empty
                        } else {
                            UiState.Success(
                                HistoryUiData(
                                    conversations = filtered,
                                    query = query,
                                    filter = filter,
                                    selectedIds = selected
                                )
                            )
                        }
                    }
                }
            }.collect { _uiState.value = it }
        }
        refresh(simulateError = false)
    }

    fun refresh(simulateError: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = chatRepo.loadConversations(simulateError)
            _isLoading.value = false
            result.onFailure { err -> _error.value = err.message ?: "Load fail" }
        }
    }

    fun onQueryChange(q: String) {
        _query.value = q
    }

    fun onFilterChange(filter: HistoryFilter) {
        _filter.value = filter
    }

    fun toggleSelection(id: String) {
        _selectedIds.update { set ->
            if (id in set) set - id else set + id
        }
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun deleteConversation(id: String): Conversation? = chatRepo.deleteConversation(id)

    fun undoDelete() = chatRepo.undoDelete()

    fun togglePin(id: String) = chatRepo.togglePin(id)

    fun renameConversation(id: String, title: String) = chatRepo.renameConversation(id, title)

    fun duplicateConversation(id: String) = chatRepo.duplicateConversation(id)

    fun deleteSelected() {
        val ids = _selectedIds.value
        if (ids.isEmpty()) return
        chatRepo.deleteMultiple(ids)
        clearSelection()
    }

    fun pinSelected() {
        val ids = _selectedIds.value
        if (ids.isEmpty()) return
        chatRepo.pinMultiple(ids)
        clearSelection()
    }
}
