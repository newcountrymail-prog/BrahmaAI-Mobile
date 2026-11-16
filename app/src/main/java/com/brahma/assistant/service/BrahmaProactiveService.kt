package com.brahma.assistant.service

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.brahma.assistant.BrahmaApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class BrahmaProactiveService : Service() {

    companion object {
        private const val TAG = "BrahmaProactiveService"
        private const val NOTIFICATION_ID = 1002

        fun start(context: Context) {
            val intent = Intent(context, BrahmaProactiveService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, BrahmaProactiveService::class.java))
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildNotification())
        startProactiveLoop()
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, BrahmaApplication.CHANNEL_ID_PROACTIVE)
            .setContentTitle("Brahma Autonomous Core Active")
            .setContentText("Proactive contextual intelligence daemon running.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun startProactiveLoop() {
        serviceScope.launch {
            Log.i(TAG, "Proactive loop initiated.")
            while (isActive) {
                // Low-frequency heartbeat (every 60s) for background context evaluation
                delay(60_000L)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
