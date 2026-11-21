package com.bansify.ai.feature.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bansify.ai.core.designsystem.HealthStatus
import com.bansify.ai.data.mock.ProviderGroup
import com.bansify.ai.data.repo.ModelRepository
import com.bansify.ai.data.repo.UserPreferencesRepository
import com.bansify.ai.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

enum class ModelFilter(val label: String) {
    ALL("All"),
    FREE("Free"),
    PAID("Paid"),
    HEALTHY("Healthy")
}

data class ModelsUiData(
    val providers: List<ProviderGroup>,
    val selectedModelId: String,
    val defaultModelId: String,
    val filter: ModelFilter,
    val query: String
)

class ModelsViewModel(
    private val modelRepo: ModelRepository,
    private val prefsRepo: UserPreferencesRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)
    private val _filter = MutableStateFlow(ModelFilter.ALL)
    private val _query = MutableStateFlow("")

    private val _uiState = MutableStateFlow<UiState<ModelsUiData>>(UiState.Loading)
    val uiState: StateFlow<UiState<ModelsUiData>> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                modelRepo.providers,
                modelRepo.selectedModelId,
                prefsRepo.appSettings,
                _isLoading,
                _error,
                _filter,
                _query
            ) { values ->
                @Suppress("UNCHECKED_CAST")
                val providers = values[0] as List<ProviderGroup>
                val selectedId = values[1] as String
                val settings = values[2] as com.bansify.ai.data.mock.AppSettings
                val loading = values[3] as Boolean
                val error = values[4] as String?
                val filter = values[5] as ModelFilter
                val query = values[6] as String

                when {
                    loading -> UiState.Loading
                    error != null -> UiState.Error(error)
                    else -> {
                        val q = query.trim().lowercase()
                        val filtered = providers.mapNotNull { group ->
                            val matchesProvider = q.isEmpty() || group.name.lowercase().contains(q)
                            val models = group.models.filter { m ->
                                val matchesFilter = when (filter) {
                                    ModelFilter.ALL -> true
                                    ModelFilter.FREE -> m.isFree
                                    ModelFilter.PAID -> !m.isFree
                                    ModelFilter.HEALTHY -> m.status == HealthStatus.UP
                                }
                                val matchesSearch = matchesProvider || m.name.lowercase().contains(q)
                                matchesFilter && matchesSearch
                            }
                            if (models.isEmpty()) null else group.copy(models = models)
                        }
                        UiState.Success(
                            ModelsUiData(
                                providers = filtered,
                                selectedModelId = selectedId,
                                defaultModelId = settings.defaultModelId,
                                filter = filter,
                                query = query
                            )
                        )
                    }
                }
            }.collect { _uiState.value = it }
        }
        refresh(false)
    }

    fun refresh(simulateError: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val res = modelRepo.fetchProviders(simulateError)
            _isLoading.value = false
            res.onFailure { _error.value = it.message ?: "Unable to load provider status." }
        }
    }

    fun onFilterChange(filter: ModelFilter) {
        _filter.value = filter
    }

    fun onQueryChange(query: String) {
        _query.value = query
    }

    fun selectModel(modelId: String) {
        modelRepo.selectModel(modelId)
    }

    fun setDefaultModel(modelId: String) {
        modelRepo.selectModel(modelId)
        viewModelScope.launch {
            prefsRepo.setDefaultModelId(modelId)
        }
    }

    fun addCustomProvider(name: String, baseUrl: String, modelsRaw: String) {
        modelRepo.addCustomProvider(name, baseUrl, modelsRaw)
    }
}
