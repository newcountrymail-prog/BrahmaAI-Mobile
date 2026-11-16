package com.brahma.assistant.actions

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SystemDiagnosticsAction(private val context: Context) : BrahmaAction {
    override val actionType: String = "SYSTEM_DIAGNOSTICS"

    override suspend fun execute(parameters: Map<String, String>): Result<String> = withContext(Dispatchers.IO) {
        val metric = parameters["metric"]?.lowercase() ?: "all"

        val report = buildString {
            if (metric == "battery" || metric == "all") {
                val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                val batteryStatus = context.registerReceiver(null, ifilter)
                val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else -1
                appendLine("Battery Level: $batteryPct%")
            }
            if (metric == "storage" || metric == "all") {
                val stat = StatFs(Environment.getDataDirectory().path)
                val bytesAvailable = stat.availableBlocksLong * stat.blockSizeLong
                val gigaAvailable = bytesAvailable / (1024 * 1024 * 1024)
                appendLine("Available Storage: ${gigaAvailable}GB")
            }
            if (metric == "memory" || metric == "all") {
                val runtime = Runtime.getRuntime()
                val usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
                val maxMemInMB = runtime.maxMemory() / (1024 * 1024)
                appendLine("JVM Memory: ${usedMemInMB}MB / ${maxMemInMB}MB")
            }
        }

        Result.success(report.trim())
    }
}

class DeviceSettingsAction(private val context: Context) : BrahmaAction {
    override val actionType: String = "DEVICE_SETTINGS"

    override suspend fun execute(parameters: Map<String, String>): Result<String> = withContext(Dispatchers.Main) {
        val setting = parameters["setting"]?.lowercase() ?: "settings"

        val intent = when (setting) {
            "wifi" -> Intent(Settings.ACTION_WIFI_SETTINGS)
            "bluetooth" -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            "battery_saver" -> Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
            "display" -> Intent(Settings.ACTION_DISPLAY_SETTINGS)
            "sound", "volume" -> Intent(Settings.ACTION_SOUND_SETTINGS)
            else -> Intent(Settings.ACTION_SETTINGS)
        }.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
        Result.success("Navigated to $setting settings screen.")
    }
}

class MessagingAction(private val context: Context) : BrahmaAction {
    override val actionType: String = "SEND_MESSAGE"

    override suspend fun execute(parameters: Map<String, String>): Result<String> = withContext(Dispatchers.Main) {
        val recipient = parameters["recipient"].orEmpty()
        val body = parameters["body"].orEmpty()

        val uri = if (recipient.isNotBlank()) Uri.parse("smsto:$recipient") else Uri.parse("smsto:")
        val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
            putExtra("sms_body", body)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
        Result.success("Drafted message to $recipient with content: $body")
    }
}

class BrowserAction(private val context: Context) : BrahmaAction {
    override val actionType: String = "OPEN_URL"

    override suspend fun execute(parameters: Map<String, String>): Result<String> = withContext(Dispatchers.Main) {
        val url = parameters["url"] ?: return@withContext Result.failure(IllegalArgumentException("Missing 'url'"))
        val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formattedUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        Result.success("Opened web URL: $formattedUrl")
    }
}
