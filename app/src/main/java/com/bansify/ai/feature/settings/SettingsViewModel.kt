package com.bansify.ai.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bansify.ai.core.designsystem.ThemeMode
import com.bansify.ai.data.mock.AppSettings
import com.bansify.ai.data.mock.MemoryFact
import com.bansify.ai.data.mock.ResponseStyle
import com.bansify.ai.data.mock.UserProfile
import com.bansify.ai.data.repo.ChatRepository
import com.bansify.ai.data.repo.ModelRepository
import com.bansify.ai.data.repo.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val prefsRepo: UserPreferencesRepository,
    private val chatRepo: ChatRepository,
    private val modelRepo: ModelRepository
) : ViewModel() {

    val userProfile: StateFlow<UserProfile> = prefsRepo.userProfile.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        UserProfile()
    )

    val appSettings: StateFlow<AppSettings> = prefsRepo.appSettings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AppSettings()
    )

    val memoryFacts: StateFlow<List<MemoryFact>> = prefsRepo.memoryFacts
    val conversations = chatRepo.conversations

    fun modelNameFor(modelId: String): String = modelRepo.findModel(modelId).name

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { prefsRepo.setThemeMode(mode) }
    }

    fun setFontScale(scale: Float) {
        viewModelScope.launch { prefsRepo.setFontScale(scale) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setDynamicColor(enabled) }
    }

    fun setAutoFallback(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setAutoFallback(enabled) }
    }

    fun setResponseStyle(style: ResponseStyle) {
        viewModelScope.launch { prefsRepo.setResponseStyle(style) }
    }

    fun setStreamingEnabled(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setStreamingEnabled(enabled) }
    }

    fun setSendOnEnter(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setSendOnEnter(enabled) }
    }

    fun setGuestMode(isGuest: Boolean) {
        viewModelScope.launch { prefsRepo.setGuestMode(isGuest) }
    }

    fun updatePersona(name: String, profession: String, goals: List<String>, aboutYou: String) {
        viewModelScope.launch {
            prefsRepo.updatePersona(name, profession, goals, aboutYou)
        }
    }

    fun deleteMemoryFact(id: String) {
        prefsRepo.deleteMemoryFact(id)
    }

    fun clearAllMemory() {
        prefsRepo.clearAllMemory()
    }

    fun deleteAllUserData() {
        viewModelScope.launch {
            chatRepo.deleteAllConversations()
            prefsRepo.resetAllData()
        }
    }
}
