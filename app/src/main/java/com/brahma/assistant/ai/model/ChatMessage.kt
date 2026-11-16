package com.brahma.assistant.ai.model

import kotlinx.serialization.Serializable

@Serializable
enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM,
    TOOL
}

@Serializable
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: MessageRole,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attachedImagePath: String? = null,
    val isStreaming: Boolean = false
)
