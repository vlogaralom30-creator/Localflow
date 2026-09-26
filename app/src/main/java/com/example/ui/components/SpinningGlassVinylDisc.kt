package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.LocalLiquidPreset
import com.example.ui.theme.getPaletteForPreset
import com.example.ui.theme.liquidGlass

@Composable
fun SpinningGlassVinylDisc(
    albumArtUri: String?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 240.dp
) {
    val context = LocalContext.current
    val preset = LocalLiquidPreset.current
    val palette = getPaletteForPreset(preset)

    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_disc_rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "disc_angle"
    )

    val currentRotation = if (isPlaying) rotationAngle else 0f

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 20.dp,
                shape = CircleShape,
                spotColor = palette.primaryGlow.copy(alpha = if (isPlaying) 0.6f else 0.25f),
                ambientColor = palette.deepAccent.copy(alpha = 0.4f)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Rotating Vinyl Canvas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .rotate(currentRotation)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1E2430),
                            Color(0xFF0F131A),
                            Color(0xFF07090C)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.sweepGradient(
                        listOf(
                            palette.primaryGlow.copy(alpha = 0.8f),
                            Color.White.copy(alpha = 0.2f),
                            palette.deepAccent.copy(alpha = 0.6f),
                            Color.White.copy(alpha = 0.1f),
                            palette.primaryGlow.copy(alpha = 0.8f)
                        )
                    ),
                    shape = CircleShape
                )
        ) {
            // Concentric vinyl grooves
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.toPx() / 2f
                val grooveColor = Color.White.copy(alpha = 0.05f)
                val stroke = Stroke(width = 1.dp.toPx())

                for (step in 1..6) {
                    val r = radius * (0.42f + step * 0.085f)
                    drawCircle(color = grooveColor, radius = r, style = stroke)
                }
            }

            // Center Label / Album Art Hub
            Box(
                modifier = Modifier
                    .size(size * 0.42f)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .liquidGlass(
                        shape = CircleShape,
                        borderColor = Color.White.copy(alpha = 0.6f),
                        glassAlpha = 0.35f
                    )
                    .border(1.5.dp, palette.primaryGlow.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!albumArtUri.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(albumArtUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Album Artwork",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(palette.primaryGlow.copy(alpha = 0.4f), palette.deepAccent.copy(alpha = 0.7f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MusicNote,
                            contentDescription = "Music",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Spindle hole
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF07090C))
                        .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                )
            }
        }
    }
}
