package com.brahma.assistant.ai.client

import android.util.Log
import com.brahma.assistant.ai.model.Candidate
import com.brahma.assistant.ai.model.ContentBlock
import com.brahma.assistant.ai.model.GeminiChunk
import com.brahma.assistant.ai.model.GeminiGenerateContentRequest
import com.brahma.assistant.ai.model.GeminiResponse
import com.brahma.assistant.ai.model.GenerationConfig
import com.brahma.assistant.ai.model.InlineData
import com.brahma.assistant.ai.model.Part
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class GeminiMobileClient(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build(),
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
) {
    companion object {
        private const val TAG = "GeminiMobileClient"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
    }

    /**
     * Streams content chunks in real-time from Gemini using Server-Sent Events (SSE).
     */
    fun streamGenerateContent(
        apiKey: String,
        model: String = "gemini-1.5-flash",
        prompt: String,
        systemInstruction: String? = null,
        imageBase64: String? = null,
        mimeType: String = "image/jpeg"
    ): Flow<String> = flow {
        require(apiKey.isNotBlank()) { "Gemini API Key cannot be blank" }

        val parts = mutableListOf<Part>()
        parts.add(Part(text = prompt))
        if (!imageBase64.isNullOrBlank()) {
            parts.add(Part(inlineData = InlineData(mimeType = mimeType, data = imageBase64)))
        }

        val requestPayload = GeminiGenerateContentRequest(
            contents = listOf(ContentBlock(role = "user", parts = parts)),
            systemInstruction = systemInstruction?.let {
                ContentBlock(role = "system", parts = listOf(Part(text = it)))
            },
            generationConfig = GenerationConfig(
                temperature = 0.7f,
                maxOutputTokens = 4096
            )
        )

        val jsonBody = json.encodeToString(requestPayload)
        val url = "$BASE_URL/$model:streamGenerateContent?alt=sse&key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .addHeader("Accept", "text/event-stream")
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            val errorBody = response.body?.string().orEmpty()
            response.close()
            throw IllegalStateException("Gemini API Error [${response.code}]: $errorBody")
        }

        response.body?.byteStream()?.use { inputStream ->
            val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val currentLine = line ?: continue
                if (currentLine.startsWith("data: ")) {
                    val rawJson = currentLine.removePrefix("data: ").trim()
                    if (rawJson.isNotBlank()) {
                        try {
                            val chunk = json.decodeFromString<GeminiChunk>(rawJson)
                            val textPart = chunk.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                            if (!textPart.isNullOrEmpty()) {
                                emit(textPart)
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Error parsing SSE chunk: ${e.message}")
                        }
                    }
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Executes single-shot generation with structured JSON response guarantees.
     */
    suspend fun generateStructuredContent(
        apiKey: String,
        model: String = "gemini-1.5-flash",
        prompt: String,
        systemInstruction: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        NetworkResilience.executeWithRetry {
            require(apiKey.isNotBlank()) { "Gemini API Key cannot be blank" }

            val requestPayload = GeminiGenerateContentRequest(
                contents = listOf(ContentBlock(role = "user", parts = listOf(Part(text = prompt)))),
                systemInstruction = systemInstruction?.let {
                    ContentBlock(role = "system", parts = listOf(Part(text = it)))
                },
                generationConfig = GenerationConfig(
                    temperature = 0.2f, // Low temperature for deterministic output
                    responseMimeType = "application/json"
                )
            )

            val jsonBody = json.encodeToString(requestPayload)
            val url = "$BASE_URL/$model:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Gemini Error [${response.code}]: $responseBody")
            }

            val parsedResponse = json.decodeFromString<GeminiResponse>(responseBody)
            val outputText = parsedResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw IllegalStateException("Empty response from Gemini API")

            outputText
        }
    }
}
