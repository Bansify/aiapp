package com.bansify.ai.data.repo

import com.bansify.ai.auth.AiAuth
import com.bansify.ai.auth.AiAccessTokenProvider
import com.bansify.ai.core.designsystem.HealthStatus
import com.bansify.ai.data.mock.MockModels
import com.bansify.ai.data.mock.ModelItem
import com.bansify.ai.data.mock.ProviderGroup
import com.bansify.ai.data.remote.AiApiClient
import com.bansify.ai.data.remote.RemoteModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * The model catalog, loaded from the Bansify AI Worker.
 *
 * The worker is the source of truth: which providers exist, which models each one
 * serves and whether they cost money. Picking a model here also picks its
 * provider, because the worker routes an explicit model only to the provider that
 * owns it. `autoModel` stays first: it means "let the free chain decide".
 */
class ModelRepository(
    auth: AiAccessTokenProvider = AiAuth(),
    private val api: AiApiClient = AiApiClient(auth),
) {
    private val _providers = MutableStateFlow(MockModels.initialProviders)
    val providers: StateFlow<List<ProviderGroup>> = _providers.asStateFlow()

    private val _selectedModelId = MutableStateFlow(MockModels.autoModel.id)
    val selectedModelId: StateFlow<String> = _selectedModelId.asStateFlow()

    val autoModel: ModelItem = MockModels.autoModel

    /** Reloads the catalog from the worker. */
    suspend fun loadFromWorker(): Result<List<ProviderGroup>> =
        api.fetchModels().map { models ->
            val groups = models
                .groupBy { it.providerId }
                .map { (providerId, list) -> list.toProviderGroup(providerId) }
                .sortedByDescending { it.models.count { model -> model.isFree } }
            if (groups.isNotEmpty()) _providers.value = groups
            groups
        }

    suspend fun fetchProviders(simulateError: Boolean = false): Result<List<ProviderGroup>> {
        if (simulateError) {
            return Result.failure(IllegalStateException("Provider health check failed."))
        }
        val remote = loadFromWorker()
        return remote.fold(
            onSuccess = { Result.success(it) },
            // Offline: keep showing the last catalog we had instead of an error.
            onFailure = { Result.success(_providers.value) },
        )
    }

    private fun List<RemoteModel>.toProviderGroup(providerId: String): ProviderGroup {
        val meta = PROVIDER_META[providerId]
        val models = map { remote ->
            ModelItem(
                id = remote.id,
                providerId = remote.providerId,
                name = remote.displayName,
                shortName = remote.displayName,
                isFree = remote.isFree == 1,
                contextLength = formatContext(remote.contextLen),
                speedRating = if (remote.speedClass == "instant") 3 else 2,
                status = HealthStatus.UP,
                description = if (remote.isFree == 1) "Free tier on ${meta?.name ?: providerId}." else "Paid model on ${meta?.name ?: providerId}.",
                bestFor = if (remote.isFree == 1) listOf("Daily chat", "Free") else listOf("Best quality"),
                rateLimitNote = if (remote.isFree == 1) "Free tier limits apply" else "Paid — only when you pick it",
            )
        }
        return ProviderGroup(
            id = providerId,
            name = meta?.name ?: providerId,
            monogram = meta?.monogram ?: providerId.take(1).uppercase(),
            isCardFreeTier = models.any { it.isFree },
            setupNeeded = false,
            status = HealthStatus.UP,
            freeTierSummary = if (models.any { it.isFree }) "Free tier available" else "Paid provider",
            quotaChips = emptyList(),
            resetHint = null,
            models = models.sortedByDescending { it.isFree },
        )
    }

    private fun formatContext(tokens: Long): String = when {
        tokens <= 0 -> "—"
        tokens >= 1_000_000 -> "${tokens / 1_000_000}M"
        tokens >= 1_000 -> "${tokens / 1_000}K"
        else -> "$tokens"
    }

    private data class ProviderMeta(val name: String, val monogram: String)

    private companion object {
        /** Display names for the providers the worker ships with. */
        val PROVIDER_META: Map<String, ProviderMeta> = mapOf(
            "groq" to ProviderMeta("Groq", "G"),
            "gemini" to ProviderMeta("Google Gemini", "G"),
            "cloudflare-ai" to ProviderMeta("Cloudflare Workers AI", "C"),
            "zai" to ProviderMeta("Z.ai (GLM)", "Z"),
            "openrouter" to ProviderMeta("OpenRouter", "O"),
            "sambanova" to ProviderMeta("SambaNova", "S"),
            "sarvam" to ProviderMeta("Sarvam AI · paid", "S"),
        )
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
