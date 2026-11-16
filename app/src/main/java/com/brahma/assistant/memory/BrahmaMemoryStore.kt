package com.brahma.assistant.memory

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.brahma.assistant.ai.preferences.dataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class BrahmaMemoryStore(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    private val memoryStoreKey = stringPreferencesKey("brahma_semantic_memory_store")

    val memoriesFlow: Flow<List<MemoryItem>> = context.dataStore.data.map { prefs ->
        val rawJson = prefs[memoryStoreKey].orEmpty()
        if (rawJson.isBlank()) {
            emptyList()
        } else {
            try {
                json.decodeFromString<List<MemoryItem>>(rawJson)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun saveMemory(item: MemoryItem): Unit = withContext(Dispatchers.IO) {
        context.dataStore.edit { prefs ->
            val currentList = try {
                val raw = prefs[memoryStoreKey].orEmpty()
                if (raw.isBlank()) mutableListOf() else json.decodeFromString<List<MemoryItem>>(raw).toMutableList()
            } catch (e: Exception) {
                mutableListOf()
            }

            val existingIndex = currentList.indexOfFirst { it.key.equals(item.key, ignoreCase = true) }
            if (existingIndex >= 0) {
                val existing = currentList[existingIndex]
                currentList[existingIndex] = item.copy(
                    accessCount = existing.accessCount + 1,
                    lastAccessed = System.currentTimeMillis()
                )
            } else {
                currentList.add(item)
            }

            // Cap memories to prevent memory bloat
            if (currentList.size > 200) {
                currentList.sortBy { it.lastAccessed }
                while (currentList.size > 200) {
                    currentList.removeAt(0)
                }
            }

            prefs[memoryStoreKey] = json.encodeToString(currentList)
        }
    }

    suspend fun findRelevantMemories(query: String): List<MemoryItem> = withContext(Dispatchers.IO) {
        val allMemories = memoriesFlow.first()
        val queryTokens = query.lowercase().split(" ", ",", ".", "?").filter { it.length > 2 }
        if (queryTokens.isEmpty()) return@withContext emptyList()

        allMemories.filter { item ->
            val text = "${item.key} ${item.value}".lowercase()
            queryTokens.any { token -> text.contains(token) }
        }.sortedByDescending { it.accessCount }
    }

    suspend fun clearAll(): Unit = withContext(Dispatchers.IO) {
        context.dataStore.edit { it.remove(memoryStoreKey) }
    }
}
