package com.brahma.assistant.service

import android.content.Intent
import android.os.Bundle
import android.service.voice.VoiceInteractionService
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService
import android.speech.RecognitionService
import android.util.Log

class BrahmaVoiceInteractionService : VoiceInteractionService() {
    companion object {
        private const val TAG = "BrahmaVoiceService"
    }

    override fun onReady() {
        super.onReady()
        Log.i(TAG, "Brahma Default Voice Interaction Service Ready.")
    }
}

class BrahmaVoiceInteractionSessionService : VoiceInteractionSessionService() {
    override fun onNewSession(args: Bundle?): VoiceInteractionSession {
        return BrahmaVoiceSession(this)
    }
}

class BrahmaVoiceSession(context: android.content.Context) : VoiceInteractionSession(context) {
    override fun onHandleAssist(data: Bundle?, structure: android.app.assist.AssistStructure?, content: android.app.assist.AssistContent?) {
        super.onHandleAssist(data, structure, content)
        Log.d("BrahmaVoiceSession", "Assisting user invocation with system context.")
    }
}

class BrahmaRecognitionService : RecognitionService() {
    override fun onStartListening(recognizerIntent: Intent?, listener: Callback?) {
        Log.d("BrahmaRecognition", "Speech recognition session started.")
    }

    override fun onCancel(listener: Callback?) {
        Log.d("BrahmaRecognition", "Speech recognition session canceled.")
    }

    override fun onStopListening(listener: Callback?) {
        Log.d("BrahmaRecognition", "Speech recognition session stopped.")
    }
}
