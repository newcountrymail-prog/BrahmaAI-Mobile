package com.brahma.assistant.code

import android.content.Context
import com.brahma.assistant.actions.BrahmaAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CodingAgentAction(private val context: Context) : BrahmaAction {
    override val actionType: String = "CODE_EXECUTE"

    private val codeRunner = CodeRunner(context)

    override suspend fun execute(parameters: Map<String, String>): Result<String> = withContext(Dispatchers.IO) {
        val code = parameters["code"]
            ?: return@withContext Result.failure(IllegalArgumentException("Missing 'code' parameter"))
        val languageStr = parameters["language"]?.uppercase() ?: "PYTHON"
        val filename = parameters["filename"] ?: "script.py"

        val language = try {
            CodeLanguage.valueOf(languageStr)
        } catch (e: Exception) {
            CodeLanguage.PYTHON
        }

        val artifact = CodeArtifact(
            filename = filename,
            language = language,
            sourceCode = code
        )

        val result = codeRunner.execute(artifact)
        if (result.executionError != null) {
            Result.failure(RuntimeException("Execution Failed: ${result.executionError}"))
        } else {
            Result.success("Execution Output:\n${result.executionOutput}")
        }
    }
}
