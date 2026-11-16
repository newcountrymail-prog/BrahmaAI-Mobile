package com.brahma.assistant.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brahma.assistant.ai.model.AIState
import com.brahma.assistant.ai.model.ChatMessage
import com.brahma.assistant.ai.model.MessageRole
import com.brahma.assistant.ui.BrahmaViewModel
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
    val messages by viewModel.chatMessages.collectAsState()
    val aiState by viewModel.aiState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

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
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(BrahmaCyan)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "BRAHMA SOVEREIGN AI",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp
                )
            }

            IconButton(onClick = { viewModel.clearChat() }) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear Chat",
                    tint = BrahmaTextDim
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Chat Stream Area
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatMessageBubble(msg)
            }

            // Real-time streaming or thinking bubble
            when (val state = aiState) {
                is AIState.Thinking -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(BrahmaSurface)
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BrahmaCyan, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Neural Cortex reasoning...",
                                    color = BrahmaTextDim,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
                is AIState.Streaming -> {
                    item {
                        ChatMessageBubble(
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
                                .padding(12.dp)
                        ) {
                            Text(
                                text = state.stepDescription,
                                color = BrahmaCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
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
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Fault: ${state.message}",
                                color = Color(0xFFFF8A80),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
                else -> Unit
            }
        }

        // Voice Visualizer
        if (aiState is AIState.Streaming || aiState is AIState.Thinking) {
            VoiceWaveformVisualizer(isSpeaking = true, modifier = Modifier.padding(vertical = 4.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Ask Brahma or type plan: <directive>", color = BrahmaTextDim, fontSize = 14.sp) },
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
                    contentDescription = "Send",
                    tint = Color.Black
                )
            }
        }
    }
}

@Composable
fun ChatMessageBubble(message: ChatMessage) {
    val isUser = message.role == MessageRole.USER
    val bubbleColor = if (isUser) BrahmaSurfaceLight else BrahmaSurface
    val alignment = if (isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
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
                text = message.text,
                color = BrahmaText,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}
