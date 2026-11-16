package com.brahma.assistant.ai.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "brahma_preferences")

class PreferencesManager(private val context: Context) {

    companion object {
        val KEY_GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val KEY_OPENROUTER_API_KEY = stringPreferencesKey("openrouter_api_key")
        val KEY_SELECTED_MODEL = stringPreferencesKey("selected_model")
        val KEY_SENTINEL_ENABLED = booleanPreferencesKey("sentinel_enabled")
        val KEY_FLOATING_ORB_ENABLED = booleanPreferencesKey("floating_orb_enabled")
        val KEY_PROACTIVE_MODE = booleanPreferencesKey("proactive_mode")
        val KEY_VOICE_FEEDBACK = booleanPreferencesKey("voice_feedback")

        const val DEFAULT_MODEL = "gemini-1.5-flash"
    }

    val geminiApiKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_GEMINI_API_KEY] ?: ""
    }

    val openRouterApiKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_OPENROUTER_API_KEY] ?: ""
    }

    val selectedModel: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_SELECTED_MODEL] ?: DEFAULT_MODEL
    }

    val isSentinelEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_SENTINEL_ENABLED] ?: false
    }

    val isFloatingOrbEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_FLOATING_ORB_ENABLED] ?: true
    }

    val isProactiveModeEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_PROACTIVE_MODE] ?: true
    }

    val isVoiceFeedbackEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_VOICE_FEEDBACK] ?: true
    }

    suspend fun setGeminiApiKey(key: String) {
        context.dataStore.edit { it[KEY_GEMINI_API_KEY] = key.trim() }
    }

    suspend fun setOpenRouterApiKey(key: String) {
        context.dataStore.edit { it[KEY_OPENROUTER_API_KEY] = key.trim() }
    }

    suspend fun setSelectedModel(model: String) {
        context.dataStore.edit { it[KEY_SELECTED_MODEL] = model }
    }

    suspend fun setSentinelEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SENTINEL_ENABLED] = enabled }
    }

    suspend fun setFloatingOrbEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_FLOATING_ORB_ENABLED] = enabled }
    }

    suspend fun setProactiveMode(enabled: Boolean) {
        context.dataStore.edit { it[KEY_PROACTIVE_MODE] = enabled }
    }

    suspend fun setVoiceFeedback(enabled: Boolean) {
        context.dataStore.edit { it[KEY_VOICE_FEEDBACK] = enabled }
    }
}
