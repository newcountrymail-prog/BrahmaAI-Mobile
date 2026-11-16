package com.brahma.assistant.sentinel

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class SentinelBiometricManager(private val context: Context) {

    companion object {
        private const val TAG = "SentinelBiometrics"
    }

    private val _sentinelState = MutableStateFlow<SentinelState>(SentinelState.Armed)
    val sentinelState: StateFlow<SentinelState> = _sentinelState.asStateFlow()

    private val detectorOptions = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()

    private val faceDetector = FaceDetection.getClient(detectorOptions)

    suspend fun verifyFace(bitmap: Bitmap): Boolean = withContext(Dispatchers.Default) {
        _sentinelState.value = SentinelState.Authenticating(0.5f)
        val image = InputImage.fromBitmap(bitmap, 0)

        suspendCancellableCoroutine { continuation ->
            faceDetector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty()) {
                        val primaryFace = faces.first()
                        val hasEyesOpen = (primaryFace.leftEyeOpenProbability ?: 0f) > 0.5f &&
                                          (primaryFace.rightEyeOpenProbability ?: 0f) > 0.5f
                        Log.i(TAG, "Face verified. Count: ${faces.size}, Eyes open: $hasEyesOpen")
                        _sentinelState.value = SentinelState.Armed
                        continuation.resume(true)
                    } else {
                        Log.w(TAG, "No valid face detected in frame.")
                        _sentinelState.value = SentinelState.TamperDetected(System.currentTimeMillis(), null)
                        continuation.resume(false)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Face detection pipeline error", e)
                    _sentinelState.value = SentinelState.Armed
                    continuation.resume(false)
                }
        }
    }
}
