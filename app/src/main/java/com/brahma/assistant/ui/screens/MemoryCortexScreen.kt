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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brahma.assistant.memory.MemoryItem
import com.brahma.assistant.memory.MemoryType
import com.brahma.assistant.ui.BrahmaViewModel
import com.brahma.assistant.ui.theme.BrahmaCyan
import com.brahma.assistant.ui.theme.BrahmaSurface
import com.brahma.assistant.ui.theme.BrahmaSurfaceLight
import com.brahma.assistant.ui.theme.BrahmaText
import com.brahma.assistant.ui.theme.BrahmaTextDim
import com.brahma.assistant.ui.theme.BrahmaVoid
import kotlinx.coroutines.launch

@Composable
fun MemoryCortexScreen(viewModel: BrahmaViewModel) {
    val memories by viewModel.memoriesList.collectAsState()
    val scope = rememberCoroutineScope()

    var newKey by remember { mutableStateOf("") }
    var newValue by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrahmaVoid)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = BrahmaCyan
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Text(
                text = "MEMORY CORTEX",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Text(
            text = "Persistent semantic knowledge graph and episodic facts synthesized on-device.",
            color = BrahmaTextDim,
            fontSize = 12.sp,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Add manual memory card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BrahmaSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Add Semantic Memory",
                    color = BrahmaCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newKey,
                    onValueChange = { newKey = it },
                    label = { Text("Memory Key (e.g., User Identity)", color = BrahmaTextDim) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = BrahmaText,
                        unfocusedTextColor = BrahmaText,
                        focusedBorderColor = BrahmaCyan,
                        unfocusedBorderColor = BrahmaSurfaceLight
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newValue,
                    onValueChange = { newValue = it },
                    label = { Text("Factual Value", color = BrahmaTextDim) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = BrahmaText,
                        unfocusedTextColor = BrahmaText,
                        focusedBorderColor = BrahmaCyan,
                        unfocusedBorderColor = BrahmaSurfaceLight
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (newKey.isNotBlank() && newValue.isNotBlank()) {
                            scope.launch {
                                viewModel.memoryCortex.recordManualMemory(newKey.trim(), newValue.trim())
                                newKey = ""
                                newValue = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrahmaCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Text("Store Memory", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Retained Knowledge Items (${memories.size})",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(memories) { item ->
                MemoryItemCard(item)
            }
        }
    }
}

@Composable
fun MemoryItemCard(item: MemoryItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(BrahmaSurface)
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.key,
                    color = BrahmaCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Hits: ${item.accessCount}",
                    color = BrahmaTextDim,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.value,
                color = BrahmaText,
                fontSize = 13.sp
            )
        }
    }
}
