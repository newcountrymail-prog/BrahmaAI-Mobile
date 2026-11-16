package com.brahma.assistant.memory

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.brahma.assistant.ai.model.ChatMessage
import com.brahma.assistant.ai.preferences.dataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class BrahmaChatHistoryStore(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    private val historyKey = stringPreferencesKey("brahma_chat_history")

    val historyFlow: Flow<List<ChatMessage>> = context.dataStore.data.map { prefs ->
        val raw = prefs[historyKey].orEmpty()
        if (raw.isBlank()) {
            emptyList()
        } else {
            try {
                json.decodeFromString<List<ChatMessage>>(raw)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun appendMessage(message: ChatMessage): Unit = withContext(Dispatchers.IO) {
        context.dataStore.edit { prefs ->
            val list = try {
                val raw = prefs[historyKey].orEmpty()
                if (raw.isBlank()) mutableListOf() else json.decodeFromString<List<ChatMessage>>(raw).toMutableList()
            } catch (e: Exception) {
                mutableListOf()
            }

            list.add(message)
            // Keep last 50 messages to maintain lean payload sizes
            if (list.size > 50) {
                while (list.size > 50) {
                    list.removeAt(0)
                }
            }
            prefs[historyKey] = json.encodeToString(list)
        }
    }

    suspend fun getRecentContext(count: Int = 10): List<ChatMessage> = withContext(Dispatchers.IO) {
        val all = historyFlow.first()
        all.takeLast(count)
    }

    suspend fun clearHistory(): Unit = withContext(Dispatchers.IO) {
        context.dataStore.edit { it.remove(historyKey) }
    }
}
