package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalLiquidPreset
import com.example.ui.theme.getPaletteForPreset

/**
 * Animated rhythmic multi-band music visualizer reflecting the beat and vibes.
 */
@Composable
fun LiveMusicVisualizer(
    frequencies: List<Float>,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barHeight: Dp = 48.dp
) {
    val preset = LocalLiquidPreset.current
    val palette = getPaletteForPreset(preset)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        frequencies.forEach { freq ->
            val animatedHeight by animateFloatAsState(
                targetValue = if (isPlaying) freq else 0.12f,
                animationSpec = tween(durationMillis = 100),
                label = "freq_bar"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(animatedHeight.coerceIn(0.08f, 1.0f))
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                palette.primaryGlow,
                                palette.deepAccent.copy(alpha = 0.6f),
                                Color.White.copy(alpha = 0.2f)
                            )
                        )
                    )
            )
            Box(modifier = Modifier.width(3.dp))
        }
    }
}
