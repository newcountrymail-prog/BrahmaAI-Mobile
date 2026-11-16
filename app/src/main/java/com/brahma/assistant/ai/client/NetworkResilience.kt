package com.brahma.assistant.ai.client

import android.util.Log
import kotlinx.coroutines.delay
import java.io.IOException
import kotlin.random.Random

object NetworkResilience {
    private const val TAG = "NetworkResilience"

    suspend fun <T> executeWithRetry(
        maxAttempts: Int = 3,
        initialDelayMs: Long = 1000L,
        maxDelayMs: Long = 8000L,
        factor: Double = 2.0,
        block: suspend () -> T
    ): Result<T> {
        var currentDelay = initialDelayMs
        var lastException: Throwable? = null

        for (attempt in 1..maxAttempts) {
            try {
                val result = block()
                return Result.success(result)
            } catch (e: Throwable) {
                lastException = e
                Log.w(TAG, "Operation failed on attempt $attempt/$maxAttempts: ${e.message}")

                if (attempt == maxAttempts || !isRetryable(e)) {
                    break
                }

                // Add jitter to prevent thundering herd
                val jitter = Random.nextLong(0, 250)
                delay(currentDelay + jitter)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMs)
            }
        }

        return Result.failure(lastException ?: IOException("Execution failed after $maxAttempts attempts"))
    }

    private fun isRetryable(t: Throwable): Boolean {
        return when (t) {
            is java.net.SocketTimeoutException,
            is java.net.UnknownHostException,
            is java.net.ConnectException,
            is IOException -> true
            else -> false
        }
    }
}
