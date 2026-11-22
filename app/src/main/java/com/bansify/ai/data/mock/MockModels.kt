package com.bansify.ai.data.mock

import com.bansify.ai.core.designsystem.HealthStatus

/**
 * A model the user can pick in the chat composer.
 *
 * [id] is the catalogue key `providerId::apiModelId` built by
 * [com.bansify.ai.data.repo.ModelRepository], so it can be resolved back to a real
 * provider call without keeping a second lookup table.
 */
data class ModelItem(
    val id: String,
    val providerId: String,
    val name: String,
    val shortName: String,
    val isFree: Boolean,
    val contextLength: String,
    val speedRating: Int, // 1..3 dots
    val status: HealthStatus,
    val description: String,
    val bestFor: List<String>,
    val rateLimitNote: String
)

/** One provider section in the model sheet and the models screen. */
data class ProviderGroup(
    val id: String,
    val name: String,
    val monogram: String,
    val isCardFreeTier: Boolean,
    val setupNeeded: Boolean,
    val status: HealthStatus,
    val freeTierSummary: String,
    val quotaChips: List<String>,
    val resetHint: String?,
    val models: List<ModelItem>
)
