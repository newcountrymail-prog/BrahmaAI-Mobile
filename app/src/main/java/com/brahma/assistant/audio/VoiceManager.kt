package com.brahma.assistant.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Locale

class VoiceManager(private val context: Context) : TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "VoiceManager"
    }

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val neuralClient = EdgeNeuralTtsClient(context)
    private var fallbackTts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isFallbackInitialized = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            fallbackTts?.language = Locale.US
            isFallbackInitialized = true
        }
    }

    /**
     * Speaks text using ultra-realistic human neural speech by default.
     * Falls back to offline synthesizer only if completely offline.
     */
    fun speak(text: String, voiceName: String = EdgeNeuralTtsClient.VOICE_FRIENDLY_MALE) {
        scope.launch {
            val result = neuralClient.synthesizeAndPlay(text, voiceName)
            if (result.isFailure) {
                Log.w(TAG, "Neural TTS unavailable, falling back to local TTS engine: ${result.exceptionOrNull()?.message}")
                if (isFallbackInitialized) {
                    fallbackTts?.speak(text, TextToSpeech.QUEUE_ADD, null, "BrahmaFallback_${System.currentTimeMillis()}")
                }
            }
        }
    }

    fun stop() {
        neuralClient.stop()
        fallbackTts?.stop()
    }

    fun shutdown() {
        stop()
        scope.cancel()
        fallbackTts?.shutdown()
        fallbackTts = null
    }
}
