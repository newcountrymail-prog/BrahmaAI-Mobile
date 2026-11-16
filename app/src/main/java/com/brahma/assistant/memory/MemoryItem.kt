package com.brahma.assistant.memory

import kotlinx.serialization.Serializable

@Serializable
enum class MemoryType {
    USER_PREFERENCE,
    FACT,
    PROCEDURAL_SKILL,
    EPISODIC_EVENT,
    DEVICE_CONTEXT
}

@Serializable
data class MemoryItem(
    val key: String,
    val value: String,
    val type: MemoryType = MemoryType.FACT,
    val confidence: Float = 1.0f,
    val lastAccessed: Long = System.currentTimeMillis(),
    val accessCount: Int = 1
)
