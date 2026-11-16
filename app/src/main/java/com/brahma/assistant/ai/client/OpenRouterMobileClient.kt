package com.brahma.assistant.ai.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

@Serializable
data class OpenRouterRequest(
    val model: String,
    val messages: List<OpenRouterMessage>,
    val temperature: Float = 0.7f
)

@Serializable
data class OpenRouterMessage(
    val role: String,
    val content: String
)

@Serializable
data class OpenRouterResponse(
    val choices: List<OpenRouterChoice>? = null,
    val error: OpenRouterError? = null
)

@Serializable
data class OpenRouterChoice(
    val message: OpenRouterMessage? = null,
    @SerialName("finish_reason")
    val finishReason: String? = null
)

@Serializable
data class OpenRouterError(
    val message: String? = null,
    val code: Int? = null
)

class OpenRouterMobileClient(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build(),
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
) {
    companion object {
        private const val API_URL = "https://openrouter.ai/api/v1/chat/completions"
    }

    suspend fun chatCompletion(
        apiKey: String,
        model: String = "anthropic/claude-3.5-sonnet",
        messages: List<OpenRouterMessage>
    ): Result<String> = withContext(Dispatchers.IO) {
        NetworkResilience.executeWithRetry {
            require(apiKey.isNotBlank()) { "OpenRouter API Key cannot be blank" }

            val payload = OpenRouterRequest(
                model = model,
                messages = messages
            )

            val request = Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("HTTP-Referer", "https://brahma.ai")
                .addHeader("X-Title", "Brahma AI Mobile")
                .post(json.encodeToString(payload).toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("OpenRouter Error [${response.code}]: $raw")
            }

            val decoded = json.decodeFromString<OpenRouterResponse>(raw)
            decoded.choices?.firstOrNull()?.message?.content
                ?: throw IllegalStateException(decoded.error?.message ?: "Empty OpenRouter response")
        }
    }
}
