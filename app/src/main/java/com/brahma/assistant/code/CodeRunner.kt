package com.brahma.assistant.code

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.UUID

class CodeRunner(private val context: Context) {

    companion object {
        private const val TAG = "CodeRunner"
        private const val DEFAULT_TIMEOUT_MS = 15_000L
    }

    private val workspaceDir = File(context.filesDir, "brahma_code_workspace").apply { mkdirs() }

    suspend fun execute(
        artifact: CodeArtifact,
        timeoutMs: Long = DEFAULT_TIMEOUT_MS
    ): CodeArtifact = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val extension = when (artifact.language) {
            CodeLanguage.PYTHON -> "py"
            CodeLanguage.BASH -> "sh"
            CodeLanguage.JAVASCRIPT -> "js"
            CodeLanguage.JSON -> "json"
            CodeLanguage.SQL -> "sql"
            CodeLanguage.KOTLIN -> "kt"
            CodeLanguage.HTML -> "html"
            else -> "txt"
        }

        val scriptFile = File(workspaceDir, "${artifact.filename.substringBeforeLast(".")}_${UUID.randomUUID()}.$extension")
        scriptFile.writeText(artifact.sourceCode)

        val command = when (artifact.language) {
            CodeLanguage.PYTHON -> listOf("python3", scriptFile.absolutePath)
            CodeLanguage.BASH -> listOf("sh", scriptFile.absolutePath)
            CodeLanguage.JAVASCRIPT -> listOf("node", scriptFile.absolutePath)
            else -> null
        }

        if (command == null) {
            return@withContext artifact.copy(
                executionOutput = "Code saved to workspace: ${scriptFile.name}\n(Direct compilation for ${artifact.language} requires external build harness)",
                executionDurationMs = System.currentTimeMillis() - startTime
            )
        }

        val result = withTimeoutOrNull(timeoutMs) {
            try {
                val processBuilder = ProcessBuilder(command)
                processBuilder.directory(workspaceDir)
                processBuilder.environment()["TMPDIR"] = context.cacheDir.absolutePath
                val process = processBuilder.start()

                val stdout = BufferedReader(InputStreamReader(process.inputStream)).readText()
                val stderr = BufferedReader(InputStreamReader(process.errorStream)).readText()
                val exitCode = process.waitFor()

                val duration = System.currentTimeMillis() - startTime
                if (exitCode == 0) {
                    artifact.copy(
                        executionOutput = stdout.ifBlank { "Program exited successfully with code 0." },
                        executionDurationMs = duration
                    )
                } else {
                    artifact.copy(
                        executionError = "Process exited with code $exitCode\n$stderr",
                        executionOutput = stdout,
                        executionDurationMs = duration
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Execution error: ${e.message}", e)
                artifact.copy(
                    executionError = "Runtime execution error: ${e.message}",
                    executionDurationMs = System.currentTimeMillis() - startTime
                )
            } finally {
                scriptFile.delete()
            }
        }

        result ?: artifact.copy(
            executionError = "Execution timed out after ${timeoutMs / 1000}s",
            executionDurationMs = timeoutMs
        )
    }

    fun saveToDisk(filename: String, code: String): File {
        val file = File(workspaceDir, filename)
        file.writeText(code)
        return file
    }

    fun listWorkspaceFiles(): List<File> = workspaceDir.listFiles()?.toList().orEmpty()
}
