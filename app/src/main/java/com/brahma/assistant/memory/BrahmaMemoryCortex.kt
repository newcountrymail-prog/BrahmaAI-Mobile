package com.brahma.assistant.memory

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BrahmaMemoryCortex(
    private val memoryStore: BrahmaMemoryStore,
    private val historyStore: BrahmaChatHistoryStore
) {
    companion object {
        private const val TAG = "BrahmaMemoryCortex"

        @Volatile
        private var INSTANCE: BrahmaMemoryCortex? = null

        fun getInstance(context: Context): BrahmaMemoryCortex {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BrahmaMemoryCortex(
                    BrahmaMemoryStore(context.applicationContext),
                    BrahmaChatHistoryStore(context.applicationContext)
                ).also { INSTANCE = it }
            }
        }
    }

    /**
     * Synthesizes user preferences, past relevant episodic facts, and rolling conversation history
     * into a unified prompt context prefix.
     */
    suspend fun buildEnrichedPrompt(rawPrompt: String): String = withContext(Dispatchers.IO) {
        val relevantMemories = memoryStore.findRelevantMemories(rawPrompt)
        val recentHistory = historyStore.getRecentContext(count = 6)

        val memoryBlock = if (relevantMemories.isNotEmpty()) {
            val facts = relevantMemories.joinToString("\n") { "- ${it.key}: ${it.value}" }
            """
            [RECALLED SEMANTIC MEMORY]
            $facts
            """.trimIndent()
        } else {
            ""
        }

        val historyBlock = if (recentHistory.isNotEmpty()) {
            val conv = recentHistory.joinToString("\n") { "${it.role}: ${it.text}" }
            """
            [RECENT CONVERSATION HISTORY]
            $conv
            """.trimIndent()
        } else {
            ""
        }

        buildString {
            if (memoryBlock.isNotBlank()) {
                appendLine(memoryBlock)
                appendLine()
            }
            if (historyBlock.isNotBlank()) {
                appendLine(historyBlock)
                appendLine()
            }
            appendLine("[CURRENT USER DIRECTIVE]")
            append(rawPrompt)
        }
    }

    /**
     * Inspects a conversation turn to automatically extract facts (e.g. "my name is...", "remember that...").
     */
    suspend fun analyzeAndConsolidate(userText: String, assistantText: String) = withContext(Dispatchers.IO) {
        try {
            val lower = userText.lowercase()
            if (lower.contains("remember that") || lower.contains("my name is") || lower.contains("i like") || lower.contains("i prefer")) {
                val key = "User Preference: " + userText.take(30)
                memoryStore.saveMemory(
                    MemoryItem(
                        key = key,
                        value = userText,
                        type = MemoryType.USER_PREFERENCE,
                        confidence = 0.95f
                    )
                )
                Log.d(TAG, "Consolidated new memory: $key")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Memory consolidation failed: ${e.message}")
        }
    }

    suspend fun recordManualMemory(key: String, value: String, type: MemoryType = MemoryType.FACT) {
        memoryStore.saveMemory(MemoryItem(key = key, value = value, type = type))
    }

    fun getMemoriesFlow() = memoryStore.memoriesFlow
    fun getHistoryFlow() = historyStore.historyFlow
}
