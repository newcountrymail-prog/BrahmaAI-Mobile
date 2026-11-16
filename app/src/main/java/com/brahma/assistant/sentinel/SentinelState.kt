package com.brahma.assistant.sentinel

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

sealed interface SentinelState {
    data object Disarmed : SentinelState
    data object Armed : SentinelState
    data class Authenticating(val progress: Float) : SentinelState
    data class TamperDetected(val timestamp: Long, val snapshotPath: String?) : SentinelState
}

class BrahmaDeviceAdminReceiver : DeviceAdminReceiver() {
    companion object {
        private const val TAG = "BrahmaDeviceAdmin"
    }

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.i(TAG, "Brahma Sentinel Device Administrator Privileges Granted")
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.w(TAG, "Brahma Sentinel Device Administrator Privileges Revoked")
    }

    override fun onPasswordFailed(context: Context, intent: Intent) {
        super.onPasswordFailed(context, intent)
        Log.w(TAG, "Device unlock failed! Alerting Sentinel subsystem.")
        val lockIntent = Intent(context, SentinelLockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(lockIntent)
    }
}
