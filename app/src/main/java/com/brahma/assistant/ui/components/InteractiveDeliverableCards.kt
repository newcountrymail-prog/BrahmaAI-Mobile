package com.brahma.assistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brahma.assistant.code.CodeArtifact
import com.brahma.assistant.code.CodeLanguage
import com.brahma.assistant.code.CodeRunner
import com.brahma.assistant.ui.theme.BrahmaCyan
import com.brahma.assistant.ui.theme.BrahmaPurple
import com.brahma.assistant.ui.theme.BrahmaSurface
import com.brahma.assistant.ui.theme.BrahmaSurfaceLight
import com.brahma.assistant.ui.theme.BrahmaText
import com.brahma.assistant.ui.theme.BrahmaTextDim
import kotlinx.coroutines.launch

data class ParsedCodeSnippet(
    val language: String,
    val code: String
)

object CodeBlockParser {
    private val CODE_REGEX = Regex("```(\\w*)\\n([\\s\\S]*?)```")

    fun extractSnippets(rawText: String): List<ParsedCodeSnippet> {
        val matches = CODE_REGEX.findAll(rawText)
        return matches.map { match ->
            val lang = match.groupValues[1].ifBlank { "python" }
            val code = match.groupValues[2].trimEnd()
            ParsedCodeSnippet(language = lang, code = code)
        }.toList()
    }

    fun removeCodeBlocks(rawText: String): String {
        return CODE_REGEX.replace(rawText, "").trim()
    }
}

@Composable
fun InteractiveCodeCard(
    snippet: ParsedCodeSnippet,
    onOpenInStudio: ((String, String) -> Unit)? = null
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val codeRunner = remember { CodeRunner(context) }

    var isRunning by remember { mutableStateOf(false) }
    var executionOutput by remember { mutableStateOf<String?>(null) }
    var executionError by remember { mutableStateOf<String?>(null) }
    var durationMs by remember { mutableStateOf(0L) }

    val codeLanguage = when (snippet.language.lowercase()) {
        "python", "py" -> CodeLanguage.PYTHON
        "bash", "sh", "shell" -> CodeLanguage.BASH
        "javascript", "js" -> CodeLanguage.JAVASCRIPT
        "kotlin", "kt" -> CodeLanguage.KOTLIN
        "json" -> CodeLanguage.JSON
        "sql" -> CodeLanguage.SQL
        else -> CodeLanguage.PYTHON
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF070B16))
            .padding(12.dp)
    ) {
        // Top Card Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(BrahmaPurple)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = snippet.language.uppercase().ifBlank { "CODE" },
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Live Executable Deliverable",
                    color = BrahmaTextDim,
                    fontSize = 12.sp
                )
            }

            Row {
                IconButton(
                    onClick = { clipboard.setText(AnnotatedString(snippet.code)) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = BrahmaCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Code Content Viewer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF03050A))
                .padding(10.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            Text(
                text = snippet.code,
                color = Color(0xFF80D8FF),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 17.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    scope.launch {
                        isRunning = true
                        executionOutput = null
                        executionError = null
                        val artifact = CodeArtifact(
                            filename = "generated_script.${if (codeLanguage == CodeLanguage.PYTHON) "py" else "sh"}",
                            language = codeLanguage,
                            sourceCode = snippet.code
                        )
                        val result = codeRunner.execute(artifact)
                        executionOutput = result.executionOutput
                        executionError = result.executionError
                        durationMs = result.executionDurationMs
                        isRunning = false
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrahmaCyan,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (isRunning) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.Black, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Running...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Run on Device", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Terminal Console Output Window
        if (executionOutput != null || executionError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF020408))
                    .padding(10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = if (executionError != null) Color(0xFFFF5252) else Color(0xFF69F0AE),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (executionError != null) "Execution Error" else "Standard Output",
                                color = if (executionError != null) Color(0xFFFF5252) else Color(0xFF69F0AE),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${durationMs}ms",
                            color = BrahmaTextDim,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = executionError ?: executionOutput.orEmpty(),
                        color = if (executionError != null) Color(0xFFFF8A80) else Color(0xFFE0F7FA),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
