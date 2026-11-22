package com.bansify.ai.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.accountStore: DataStore<Preferences> by preferencesDataStore(name = "bansify_accounts")

@Serializable
data class AccountSession(
    val userId: String,
    val username: String,
    val email: String,
    val firstName: String = "",
    val lastName: String = "",
    val avatarUrl: String? = null,
    val accessToken: String = "",
    val refreshToken: String? = null,
    val expiresAtMillis: Long = 0L
) {
    val displayName: String
        get() = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")
            .ifBlank { username.ifBlank { email.substringBefore('@') } }

    val initial: String get() = displayName.trim().take(1).uppercase().ifBlank { "?" }
}

/** Keeps up to six signed-in Gothwad accounts on the device, exactly like Mail does. */
class AccountStore(context: Context) {
    private val dataStore = context.applicationContext.accountStore
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun read(): Pair<List<AccountSession>, String?> {
        val prefs = dataStore.data.first()
        val raw = prefs[ACCOUNTS] ?: return emptyList<AccountSession>() to null
        val list = runCatching { json.decodeFromString<List<AccountSession>>(raw) }.getOrDefault(emptyList())
        return list to prefs[ACTIVE]
    }

    suspend fun write(accounts: List<AccountSession>, activeUserId: String?) {
        dataStore.edit { prefs ->
            prefs[ACCOUNTS] = json.encodeToString(accounts.take(MAX_ACCOUNTS))
            if (activeUserId == null) prefs.remove(ACTIVE) else prefs[ACTIVE] = activeUserId
        }
    }

    private companion object {
        val ACCOUNTS = stringPreferencesKey("accounts_json")
        val ACTIVE = stringPreferencesKey("active_user_id")
        const val MAX_ACCOUNTS = 6
    }
}
