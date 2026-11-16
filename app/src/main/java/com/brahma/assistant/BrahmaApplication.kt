package com.brahma.assistant

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log

class BrahmaApplication : Application() {

    companion object {
        const val TAG = "BrahmaApplication"
        const val CHANNEL_ID_OVERLAY = "brahma_overlay_channel"
        const val CHANNEL_ID_PROACTIVE = "brahma_proactive_channel"
        const val CHANNEL_ID_SENTINEL = "brahma_sentinel_channel"

        lateinit var instance: BrahmaApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.i(TAG, "Initializing Brahma AI Sovereign Neural Runtime v1.0.1")
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val overlayChannel = NotificationChannel(
                CHANNEL_ID_OVERLAY,
                "Brahma Floating HUD Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Maintains persistent low-latency floating orb and accessibility bridge."
                setShowBadge(false)
            }

            val proactiveChannel = NotificationChannel(
                CHANNEL_ID_PROACTIVE,
                "Brahma Proactive Autonomous Daemon",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Handles background context monitoring and schedule triggers."
                setShowBadge(false)
            }

            val sentinelChannel = NotificationChannel(
                CHANNEL_ID_SENTINEL,
                "Brahma Sentinel Security Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts on unauthorized device access and biometric tamper events."
                enableVibration(true)
            }

            notificationManager.createNotificationChannels(
                listOf(overlayChannel, proactiveChannel, sentinelChannel)
            )
        }
    }
}
