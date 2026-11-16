package com.brahma.assistant.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.util.Locale

class SentenceChunker {
    private val buffer = StringBuilder()
    private val sentenceDelimiters = charArrayOf('.', '!', '?', '\n', ';', ':')

    /**
     * Appends streaming token chunks and yields complete sentences as soon as punctuation is detected.
     */
    fun processChunk(token: String): List<String> {
        buffer.append(token)
        val readySentences = mutableListOf<String>()

        var searchIndex = 0
        while (searchIndex < buffer.length) {
            val char = buffer[searchIndex]
            if (sentenceDelimiters.contains(char)) {
                val sentence = buffer.substring(0, searchIndex + 1).trim()
                if (sentence.isNotBlank()) {
                    readySentences.add(sentence)
                }
                buffer.delete(0, searchIndex + 1)
                searchIndex = 0
            } else {
                searchIndex++
            }
        }

        return readySentences
    }

    fun flushRemaining(): String? {
        val remaining = buffer.toString().trim()
        buffer.clear()
        return if (remaining.isNotBlank()) remaining else null
    }
}

class VoiceManager(private val context: Context) : TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "VoiceManager"
    }

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            isInitialized = true
            Log.i(TAG, "Brahma Neural TTS Engine Initialized")
        } else {
            Log.e(TAG, "TTS Initialization failed: $status")
        }
    }

    fun speak(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH) {
        if (!isInitialized) return
        tts?.speak(text, queueMode, null, "BrahmaTTS_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
