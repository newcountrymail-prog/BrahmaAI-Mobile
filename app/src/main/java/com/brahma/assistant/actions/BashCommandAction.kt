package com.brahma.assistant.actions

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class BashCommandAction(private val context: Context) : BrahmaAction {
    override val actionType: String = "BASH_COMMAND"

    override suspend fun execute(parameters: Map<String, String>): Result<String> = withContext(Dispatchers.IO) {
        val command = parameters["command"]
            ?: return@withContext Result.failure(IllegalArgumentException("Missing 'command' parameter"))

        try {
            val process = ProcessBuilder("sh", "-c", command)
                .directory(context.filesDir)
                .redirectErrorStream(true)
                .start()

            val output = BufferedReader(InputStreamReader(process.inputStream)).readText()
            val exitCode = process.waitFor()

            if (exitCode == 0) {
                Result.success(output.ifBlank { "Command executed successfully (exit code 0)." })
            } else {
                Result.failure(RuntimeException("Command failed (exit $exitCode):\n$output"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
