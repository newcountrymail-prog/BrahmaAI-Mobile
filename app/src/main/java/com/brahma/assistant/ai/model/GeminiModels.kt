package com.brahma.assistant.ai.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeminiGenerateContentRequest(
    val contents: List<ContentBlock>,
    val systemInstruction: ContentBlock? = null,
    val generationConfig: GenerationConfig? = null,
    val safetySettings: List<SafetySetting>? = null
)

@Serializable
data class ContentBlock(
    val role: String = "user",
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String? = null,
    @SerialName("inline_data")
    val inlineData: InlineData? = null
)

@Serializable
data class InlineData(
    @SerialName("mime_type")
    val mimeType: String,
    val data: String // Base64 encoded payload
)

@Serializable
data class GenerationConfig(
    val temperature: Float? = 0.7f,
    val topP: Float? = 0.95f,
    val topK: Int? = 40,
    val maxOutputTokens: Int? = 4096,
    val responseMimeType: String? = null
)

@Serializable
data class SafetySetting(
    val category: String,
    val threshold: String
)

@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>? = null,
    val promptFeedback: PromptFeedback? = null,
    val usageMetadata: UsageMetadata? = null
)

@Serializable
data class Candidate(
    val content: ContentBlock? = null,
    val finishReason: String? = null,
    val index: Int? = 0
)

@Serializable
data class PromptFeedback(
    val blockReason: String? = null
)

@Serializable
data class UsageMetadata(
    val promptTokenCount: Int? = 0,
    val candidatesTokenCount: Int? = 0,
    val totalTokenCount: Int? = 0
)

@Serializable
data class GeminiChunk(
    val candidates: List<Candidate>? = null
)
