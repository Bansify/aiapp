package com.bansify.ai.nav

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object Splash : Route

    @Serializable
    data object OnboardingWelcome : Route

    @Serializable
    data object OnboardingName : Route

    @Serializable
    data object OnboardingProfession : Route

    @Serializable
    data object OnboardingGoals : Route

    @Serializable
    data object Chat : Route

    @Serializable
    data object History : Route

    @Serializable
    data object Library : Route

    @Serializable
    data object Scheduled : Route

    @Serializable
    data object Models : Route

    @Serializable
    data object StudioHome : Route

    @Serializable
    data class StudioRepo(val repoId: String) : Route

    @Serializable
    data class StudioFileViewer(val repoId: String, val filePath: String) : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data object SettingsAppearance : Route

    @Serializable
    data object SettingsPersona : Route

    @Serializable
    data object SettingsMemory : Route

    @Serializable
    data object SettingsData : Route

    @Serializable
    data object SettingsAbout : Route

    @Serializable
    data object Profile : Route

    @Serializable
    data object Login : Route

    @Serializable
    data object Account : Route

    @Serializable
    data object Providers : Route
}
