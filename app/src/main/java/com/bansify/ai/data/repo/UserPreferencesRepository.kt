package com.bansify.ai.data.repo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bansify.ai.core.designsystem.ThemeMode
import com.bansify.ai.data.mock.AppSettings
import com.bansify.ai.data.mock.MemoryFact
import com.bansify.ai.data.mock.MockUser
import com.bansify.ai.data.mock.ResponseStyle
import com.bansify.ai.data.mock.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

private val Context.bansifyDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "bansify_preferences"
)

class UserPreferencesRepository(context: Context) {
    private val dataStore = context.applicationContext.bansifyDataStore

    private object Keys {
        val NAME = stringPreferencesKey("user_name")
        val PROFESSION = stringPreferencesKey("user_profession")
        val GOALS = stringPreferencesKey("user_goals")
        val ABOUT = stringPreferencesKey("user_about")
        val IS_GUEST = booleanPreferencesKey("is_guest")
        val EMAIL = stringPreferencesKey("user_email")
        val ONBOARDED = booleanPreferencesKey("onboarding_completed")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val FONT_SCALE = floatPreferencesKey("font_scale")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val DEFAULT_MODEL = stringPreferencesKey("default_model_id")
        val AUTO_FALLBACK = booleanPreferencesKey("auto_fallback")
        val RESPONSE_STYLE = stringPreferencesKey("response_style")
        val STREAMING = booleanPreferencesKey("streaming_enabled")
        val SEND_ON_ENTER = booleanPreferencesKey("send_on_enter")
        val OFFLINE_DEMO = booleanPreferencesKey("offline_demo_mode")
    }

    val userProfile: Flow<UserProfile> = dataStore.data.map { prefs ->
        val goalsRaw = prefs[Keys.GOALS]
        val goals = when {
            goalsRaw == null -> listOf("Study", "Coding")
            goalsRaw.isBlank() -> emptyList()
            else -> goalsRaw.split("|").filter { it.isNotBlank() }
        }
        UserProfile(
            name = prefs[Keys.NAME] ?: "Pawan",
            profession = prefs[Keys.PROFESSION] ?: "Student",
            goals = goals,
            aboutYou = prefs[Keys.ABOUT] ?: UserProfile().aboutYou,
            isGuest = prefs[Keys.IS_GUEST] ?: false,
            email = prefs[Keys.EMAIL] ?: "pawan@bansify.ai",
            onboardingCompleted = prefs[Keys.ONBOARDED] ?: false
        )
    }

    val appSettings: Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            themeMode = prefs[Keys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            fontScale = prefs[Keys.FONT_SCALE] ?: 1.0f,
            dynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: false,
            defaultModelId = prefs[Keys.DEFAULT_MODEL] ?: "auto",
            autoFallback = prefs[Keys.AUTO_FALLBACK] ?: true,
            responseStyle = prefs[Keys.RESPONSE_STYLE]?.let {
                runCatching { ResponseStyle.valueOf(it) }.getOrNull()
            } ?: ResponseStyle.BALANCED,
            streamingEnabled = prefs[Keys.STREAMING] ?: true,
            sendOnEnter = prefs[Keys.SEND_ON_ENTER] ?: false,
            offlineDemoMode = prefs[Keys.OFFLINE_DEMO] ?: false
        )
    }

    private val _memoryFacts = MutableStateFlow(MockUser.initialMemoryFacts)
    val memoryFacts: StateFlow<List<MemoryFact>> = _memoryFacts.asStateFlow()

    suspend fun saveOnboarding(
        name: String,
        profession: String,
        goals: List<String>,
        isGuest: Boolean
    ) {
        dataStore.edit { prefs ->
            prefs[Keys.NAME] = if (isGuest) "" else name.trim().ifEmpty { "Pawan" }
            prefs[Keys.PROFESSION] = if (isGuest) "" else profession.trim()
            prefs[Keys.GOALS] = if (isGuest) "" else goals.take(3).joinToString("|")
            prefs[Keys.IS_GUEST] = isGuest
            prefs[Keys.EMAIL] = if (isGuest) "" else "pawan@bansify.ai"
            prefs[Keys.ONBOARDED] = true
        }
    }

    suspend fun updatePersona(
        name: String,
        profession: String,
        goals: List<String>,
        aboutYou: String
    ) {
        dataStore.edit { prefs ->
            prefs[Keys.NAME] = name.trim()
            prefs[Keys.PROFESSION] = profession.trim()
            prefs[Keys.GOALS] = goals.take(3).joinToString("|")
            prefs[Keys.ABOUT] = aboutYou.trim()
        }
    }

    suspend fun setGuestMode(isGuest: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.IS_GUEST] = isGuest
            if (!isGuest) {
                if (prefs[Keys.NAME].isNullOrBlank()) prefs[Keys.NAME] = "Pawan"
                prefs[Keys.EMAIL] = "pawan@bansify.ai"
            }
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setFontScale(scale: Float) {
        dataStore.edit { it[Keys.FONT_SCALE] = scale.coerceIn(0.85f, 1.4f) }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    suspend fun setDefaultModelId(modelId: String) {
        dataStore.edit { it[Keys.DEFAULT_MODEL] = modelId }
    }

    suspend fun setAutoFallback(enabled: Boolean) {
        dataStore.edit { it[Keys.AUTO_FALLBACK] = enabled }
    }

    suspend fun setResponseStyle(style: ResponseStyle) {
        dataStore.edit { it[Keys.RESPONSE_STYLE] = style.name }
    }

    suspend fun setStreamingEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.STREAMING] = enabled }
    }

    suspend fun setSendOnEnter(enabled: Boolean) {
        dataStore.edit { it[Keys.SEND_ON_ENTER] = enabled }
    }

    suspend fun setOfflineDemoMode(enabled: Boolean) {
        dataStore.edit { it[Keys.OFFLINE_DEMO] = enabled }
    }

    fun deleteMemoryFact(id: String) {
        _memoryFacts.update { list -> list.filterNot { it.id == id } }
    }

    fun addMemoryFact(category: String, text: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        _memoryFacts.update { list ->
            listOf(MemoryFact("m_${System.currentTimeMillis()}", category, clean)) + list
        }
    }

    fun clearAllMemory() {
        _memoryFacts.value = emptyList()
    }

    /**
     * Deletes everything this app stored for the account.
     *
     * `clear()` removes every key, not just `about`: a "Delete all data" button
     * that leaves the name, email, profession, goals and the onboarding flag
     * behind is a privacy bug, not a UI detail.
     */
    suspend fun resetAllData() {
        _memoryFacts.value = emptyList()
        dataStore.edit { prefs -> prefs.clear() }
    }
}
