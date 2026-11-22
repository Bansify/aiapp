package com.bansify.ai.data.ai

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.providerKeyStore: DataStore<Preferences> by preferencesDataStore(
    name = "bansify_provider_keys"
)

/**
 * Stores one API key per provider on the device. Keys never leave the device except in a
 * request to that provider. Move this to EncryptedSharedPreferences or the Keystore before
 * shipping a release build.
 */
class ApiKeyStore(context: Context) {
    private val dataStore = context.applicationContext.providerKeyStore

    val keys: Flow<Map<String, String>> = dataStore.data.map { prefs ->
        prefs.asMap()
            .filterKeys { it.name.startsWith(KEY_PREFIX) }
            .mapKeys { (pref, _) -> pref.name.removePrefix(KEY_PREFIX) }
            .mapValues { (_, value) -> value.toString() }
            .filterValues { it.isNotBlank() }
    }

    suspend fun save(providerId: String, key: String) {
        dataStore.edit { prefs ->
            val clean = key.trim()
            if (clean.isEmpty()) prefs.remove(stringPreferencesKey(KEY_PREFIX + providerId))
            else prefs[stringPreferencesKey(KEY_PREFIX + providerId)] = clean
        }
    }

    suspend fun clear(providerId: String) {
        dataStore.edit { prefs -> prefs.remove(stringPreferencesKey(KEY_PREFIX + providerId)) }
    }

    private companion object {
        const val KEY_PREFIX = "provider_key_"
    }
}
