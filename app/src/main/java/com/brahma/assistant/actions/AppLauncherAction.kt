package com.brahma.assistant.actions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface BrahmaAction {
    val actionType: String
    suspend fun execute(parameters: Map<String, String>): Result<String>
}

class AppLauncherAction(private val context: Context) : BrahmaAction {
    override val actionType: String = "LAUNCH_APP"

    override suspend fun execute(parameters: Map<String, String>): Result<String> = withContext(Dispatchers.Main) {
        val packageName = parameters["packageName"]
            ?: return@withContext Result.failure(IllegalArgumentException("Missing 'packageName' parameter"))

        val pm: PackageManager = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(packageName)
            ?: return@withContext Result.failure(IllegalStateException("Package '$packageName' not installed"))

        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launchIntent)
        Result.success("Launched app: $packageName")
    }
}
