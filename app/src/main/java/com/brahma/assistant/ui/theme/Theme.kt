package com.brahma.assistant.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BrahmaCyan = Color(0xFF00E5FF)
val BrahmaCyanDark = Color(0xFF00B0FF)
val BrahmaPurple = Color(0xFF7C4DFF)
val BrahmaVoid = Color(0xFF050811)
val BrahmaSurface = Color(0xFF0D1527)
val BrahmaSurfaceLight = Color(0xFF16223D)
val BrahmaText = Color(0xFFE0F7FA)
val BrahmaTextDim = Color(0xFF90A4AE)
val BrahmaSuccess = Color(0xFF00E676)
val BrahmaWarning = Color(0xFFFFAB00)
val BrahmaError = Color(0xFFFF1744)

private val DarkColorScheme = darkColorScheme(
    primary = BrahmaCyan,
    secondary = BrahmaPurple,
    tertiary = BrahmaCyanDark,
    background = BrahmaVoid,
    surface = BrahmaSurface,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = BrahmaText,
    onSurface = BrahmaText
)

@Composable
fun BrahmaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
