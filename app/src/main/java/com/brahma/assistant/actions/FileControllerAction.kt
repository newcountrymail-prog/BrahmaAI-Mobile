package com.brahma.assistant.actions

import android.content.Context
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FileControllerAction(private val context: Context) : BrahmaAction {
    override val actionType: String = "FILE_CONTROLLER"

    override suspend fun execute(parameters: Map<String, String>): Result<String> = withContext(Dispatchers.IO) {
        val operation = parameters["operation"]?.lowercase() ?: "read"
        val filepath = parameters["path"] ?: return@withContext Result.failure(IllegalArgumentException("Missing 'path'"))

        val targetFile = if (filepath.startsWith("/")) {
            File(filepath)
        } else {
            File(context.filesDir, filepath)
        }

        try {
            when (operation) {
                "write" -> {
                    val content = parameters["content"] ?: ""
                    targetFile.parentFile?.mkdirs()
                    targetFile.writeText(content)
                    Result.success("Successfully wrote ${content.length} bytes to ${targetFile.absolutePath}")
                }
                "read" -> {
                    if (!targetFile.exists()) {
                        return@withContext Result.failure(NoSuchFileException(targetFile, reason = "File does not exist"))
                    }
                    val content = targetFile.readText()
                    Result.success("File Content (${targetFile.name}):\n$content")
                }
                "list" -> {
                    val list = targetFile.listFiles()?.joinToString("\n") { "${if (it.isDirectory) "[DIR] " else "[FILE] "}${it.name}" }
                    Result.success(list ?: "Directory empty or inaccessible")
                }
                else -> Result.failure(IllegalArgumentException("Unsupported file operation: $operation"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
