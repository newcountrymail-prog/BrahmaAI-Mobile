package com.brahma.assistant.code

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class CodeLanguage {
    KOTLIN,
    PYTHON,
    JAVASCRIPT,
    TYPESCRIPT,
    BASH,
    JSON,
    HTML,
    SQL,
    UNKNOWN
}

@Serializable
data class CodeArtifact(
    val id: String = UUID.randomUUID().toString(),
    val filename: String,
    val language: CodeLanguage,
    val sourceCode: String,
    val executionOutput: String? = null,
    val executionError: String? = null,
    val executionDurationMs: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
)
