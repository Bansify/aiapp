package com.bansify.ai.data.repo

import com.bansify.ai.core.designsystem.HealthStatus
import com.bansify.ai.data.ai.AiKeySource
import com.bansify.ai.data.ai.AiModel
import com.bansify.ai.data.ai.AiProvider
import com.bansify.ai.data.ai.AiProviders
import com.bansify.ai.data.ai.ApiKeyStore
import com.bansify.ai.data.ai.Wire
import com.bansify.ai.data.mock.ModelItem
import com.bansify.ai.data.mock.ProviderGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** A concrete call target: which provider, which model id and which key to use. */
data class ModelRoute(
    val provider: AiProvider,
    val apiModel: String,
    val apiKey: String
)

/**
 * Live model catalogue. It is built from [AiProviders] plus any custom OpenAI-compatible
 * endpoint the user adds, and it knows which providers already have a saved key.
 */
class ModelRepository(
    private val keyStore: ApiKeyStore
) : AiKeySource {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _custom = MutableStateFlow<List<AiProvider>>(emptyList())
    private val _selectedModelId = MutableStateFlow(AUTO_ID)

    override val keys: StateFlow<Map<String, String>> = keyStore.keys
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    val providers: StateFlow<List<ProviderGroup>> = combine(
        _custom,
        keys,
        _selectedModelId
    ) { custom, keyMap, _ ->
        (AiProviders.all + custom).map { provider -> provider.toGroup(keyMap.containsKey(provider.id)) }
    }.stateIn(scope, SharingStarted.Eagerly, AiProviders.all.map { it.toGroup(false) })

    val selectedModelId: StateFlow<String> = _selectedModelId

    val autoModel: ModelItem = autoModelItem()

    suspend fun fetchProviders(simulateError: Boolean = false): Result<List<ProviderGroup>> =
        if (simulateError) {
            Result.failure(IllegalStateException("Could not refresh providers."))
        } else {
            Result.success(providers.value)
        }

    fun selectModel(modelId: String) {
        _selectedModelId.value = modelId
    }

    fun findModel(modelId: String): ModelItem {
        if (modelId == AUTO_ID) return autoModel
        return providers.value
            .asSequence()
            .flatMap { it.models.asSequence() }
            .firstOrNull { it.id == modelId }
            ?: autoModel
    }

    /** Resolves the model the user picked into a provider, an API model id and a key. */
    fun resolve(modelId: String): ModelRoute? {
        if (modelId == AUTO_ID) return resolveAuto()
        val parsed = AiProviders.parseModelKey(modelId) ?: return null
        val provider = providerById(parsed.first) ?: return null
        val key = keys.value[provider.id].orEmpty()
        if (provider.requiresKey && key.isBlank()) return null
        return ModelRoute(provider, parsed.second, key)
    }

    /** First provider (in catalogue order) that has a saved key. */
    fun resolveAuto(): ModelRoute? {
        val keyMap = keys.value
        for (provider in AiProviders.all + _custom.value) {
            val key = keyMap[provider.id].orEmpty()
            if (!provider.requiresKey || key.isNotBlank()) {
                val model = provider.models.firstOrNull() ?: continue
                return ModelRoute(provider, model.id, key)
            }
        }
        return null
    }

    fun providerById(id: String): AiProvider? =
        (AiProviders.all + _custom.value).firstOrNull { it.id == id }

    suspend fun saveKey(providerId: String, key: String) {
        keyStore.save(providerId, key)
    }

    suspend fun clearKey(providerId: String) {
        keyStore.clear(providerId)
    }

    fun addCustomProvider(name: String, baseUrl: String, modelNamesRaw: String): ProviderGroup {
        val providerId = "custom_${System.currentTimeMillis()}"
        val models = modelNamesRaw
            .split(",", "\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .ifEmpty { listOf("default-model") }
            .mapIndexed { index, modelName ->
                AiModel(
                    id = modelName,
                    label = modelName,
                    context = "custom",
                    note = "Custom endpoint · model #$index",
                    free = false
                )
            }
        val provider = AiProvider(
            id = providerId,
            name = name.trim().ifBlank { "Custom provider" },
            monogram = name.trim().take(2).uppercase().ifBlank { "CU" },
            wire = Wire.OPENAI,
            baseUrl = baseUrl.trim().removeSuffix("/"),
            keyLabel = "API key",
            keyUrl = baseUrl.trim(),
            freeTier = "Custom OpenAI-compatible endpoint.",
            models = models
        )
        _custom.value = _custom.value + provider
        return provider.toGroup(keys.value.containsKey(provider.id))
    }

    private fun AiProvider.toGroup(hasKey: Boolean): ProviderGroup {
        val ready = !requiresKey || hasKey
        return ProviderGroup(
        id = id,
        name = name,
        monogram = monogram,
        isCardFreeTier = true,
        setupNeeded = !ready,
        status = if (ready) HealthStatus.UP else HealthStatus.SLOW,
        freeTierSummary = if (!requiresKey) "Built in" else if (hasKey) "Key saved" else "Add your key",
        quotaChips = listOfNotNull(
            freeTier.substringBefore('.').takeIf { it.isNotBlank() }
        ),
        resetHint = null,
        models = models.map { model ->
            ModelItem(
                id = AiProviders.modelKey(id, model.id),
                providerId = id,
                name = model.label,
                shortName = model.label,
                isFree = model.free,
                contextLength = model.context,
                speedRating = if (model.free) 3 else 2,
                status = if (ready) HealthStatus.UP else HealthStatus.SLOW,
                description = model.note,
                bestFor = listOf(name),
                rateLimitNote = if (ready) freeTier else "Add a ${keyLabel.lowercase()} in AI providers."
            )
        }
        )
    }

    private fun autoModelItem(): ModelItem = ModelItem(
        id = AUTO_ID,
        providerId = "auto",
        name = "Auto (recommended)",
        shortName = "Auto",
        isFree = true,
        contextLength = "Smart routing",
        speedRating = 3,
        status = HealthStatus.UP,
        description = "Uses the first provider you have a key for, in catalogue order.",
        bestFor = listOf("Daily chat", "Zero setup"),
        rateLimitNote = "Saves a key for any provider to enable Auto."
    )

    companion object {
        const val AUTO_ID = "auto"
    }
}
