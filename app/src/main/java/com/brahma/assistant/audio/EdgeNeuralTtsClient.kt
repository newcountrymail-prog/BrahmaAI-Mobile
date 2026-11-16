package com.brahma.assistant.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.util.UUID

/**
 * High-fidelity, ultra-realistic human neural TTS client.
 * Connects to Edge Neural Speech synthesizers (delivering natural human intonation,
 * expressive breathing, and warmth, eliminating robotic Android TTS monotone).
 */
class EdgeNeuralTtsClient(private val context: Context) {

    companion object {
        private const val TAG = "EdgeNeuralTts"
        // Natural, warm, friendly human neural voices
        const val VOICE_FRIENDLY_MALE = "en-US-AndrewMultilingualNeural"
        const val VOICE_FRIENDLY_FEMALE = "en-US-AvaMultilingualNeural"
        const val VOICE_HINDI_NATURAL = "hi-IN-MadhurNeural"
        const val VOICE_HINDI_FEMALE = "hi-IN-SwaraNeural"
    }

    private val httpClient = OkHttpClient()
    private var mediaPlayer: MediaPlayer? = null
    private val audioCacheDir = File(context.cacheDir, "neural_tts_cache").apply { mkdirs() }

    suspend fun synthesizeAndPlay(
        text: String,
        voiceName: String = VOICE_FRIENDLY_MALE,
        onPlaybackCompleted: (() -> Unit)? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            stop()

            val cleanText = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").trim()
            if (cleanText.isBlank()) return@withContext Result.success(Unit)

            // Microsoft Speech SSML with natural speaking pitch, rate, and warmth
            val ssml = """
                <speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xml:lang='en-US'>
                    <voice name='$voiceName'>
                        <prosody pitch='+0Hz' rate='+2%' volume='+0%'>
                            $cleanText
                        </prosody>
                    </voice>
                </speak>
            """.trimIndent()

            val url = "https://speech.platform.bing.com/consumer/speech/synthesize/readaloud/edge/v1?trustedclienttoken=6A5AA1D4EAFF4E9FB37E23D68491D6F4"
            
            val request = Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/ssml+xml")
                .addHeader("X-Edge-Origin", "chrome-extension")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .post(ssml.toRequestBody())
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(IllegalStateException("Neural TTS API error: ${response.code}"))
            }

            val audioBytes = response.body?.bytes()
                ?: return@withContext Result.failure(IllegalStateException("Empty audio payload received"))

            val tempAudioFile = File(audioCacheDir, "tts_${UUID.randomUUID()}.mp3")
            FileOutputStream(tempAudioFile).use { it.write(audioBytes) }

            withContext(Dispatchers.Main) {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_ASSISTANT)
                            .build()
                    )
                    setDataSource(tempAudioFile.absolutePath)
                    prepare()
                    setOnCompletionListener {
                        tempAudioFile.delete()
                        onPlaybackCompleted?.invoke()
                    }
                    start()
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Neural speech playback failed: ${e.message}")
            Result.failure(e)
        }
    }

    fun stop() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.reset()
                it.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping media player: ${e.message}")
        }
    }
}
