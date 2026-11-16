package com.brahma.assistant.ai.model

sealed interface AIState {
    data object Idle : AIState
    data object Listening : AIState
    data class Thinking(val prompt: String) : AIState
    data class Streaming(val partialResponse: String) : AIState
    data class ExecutingPlan(val stepDescription: String, val progress: Float) : AIState
    data class Success(val response: String) : AIState
    data class Error(val message: String, val canRetry: Boolean = true) : AIState
}
