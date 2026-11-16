package com.brahma.assistant.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.brahma.assistant.ui.theme.BrahmaCyan
import com.brahma.assistant.ui.theme.BrahmaPurple

@Composable
fun VoiceWaveformVisualizer(
    isSpeaking: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")

    val scale1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = if (isSpeaking) 1.0f else 0.3f,
        animationSpec = infiniteRepeatable(tween(400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bar1"
    )
    val scale2 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = if (isSpeaking) 0.9f else 0.4f,
        animationSpec = infiniteRepeatable(tween(550, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bar2"
    )
    val scale3 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (isSpeaking) 1.2f else 0.2f,
        animationSpec = infiniteRepeatable(tween(350, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bar3"
    )
    val scale4 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = if (isSpeaking) 0.8f else 0.5f,
        animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bar4"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val scales = listOf(scale1, scale2, scale3, scale4, scale2, scale1)
        scales.forEachIndexed { index, scale ->
            val color = if (index % 2 == 0) BrahmaCyan else BrahmaPurple
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height((32 * scale).coerceIn(4f, 48f).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}
