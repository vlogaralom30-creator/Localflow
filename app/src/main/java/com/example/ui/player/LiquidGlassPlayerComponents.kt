package com.example.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LiquidGlassMotion
import com.example.ui.theme.LocalLiquidPreset
import com.example.ui.theme.getPaletteForPreset
import com.example.ui.theme.liquidGlass
import kotlin.math.cos
import kotlin.math.sin

/**
 * 10-Second Counter-Clockwise Rewind Icon (Matching Apple TV / visionOS Reference)
 */
@Composable
fun Replay10Icon(
    modifier: Modifier = Modifier,
    tint: Color = Color.White
) {
    Box(
        modifier = modifier.size(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 1.8.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2f - 1.5.dp.toPx()
            val center = Offset(size.width / 2f, size.height / 2f)

            // Draw counter-clockwise arc with top opening
            drawArc(
                color = tint,
                startAngle = 55f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f)
            )

            // Arrow tip at the beginning of arc
            val rad = Math.toRadians(55.0)
            val arrowHeadX = (center.x + radius * cos(rad)).toFloat()
            val arrowHeadY = (center.y + radius * sin(rad)).toFloat()

            val arrowPath = Path().apply {
                moveTo(arrowHeadX - 4.dp.toPx(), arrowHeadY - 3.dp.toPx())
                lineTo(arrowHeadX, arrowHeadY)
                lineTo(arrowHeadX + 4.dp.toPx(), arrowHeadY - 1.dp.toPx())
            }
            drawPath(arrowPath, color = tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
        }

        Text(
            text = "10",
            color = tint,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp,
            modifier = Modifier.padding(top = 1.dp)
        )
    }
}

/**
 * 10-Second Clockwise Forward Icon (Matching Apple TV / visionOS Reference)
 */
@Composable
fun Forward10Icon(
    modifier: Modifier = Modifier,
    tint: Color = Color.White
) {
    Box(
        modifier = modifier.size(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 1.8.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2f - 1.5.dp.toPx()
            val center = Offset(size.width / 2f, size.height / 2f)

            // Draw clockwise arc
            drawArc(
                color = tint,
                startAngle = 125f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f)
            )

            // Arrow tip at the end of arc
            val rad = Math.toRadians(125.0 + 270.0)
            val arrowHeadX = (center.x + radius * cos(rad)).toFloat()
            val arrowHeadY = (center.y + radius * sin(rad)).toFloat()

            val arrowPath = Path().apply {
                moveTo(arrowHeadX - 4.dp.toPx(), arrowHeadY - 1.dp.toPx())
                lineTo(arrowHeadX, arrowHeadY)
                lineTo(arrowHeadX + 4.dp.toPx(), arrowHeadY - 3.dp.toPx())
            }
            drawPath(arrowPath, color = tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
        }

        Text(
            text = "10",
            color = tint,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp,
            modifier = Modifier.padding(top = 1.dp)
        )
    }
}

/**
 * Apple TV / visionOS Frosted Liquid Glass Floating Capsule Container
 */
@Composable
fun GlassCapsule(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    elevation: Dp = 8.dp,
    content: @Composable RowScopeOrBox.() -> Unit
) {
    val preset = LocalLiquidPreset.current
    val palette = getPaletteForPreset(preset)
    val capsuleShape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = capsuleShape,
                spotColor = Color.Black.copy(alpha = 0.5f),
                ambientColor = Color.Black.copy(alpha = 0.35f)
            )
            .clip(capsuleShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x551E293B),
                        Color(0x350F172A),
                        Color(0x45030712)
                    )
                )
            )
            .liquidGlass(
                shape = capsuleShape,
                isDark = true,
                borderColor = Color.White.copy(alpha = 0.35f),
                glassAlpha = 0.12f,
                accentGlow = palette.primaryGlow
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        content(RowScopeOrBox(this))
    }
}

class RowScopeOrBox(val boxScope: BoxScope)

/**
 * Standalone Circular Frosted Glass Button (e.g. Close X, 10s Rewind, 10s Forward)
 */
@Composable
fun CircularGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) LiquidGlassMotion.DeepPressScale else 1.0f,
        animationSpec = LiquidGlassMotion.SpringBouncy,
        label = "circ_glass_btn_scale"
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = 6.dp,
                shape = CircleShape,
                spotColor = Color.Black.copy(alpha = 0.6f)
            )
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x50334155),
                        Color(0x301E293B),
                        Color(0x450F172A)
                    )
                )
            )
            .liquidGlass(
                shape = CircleShape,
                isDark = true,
                borderColor = Color.White.copy(alpha = 0.45f),
                glassAlpha = 0.16f
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}

/**
 * Prominent Apple TV / visionOS Center Play / Pause Glass Disc
 */
@Composable
fun ProminentPlayPauseGlassButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp
) {
    val preset = LocalLiquidPreset.current
    val palette = getPaletteForPreset(preset)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "prominent_play_scale"
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = 14.dp,
                shape = CircleShape,
                spotColor = palette.primaryGlow.copy(alpha = 0.35f),
                ambientColor = Color.Black.copy(alpha = 0.5f)
            )
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x65475569),
                        Color(0x351E293B),
                        Color(0x40020617)
                    )
                )
            )
            .liquidGlass(
                shape = CircleShape,
                isDark = true,
                borderColor = Color.White.copy(alpha = 0.6f),
                glassAlpha = 0.22f,
                accentGlow = palette.primaryGlow,
                borderWidth = 1.8.dp
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = Color.White,
            modifier = Modifier.size(36.dp)
        )
    }
}

/**
 * Bottom Pill Button (Info, InSight, Continue Watching)
 */
@Composable
fun GlassPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false
) {
    val preset = LocalLiquidPreset.current
    val palette = getPaletteForPreset(preset)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) LiquidGlassMotion.PressScaleFactor else 1.0f,
        animationSpec = LiquidGlassMotion.SpringBouncy,
        label = "pill_scale"
    )

    val pillShape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isActive) 6.dp else 3.dp,
                shape = pillShape,
                spotColor = if (isActive) palette.primaryGlow.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.3f)
            )
            .clip(pillShape)
            .background(
                brush = if (isActive) {
                    Brush.verticalGradient(
                        colors = listOf(
                            palette.primaryGlow.copy(alpha = 0.35f),
                            palette.deepAccent.copy(alpha = 0.25f)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x40334155),
                            Color(0x201E293B),
                            Color(0x350F172A)
                        )
                    )
                }
            )
            .liquidGlass(
                shape = pillShape,
                isDark = true,
                borderColor = if (isActive) palette.primaryGlow.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.35f),
                glassAlpha = 0.12f
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isActive) palette.primaryGlow else Color.White,
            fontSize = 12.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
        )
    }
}

/**
 * Top-Right Interactive Volume Pill Capsule
 */
@Composable
fun GlassVolumeCapsule(
    volumeFraction: Float,
    isMuted: Boolean,
    onVolumeChange: (Float) -> Unit,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val preset = LocalLiquidPreset.current
    val palette = getPaletteForPreset(preset)
    val capsuleShape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .shadow(8.dp, capsuleShape, spotColor = Color.Black.copy(alpha = 0.4f))
            .clip(capsuleShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0x551E293B), Color(0x350F172A), Color(0x45030712))
                )
            )
            .liquidGlass(
                shape = capsuleShape,
                isDark = true,
                borderColor = Color.White.copy(alpha = 0.35f),
                glassAlpha = 0.14f
            )
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Interactive horizontal volume bar (width ~90.dp)
            Box(
                modifier = Modifier
                    .width(84.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.25f))
                    .clickable {
                        // Quick step toggle if tapped directly
                        val next = if (volumeFraction >= 0.8f) 0.3f else volumeFraction + 0.3f
                        onVolumeChange(next.coerceIn(0f, 1f))
                    }
            ) {
                // Filled progress indicator
                val filledWidthFraction = if (isMuted) 0f else volumeFraction.coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = filledWidthFraction)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.White, palette.primaryGlow)
                            )
                        )
                )
            }

            // Speaker icon (toggles mute)
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .clickable { onToggleMute() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isMuted || volumeFraction <= 0.02f) {
                        Icons.AutoMirrored.Filled.VolumeMute
                    } else {
                        Icons.AutoMirrored.Filled.VolumeUp
                    },
                    contentDescription = "Mute/Unmute Volume",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Gestures Overlay Floating HUDs (Brightness, Volume, Seeking, Double-Tap)
 */
@Composable
fun GestureFeedbackHUD(
    visibleBrightness: Boolean,
    brightnessPercent: Int,
    visibleVolume: Boolean,
    volumePercent: Int,
    visibleSeek: Boolean,
    seekDeltaSeconds: Int,
    seekTargetTime: String,
    totalDurationTime: String,
    doubleTapRewind: Boolean,
    doubleTapForward: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Brightness HUD (Center-Left)
        AnimatedVisibility(
            visible = visibleBrightness,
            enter = fadeIn() + scaleIn(initialScale = 0.85f),
            exit = fadeOut() + scaleOut(targetScale = 0.85f),
            modifier = Modifier.align(Alignment.Center)
        ) {
            FloatingHUDCard(
                icon = { Icon(Icons.Default.BrightnessHigh, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp)) },
                title = "Brightness",
                percent = brightnessPercent
            )
        }

        // Volume HUD (Center-Right)
        AnimatedVisibility(
            visible = visibleVolume,
            enter = fadeIn() + scaleIn(initialScale = 0.85f),
            exit = fadeOut() + scaleOut(targetScale = 0.85f),
            modifier = Modifier.align(Alignment.Center)
        ) {
            FloatingHUDCard(
                icon = { Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp)) },
                title = "Volume",
                percent = volumePercent
            )
        }

        // Seeking Scrub HUD (Center)
        AnimatedVisibility(
            visible = visibleSeek,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut() + scaleOut(targetScale = 0.9f),
            modifier = Modifier.align(Alignment.Center)
        ) {
            val deltaStr = if (seekDeltaSeconds >= 0) "+${seekDeltaSeconds}s" else "${seekDeltaSeconds}s"
            Box(
                modifier = Modifier
                    .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.6f))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xCC0F172A))
                    .border(1.2.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = deltaStr,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$seekTargetTime / $totalDurationTime",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Double-Tap Left Rewind Indicator
        AnimatedVisibility(
            visible = doubleTapRewind,
            enter = fadeIn() + scaleIn(initialScale = 0.7f),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 64.dp)
        ) {
            DoubleTapRippleBadge(isRewind = true)
        }

        // Double-Tap Right Forward Indicator
        AnimatedVisibility(
            visible = doubleTapForward,
            enter = fadeIn() + scaleIn(initialScale = 0.7f),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 64.dp)
        ) {
            DoubleTapRippleBadge(isRewind = false)
        }
    }
}

@Composable
private fun FloatingHUDCard(
    icon: @Composable () -> Unit,
    title: String,
    percent: Int
) {
    val preset = LocalLiquidPreset.current
    val palette = getPaletteForPreset(preset)

    Box(
        modifier = Modifier
            .size(130.dp)
            .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.7f))
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xCC0B1120))
            .border(1.5.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            icon()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$percent%",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Mini progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = (percent / 100f).coerceIn(0f, 1f))
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(palette.primaryGlow)
                )
            }
        }
    }
}

@Composable
private fun DoubleTapRippleBadge(isRewind: Boolean) {
    Box(
        modifier = Modifier
            .size(74.dp)
            .shadow(12.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.6f))
            .clip(CircleShape)
            .background(Color(0xAA0F172A))
            .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (isRewind) {
                Replay10Icon(tint = Color.White)
            } else {
                Forward10Icon(tint = Color.White)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isRewind) "-10s" else "+10s",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
