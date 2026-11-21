package com.bansify.ai.data.repo

import com.bansify.ai.core.designsystem.HealthStatus
import com.bansify.ai.data.mock.MockModels
import com.bansify.ai.data.mock.ModelItem
import com.bansify.ai.data.mock.ProviderGroup
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Single shared catalog repository consumed by both ModelSheet and ModelsScreen.
 */
class ModelRepository {
    private val _providers = MutableStateFlow(MockModels.initialProviders)
    val providers: StateFlow<List<ProviderGroup>> = _providers.asStateFlow()

    private val _selectedModelId = MutableStateFlow(MockModels.autoModel.id)
    val selectedModelId: StateFlow<String> = _selectedModelId.asStateFlow()

    val autoModel: ModelItem = MockModels.autoModel

    suspend fun fetchProviders(simulateError: Boolean = false): Result<List<ProviderGroup>> {
        delay(680L)
        return if (simulateError) {
            Result.failure(IllegalStateException("Provider health check timeout (mock)."))
        } else {
            Result.success(_providers.value)
        }
    }

    fun selectModel(modelId: String) {
        _selectedModelId.value = modelId
    }

    fun findModel(modelId: String): ModelItem {
        if (modelId == autoModel.id) return autoModel
        return _providers.value
            .asSequence()
            .flatMap { it.models.asSequence() }
            .firstOrNull { it.id == modelId }
            ?: autoModel
    }

    fun addCustomProvider(
        name: String,
        baseUrl: String,
        modelNamesRaw: String
    ): ProviderGroup {
        val providerId = "custom_${System.currentTimeMillis()}"
        val parsedModels = modelNamesRaw
            .split(",", "\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .ifEmpty { listOf("custom-model-1") }
            .mapIndexed { idx, modelName ->
                ModelItem(
                    id = "${providerId}_m$idx",
                    providerId = providerId,
                    name = modelName,
                    shortName = modelName.take(14),
                    isFree = false,
                    contextLength = "128K",
                    speedRating = 2,
                    status = HealthStatus.UP,
                    description = "Custom OpenAI-compatible endpoint ($baseUrl) — Demo entry.",
                    bestFor = listOf("Custom endpoint", "Demo"),
                    rateLimitNote = "Configured locally (mock — no network calls)."
                )
            }

        val group = ProviderGroup(
            id = providerId,
            name = name.trim(),
            monogram = name.trim().take(2).uppercase(),
            isCardFreeTier = false,
            setupNeeded = true,
            status = HealthStatus.UP,
            freeTierSummary = "Custom · Demo",
            quotaChips = listOf("Custom URL", "Demo"),
            resetHint = null,
            models = parsedModels
        )
        _providers.update { it + group }
        return group
    }
}
