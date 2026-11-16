package com.brahma.assistant.ui.screens

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brahma.assistant.service.BrahmaFloatingOverlayService
import com.brahma.assistant.service.BrahmaProactiveService
import com.brahma.assistant.ui.BrahmaViewModel
import com.brahma.assistant.ui.theme.BrahmaCyan
import com.brahma.assistant.ui.theme.BrahmaSurface
import com.brahma.assistant.ui.theme.BrahmaSurfaceLight
import com.brahma.assistant.ui.theme.BrahmaText
import com.brahma.assistant.ui.theme.BrahmaTextDim
import com.brahma.assistant.ui.theme.BrahmaVoid
import com.brahma.assistant.wallpaper.BrahmaLiveWallpaperService
import kotlinx.coroutines.launch

@Composable
fun HardwareHubScreen(viewModel: BrahmaViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val geminiKey by viewModel.preferencesManager.geminiApiKey.collectAsState(initial = "")
    val selectedModel by viewModel.preferencesManager.selectedModel.collectAsState(initial = "gemini-1.5-flash")
    val isFloatingOrbEnabled by viewModel.preferencesManager.isFloatingOrbEnabled.collectAsState(initial = true)
    val isProactiveEnabled by viewModel.preferencesManager.isProactiveModeEnabled.collectAsState(initial = true)
    val isVoiceEnabled by viewModel.preferencesManager.isVoiceFeedbackEnabled.collectAsState(initial = true)

    var inputGeminiKey by remember(geminiKey) { mutableStateOf(geminiKey) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrahmaVoid)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = BrahmaCyan)
            Spacer(modifier = Modifier.padding(4.dp))
            Text(
                text = "SYSTEM & HARDWARE HUB",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Gemini API Key Config Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BrahmaSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Google Gemini Intelligence Config",
                    color = BrahmaCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = inputGeminiKey,
                    onValueChange = { inputGeminiKey = it },
                    label = { Text("Gemini API Key", color = BrahmaTextDim) },
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
                        scope.launch {
                            viewModel.preferencesManager.setGeminiApiKey(inputGeminiKey)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrahmaCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(imageVector = Icons.Default.Key, contentDescription = null)
                    Spacer(modifier = Modifier.padding(2.dp))
                    Text("Save API Key", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // System Services Management Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BrahmaSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Autonomous System Services",
                    color = BrahmaCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Floating Orb Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Floating HUD Overlay Orb", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("Draws persistent on-screen assistant micro-orb", color = BrahmaTextDim, fontSize = 12.sp)
                    }
                    Switch(
                        checked = isFloatingOrbEnabled,
                        onCheckedChange = { enabled ->
                            scope.launch {
                                viewModel.preferencesManager.setFloatingOrbEnabled(enabled)
                                if (enabled) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                                        context.startActivity(intent)
                                    } else {
                                        BrahmaFloatingOverlayService.start(context)
                                    }
                                } else {
                                    BrahmaFloatingOverlayService.stop(context)
                                }
                            }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = BrahmaCyan)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Proactive Daemon Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Proactive Background Daemon", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("Background context evaluation & routine triggers", color = BrahmaTextDim, fontSize = 12.sp)
                    }
                    Switch(
                        checked = isProactiveEnabled,
                        onCheckedChange = { enabled ->
                            scope.launch {
                                viewModel.preferencesManager.setProactiveMode(enabled)
                                if (enabled) {
                                    BrahmaProactiveService.start(context)
                                } else {
                                    BrahmaProactiveService.stop(context)
                                }
                            }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = BrahmaCyan)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Voice TTS Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Streaming Neural Voice Feedback", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("Speaks streamed response chunks in real-time", color = BrahmaTextDim, fontSize = 12.sp)
                    }
                    Switch(
                        checked = isVoiceEnabled,
                        onCheckedChange = { enabled ->
                            scope.launch { viewModel.preferencesManager.setVoiceFeedback(enabled) }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = BrahmaCyan)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Live Wallpaper Launcher Button
                Button(
                    onClick = {
                        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                            putExtra(
                                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                                ComponentName(context, BrahmaLiveWallpaperService::class.java)
                            )
                        }
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrahmaSurfaceLight, contentColor = BrahmaCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply 3D Quantum Core Live Wallpaper", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Accessibility Settings Button
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrahmaSurfaceLight, contentColor = BrahmaCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Configure Accessibility Automation Service", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
