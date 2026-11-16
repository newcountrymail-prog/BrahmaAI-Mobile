package com.brahma.assistant.ui.screens

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brahma.assistant.ai.model.AIState
import com.brahma.assistant.ai.model.ChatMessage
import com.brahma.assistant.ai.model.MessageRole
import com.brahma.assistant.ui.BrahmaViewModel
import com.brahma.assistant.ui.components.CodeBlockParser
import com.brahma.assistant.ui.components.InteractiveCodeCard
import com.brahma.assistant.ui.components.VoiceWaveformVisualizer
import com.brahma.assistant.ui.theme.BrahmaCyan
import com.brahma.assistant.ui.theme.BrahmaPurple
import com.brahma.assistant.ui.theme.BrahmaSurface
import com.brahma.assistant.ui.theme.BrahmaSurfaceLight
import com.brahma.assistant.ui.theme.BrahmaText
import com.brahma.assistant.ui.theme.BrahmaTextDim
import com.brahma.assistant.ui.theme.BrahmaVoid

@Composable
fun MainAssistantScreen(viewModel: BrahmaViewModel) {
    val context = LocalContext.current
    val messages by viewModel.chatMessages.collectAsState()
    val aiState by viewModel.aiState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Query live device telemetry
    val batteryPct = remember {
        val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val status = context.registerReceiver(null, ifilter)
        val level = status?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = status?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        if (level >= 0 && scale > 0) (level * 100 / scale) else 100
    }

    val ramUsedMB = remember {
        val runtime = Runtime.getRuntime()
        ((runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)).toInt()
    }

    LaunchedEffect(messages.size, aiState) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrahmaVoid)
            .padding(16.dp)
    ) {
        // TOP HOLOGRAPHIC QUANTUM CORE HUD
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(BrahmaCyan)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "BRAHMA SOVEREIGN CORE",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "AUTONOMOUS SYSTEM ONLINE // NEURAL ENGINE READY",
                        color = BrahmaCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            IconButton(onClick = { viewModel.clearChat() }) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear Session",
                    tint = BrahmaTextDim
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // TELEMETRY STATUS PILLS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusPill(icon = Icons.Default.Speed, label = "BATTERY", value = "$batteryPct%")
            StatusPill(icon = Icons.Default.Memory, label = "JVM RAM", value = "${ramUsedMB}MB")
            StatusPill(icon = Icons.Default.Terminal, label = "COMPILER", value = "SANDBOX ON")
            StatusPill(icon = Icons.Default.AutoAwesome, label = "SENTINEL", value = "ARMED")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // If no messages yet, show prominent Power Tools and Quick Starters
        if (messages.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "AUTONOMOUS CODING & SYSTEM ENGINE",
                    color = BrahmaCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Brahma can write code, run Python & shell scripts on-device, execute multi-agent plans, and control system hardware directly from your natural language prompts.",
                    color = BrahmaTextDim,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "QUICK POWER ACTIONS",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        icon = Icons.Default.Code,
                        title = "Code & Run Script",
                        subtitle = "Python & Bash",
                        modifier = Modifier.weight(1f),
                        onClick = { inputText = "Write a python script that generates a Fibonacci sequence and run it." }
                    )
                    QuickActionCard(
                        icon = Icons.Default.Terminal,
                        title = "Shell Diagnostics",
                        subtitle = "Device Telemetry",
                        modifier = Modifier.weight(1f),
                        onClick = { inputText = "plan: diagnose battery, memory and list active files" }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        icon = Icons.Default.AutoAwesome,
                        title = "Multi-Agent Plan",
                        subtitle = "Decompose tasks",
                        modifier = Modifier.weight(1f),
                        onClick = { inputText = "plan: check wifi status and open settings" }
                    )
                    QuickActionCard(
                        icon = Icons.Default.Memory,
                        title = "Code Studio IDE",
                        subtitle = "Open Compiler",
                        modifier = Modifier.weight(1f),
                        onClick = { inputText = "Write a complete Kotlin helper class for Android preferences" }
                    )
                }
            }
        } else {
            // Live Chat & Interactive Deliverables Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    FullChatBubble(msg)
                }

                // AI Processing States
                when (val state = aiState) {
                    is AIState.Thinking -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BrahmaSurface)
                                    .padding(14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = BrahmaCyan, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Neural Cortex compiling code & reasoning...",
                                        color = BrahmaTextDim,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                    is AIState.Streaming -> {
                        item {
                            FullChatBubble(
                                ChatMessage(
                                    role = MessageRole.ASSISTANT,
                                    text = state.partialResponse,
                                    isStreaming = true
                                )
                            )
                        }
                    }
                    is AIState.ExecutingPlan -> {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BrahmaSurface)
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = state.stepDescription,
                                    color = BrahmaCyan,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = state.progress,
                                    modifier = Modifier.fillMaxWidth(),
                                    color = BrahmaCyan,
                                    trackColor = BrahmaSurfaceLight
                                )
                            }
                        }
                    }
                    is AIState.Error -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF3B1219))
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = "System Fault: ${state.message}",
                                    color = Color(0xFFFF8A80),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }

        // Voice waveform when speaking/thinking
        if (aiState is AIState.Streaming || aiState is AIState.Thinking) {
            VoiceWaveformVisualizer(isSpeaking = true, modifier = Modifier.padding(vertical = 4.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Dynamic Input Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Directive (e.g. 'code python port scanner', 'plan: ...')", color = BrahmaTextDim, fontSize = 13.sp) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrahmaCyan,
                    unfocusedBorderColor = BrahmaSurfaceLight,
                    focusedTextColor = BrahmaText,
                    unfocusedTextColor = BrahmaText,
                    cursorColor = BrahmaCyan
                ),
                shape = RoundedCornerShape(24.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendPrompt(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(BrahmaCyan)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Dispatch",
                    tint = Color.Black
                )
            }
        }
    }
}

@Composable
fun FullChatBubble(message: ChatMessage) {
    val isUser = message.role == MessageRole.USER
    val bubbleColor = if (isUser) BrahmaSurfaceLight else BrahmaSurface
    val alignment = if (isUser) Alignment.End else Alignment.Start

    val codeSnippets = remember(message.text) {
        if (!isUser) CodeBlockParser.extractSnippets(message.text) else emptyList()
    }
    val cleanText = remember(message.text) {
        if (!isUser) CodeBlockParser.removeCodeBlocks(message.text) else message.text
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        if (cleanText.isNotBlank()) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .background(bubbleColor)
                    .padding(14.dp)
            ) {
                Text(
                    text = cleanText,
                    color = BrahmaText,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }

        // Render Interactive Code Deliverables if code snippets exist!
        if (codeSnippets.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            codeSnippets.forEach { snippet ->
                InteractiveCodeCard(snippet = snippet)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun StatusPill(icon: ImageVector, label: String, value: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(BrahmaSurface)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = BrahmaCyan, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(5.dp))
            Text(text = label, color = BrahmaTextDim, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = value, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun QuickActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BrahmaSurface)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column {
            Icon(imageVector = icon, contentDescription = null, tint = BrahmaCyan, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, color = BrahmaTextDim, fontSize = 11.sp)
        }
    }
}
