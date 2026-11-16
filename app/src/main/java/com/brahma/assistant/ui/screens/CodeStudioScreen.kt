package com.brahma.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brahma.assistant.code.CodeArtifact
import com.brahma.assistant.code.CodeLanguage
import com.brahma.assistant.code.CodeRunner
import com.brahma.assistant.ui.BrahmaViewModel
import com.brahma.assistant.ui.theme.BrahmaCyan
import com.brahma.assistant.ui.theme.BrahmaSurface
import com.brahma.assistant.ui.theme.BrahmaSurfaceLight
import com.brahma.assistant.ui.theme.BrahmaText
import com.brahma.assistant.ui.theme.BrahmaTextDim
import com.brahma.assistant.ui.theme.BrahmaVoid
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeStudioScreen(viewModel: BrahmaViewModel) {
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val codeRunner = remember { CodeRunner(viewModel.getApplication()) }

    var filename by remember { mutableStateOf("script.py") }
    var selectedLanguage by remember { mutableStateOf(CodeLanguage.PYTHON) }
    var isLanguageMenuExpanded by remember { mutableStateOf(false) }

    var codeText by remember {
        mutableStateOf(
            """# Brahma AI Sovereign Code Studio
import sys
import platform

print("Brahma Neural Core active on:", platform.system(), platform.machine())
print("Python version:", sys.version.split()[0])

def solve_task(data):
    return [x * 2 for x in data if x % 2 == 0]

print("Output:", solve_task([1, 2, 3, 4, 5, 6, 7, 8, 9, 10]))
""".trimIndent()
        )
    }

    var isRunning by remember { mutableStateOf(false) }
    var consoleOutput by remember { mutableStateOf<String?>(null) }
    var consoleError by remember { mutableStateOf<String?>(null) }
    var executionTimeMs by remember { mutableStateOf(0L) }

    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrahmaVoid)
            .padding(16.dp)
            .verticalScroll(verticalScroll)
    ) {
        // Studio Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = null,
                    tint = BrahmaCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CODE STUDIO & COMPILER",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    letterSpacing = 1.sp
                )
            }

            Row {
                IconButton(onClick = {
                    clipboardManager.setText(AnnotatedString(codeText))
                }) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Code",
                        tint = BrahmaCyan
                    )
                }

                IconButton(onClick = {
                    codeRunner.saveToDisk(filename, codeText)
                }) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save Code",
                        tint = BrahmaCyan
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Toolbar: Language & Filename
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExposedDropdownMenuBox(
                expanded = isLanguageMenuExpanded,
                onExpandedChange = { isLanguageMenuExpanded = !isLanguageMenuExpanded },
                modifier = Modifier.width(150.dp)
            ) {
                OutlinedTextField(
                    value = selectedLanguage.name,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isLanguageMenuExpanded) },
                    modifier = Modifier.menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = BrahmaCyan,
                        unfocusedTextColor = BrahmaCyan,
                        focusedBorderColor = BrahmaCyan,
                        unfocusedBorderColor = BrahmaSurfaceLight
                    ),
                    textStyle = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold)
                )
                ExposedDropdownMenu(
                    expanded = isLanguageMenuExpanded,
                    onDismissRequest = { isLanguageMenuExpanded = false },
                    modifier = Modifier.background(BrahmaSurface)
                ) {
                    CodeLanguage.entries.forEach { lang ->
                        DropdownMenuItem(
                            text = { Text(lang.name, color = BrahmaText, fontSize = 13.sp) },
                            onClick = {
                                selectedLanguage = lang
                                isLanguageMenuExpanded = false
                                if (lang == CodeLanguage.PYTHON && !filename.endsWith(".py")) filename = "script.py"
                                if (lang == CodeLanguage.BASH && !filename.endsWith(".sh")) filename = "script.sh"
                                if (lang == CodeLanguage.KOTLIN && !filename.endsWith(".kt")) filename = "Main.kt"
                                if (lang == CodeLanguage.JAVASCRIPT && !filename.endsWith(".js")) filename = "index.js"
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = filename,
                onValueChange = { filename = it },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = BrahmaText,
                    unfocusedTextColor = BrahmaText,
                    focusedBorderColor = BrahmaCyan,
                    unfocusedBorderColor = BrahmaSurfaceLight
                ),
                singleLine = true,
                textStyle = TextStyle(fontSize = 13.sp, fontFamily = FontFamily.Monospace)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Code Editor Box with Line Numbers
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF070B16))
                .padding(12.dp)
        ) {
            val lines = codeText.lines()
            val lineNumbersText = (1..lines.size).joinToString("\n")

            Row(modifier = Modifier.fillMaxSize()) {
                // Line Numbers Column
                Text(
                    text = lineNumbersText,
                    color = Color(0xFF4A5568),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )

                // Editable Code Field
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(horizontalScroll)
                ) {
                    BasicTextField(
                        value = codeText,
                        onValueChange = { codeText = it },
                        textStyle = TextStyle(
                            color = Color(0xFF80D8FF),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 18.sp
                        ),
                        cursorBrush = SolidColor(BrahmaCyan),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action Buttons: Run / Execute
        Button(
            onClick = {
                scope.launch {
                    isRunning = true
                    consoleOutput = null
                    consoleError = null
                    val artifact = CodeArtifact(
                        filename = filename,
                        language = selectedLanguage,
                        sourceCode = codeText
                    )
                    val result = codeRunner.execute(artifact)
                    consoleOutput = result.executionOutput
                    consoleError = result.executionError
                    executionTimeMs = result.executionDurationMs
                    isRunning = false
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = BrahmaCyan,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isRunning) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Executing Code...", fontWeight = FontWeight.Bold)
            } else {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Execute / Run Script", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Console Output Window
        Text(
            text = "EXECUTION TERMINAL ${if (executionTimeMs > 0) "(${executionTimeMs}ms)" else ""}",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF03050A))
                .padding(12.dp)
        ) {
            val text = consoleError?.let { "ERROR:\n$it" } ?: consoleOutput ?: "Console idle. Click 'Execute / Run Script' to execute."
            val textColor = when {
                consoleError != null -> Color(0xFFFF5252)
                consoleOutput != null -> Color(0xFF69F0AE)
                else -> BrahmaTextDim
            }

            Text(
                text = text,
                color = textColor,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 16.sp,
                modifier = Modifier.verticalScroll(rememberScrollState())
            )
        }
    }
}
