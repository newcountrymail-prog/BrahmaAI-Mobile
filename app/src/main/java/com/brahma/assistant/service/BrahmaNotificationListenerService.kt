package com.brahma.assistant.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.brahma.assistant.memory.BrahmaMemoryCortex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

data class InterceptedNotification(
    val packageName: String,
    val title: String,
    val text: String,
    val timestamp: Long
)

class BrahmaNotificationListenerService : NotificationListenerService() {

    companion object {
        private const val TAG = "BrahmaNotifListener"

        @Volatile
        var latestNotification: InterceptedNotification? = null
            private set
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val notification = sbn?.notification ?: return
        val extras = notification.extras ?: return

        val title = extras.getCharSequence("android.title")?.toString().orEmpty()
        val text = extras.getCharSequence("android.text")?.toString().orEmpty()
        val packageName = sbn.packageName.orEmpty()

        if (title.isBlank() && text.isBlank()) return

        val intercepted = InterceptedNotification(
            packageName = packageName,
            title = title,
            text = text,
            timestamp = sbn.postTime
        )
        latestNotification = intercepted
        Log.d(TAG, "Captured notification from $packageName: $title - $text")

        // Pass high-value notifications (e.g. OTP, reminders, alarms) to Memory Cortex
        serviceScope.launch {
            if (text.contains("code", ignoreCase = true) || text.contains("otp", ignoreCase = true) || text.contains("meeting", ignoreCase = true)) {
                val cortex = BrahmaMemoryCortex.getInstance(applicationContext)
                cortex.recordManualMemory(
                    key = "Notification [$packageName]: $title",
                    value = text
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
