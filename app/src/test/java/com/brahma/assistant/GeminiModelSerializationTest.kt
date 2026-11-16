package com.brahma.assistant

import com.brahma.assistant.ai.model.Candidate
import com.brahma.assistant.ai.model.ContentBlock
import com.brahma.assistant.ai.model.GeminiGenerateContentRequest
import com.brahma.assistant.ai.model.GeminiResponse
import com.brahma.assistant.ai.model.GenerationConfig
import com.brahma.assistant.ai.model.Part
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiModelSerializationTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true }

    @Test
    fun testGenerateContentRequestEncoding() {
        val request = GeminiGenerateContentRequest(
            contents = listOf(
                ContentBlock(
                    role = "user",
                    parts = listOf(Part(text = "Execute system diagnosis"))
                )
            ),
            generationConfig = GenerationConfig(
                temperature = 0.5f,
                maxOutputTokens = 1024
            )
        )

        val encoded = json.encodeToString(request)
        assertTrue(encoded.contains("Execute system diagnosis"))
        assertTrue(encoded.contains("\"role\":\"user\""))
    }

    @Test
    fun testGeminiResponseDecoding() {
        val rawJson = """
            {
              "candidates": [
                {
                  "content": {
                    "role": "model",
                    "parts": [
                      {
                        "text": "Brahma AI neural core active."
                      }
                    ]
                  },
                  "finishReason": "STOP"
                }
              ]
            }
        """.trimIndent()

        val response = json.decodeFromString<GeminiResponse>(rawJson)
        assertNotNull(response.candidates)
        assertEquals(1, response.candidates?.size)
        assertEquals("Brahma AI neural core active.", response.candidates?.first()?.content?.parts?.first()?.text)
        assertEquals("STOP", response.candidates?.first()?.finishReason)
    }
}
