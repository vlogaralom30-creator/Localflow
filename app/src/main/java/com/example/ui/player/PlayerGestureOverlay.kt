package com.example.ui.player

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalLiquidPreset
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.getPaletteForPreset
import com.example.ui.theme.liquidGlass
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt

enum class DragMode {
    NONE,
    BRIGHTNESS,
    VOLUME,
    SEEK
}

@Composable
fun PlayerGestureOverlay(
    isPlaying: Boolean,
    currentPositionMs: Long,
    totalDurationMs: Long,
    onSingleTap: () -> Unit,
    onDoubleTapSeek: (deltaMs: Long) -> Unit,
    onSeekConfirm: (targetPositionMs: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
    val preset = LocalLiquidPreset.current
    val palette = getPaletteForPreset(preset)

    val maxVolume = remember { audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15 }
    var currentVolume by remember {
        mutableFloatStateOf(audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: (maxVolume / 2f))
    }

    var currentBrightness by remember {
        val windowBrightness = activity?.window?.attributes?.screenBrightness ?: -1f
        mutableFloatStateOf(if (windowBrightness < 0f) 0.5f else windowBrightness)
    }

    var dragMode by remember { mutableStateOf(DragMode.NONE) }
    var showHud by remember { mutableStateOf(false) }

    // Seek scrub drag state
    var seekDeltaMs by remember { mutableLongStateOf(0L) }
    var targetSeekMs by remember { mutableLongStateOf(currentPositionMs) }

    // Double tap indicators
    var showDoubleTapLeft by remember { mutableStateOf(false) }
    var showDoubleTapRight by remember { mutableStateOf(false) }

    // Auto hide HUD after release
    LaunchedEffect(showHud, dragMode) {
        if (showHud && dragMode == DragMode.NONE) {
            delay(1200)
            showHud = false
        }
    }

    LaunchedEffect(showDoubleTapLeft) {
        if (showDoubleTapLeft) {
            delay(650)
            showDoubleTapLeft = false
        }
    }

    LaunchedEffect(showDoubleTapRight) {
        if (showDoubleTapRight) {
            delay(650)
            showDoubleTapRight = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onSingleTap() },
                    onDoubleTap = { offset ->
                        val screenWidth = size.width
                        if (offset.x < screenWidth * 0.4f) {
                            showDoubleTapLeft = true
                            onDoubleTapSeek(-10000L)
                        } else if (offset.x > screenWidth * 0.6f) {
                            showDoubleTapRight = true
                            onDoubleTapSeek(10000L)
                        } else {
                            onSingleTap()
                        }
                    }
                )
            }
            .pointerInput(currentPositionMs, totalDurationMs) {
                var totalDragX = 0f
                var totalDragY = 0f
                var isHorizontalSeek = false

                detectDragGestures(
                    onDragStart = { offset ->
                        totalDragX = 0f
                        totalDragY = 0f
                        isHorizontalSeek = false
                        val screenWidth = size.width
                        dragMode = if (offset.x < screenWidth * 0.5f) DragMode.BRIGHTNESS else DragMode.VOLUME
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        totalDragX += dragAmount.x
                        totalDragY += dragAmount.y

                        if (!isHorizontalSeek && abs(totalDragX) > abs(totalDragY) + 20 && abs(totalDragX) > 30) {
                            isHorizontalSeek = true
                            dragMode = DragMode.SEEK
                        }

                        when (dragMode) {
                            DragMode.BRIGHTNESS -> {
                                showHud = true
                                val delta = -dragAmount.y / (size.height * 0.75f)
                                currentBrightness = (currentBrightness + delta).coerceIn(0.01f, 1.0f)
                                activity?.window?.let { window ->
                                    val lp = window.attributes
                                    lp.screenBrightness = currentBrightness
                                    window.attributes = lp
                                }
                            }
                            DragMode.VOLUME -> {
                                showHud = true
                                val delta = -dragAmount.y / (size.height * 0.75f) * maxVolume
                                currentVolume = (currentVolume + delta).coerceIn(0f, maxVolume.toFloat())
                                audioManager?.setStreamVolume(
                                    AudioManager.STREAM_MUSIC,
                                    currentVolume.roundToInt(),
                                    0
                                )
                            }
                            DragMode.SEEK -> {
                                showHud = true
                                val factor = if (totalDurationMs > 300000) 250 else 100
                                seekDeltaMs = (totalDragX * factor).toLong()
                                targetSeekMs = (currentPositionMs + seekDeltaMs).coerceIn(0L, totalDurationMs)
                            }
                            DragMode.NONE -> {}
                        }
                    },
                    onDragEnd = {
                        if (dragMode == DragMode.SEEK) {
                            onSeekConfirm(targetSeekMs)
                        }
                        dragMode = DragMode.NONE
                    },
                    onDragCancel = {
                        dragMode = DragMode.NONE
                    }
                )
            }
    ) {
        // 1. Double tap left animation (Rewind 10s)
        AnimatedVisibility(
            visible = showDoubleTapLeft,
            enter = fadeIn(tween(150)) + scaleIn(tween(150)),
            exit = fadeOut(tween(250)) + scaleOut(tween(250)),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .liquidGlass(
                        shape = CircleShape,
                        borderColor = palette.primaryGlow.copy(alpha = 0.6f),
                        glassAlpha = 0.35f,
                        accentGlow = palette.primaryGlow.copy(alpha = 0.25f)
                    )
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.FastRewind,
                        contentDescription = "Rewind 10 seconds",
                        tint = TextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "-10s",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 2. Double tap right animation (Forward 10s)
        AnimatedVisibility(
            visible = showDoubleTapRight,
            enter = fadeIn(tween(150)) + scaleIn(tween(150)),
            exit = fadeOut(tween(250)) + scaleOut(tween(250)),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .liquidGlass(
                        shape = CircleShape,
                        borderColor = palette.primaryGlow.copy(alpha = 0.6f),
                        glassAlpha = 0.35f,
                        accentGlow = palette.primaryGlow.copy(alpha = 0.25f)
                    )
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.FastForward,
                        contentDescription = "Forward 10 seconds",
                        tint = TextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "+10s",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 3. Central HUD Card for Brightness / Volume / Seek
        AnimatedVisibility(
            visible = showHud,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(250)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            val hudShape = RoundedCornerShape(20.dp)
            Box(
                modifier = Modifier
                    .liquidGlass(
                        shape = hudShape,
                        borderColor = palette.primaryGlow.copy(alpha = 0.5f),
                        glassAlpha = 0.40f,
                        accentGlow = palette.primaryGlow.copy(alpha = 0.2f)
                    )
                    .clip(hudShape)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                when (dragMode) {
                    DragMode.BRIGHTNESS -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.BrightnessMedium,
                                contentDescription = "Brightness",
                                tint = palette.primaryGlow,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Brightness ${(currentBrightness * 100).toInt()}%",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { currentBrightness },
                                    color = palette.primaryGlow,
                                    trackColor = Color.White.copy(alpha = 0.15f),
                                    modifier = Modifier
                                        .width(120.dp)
                                        .height(6.dp)
                                        .clip(CircleShape)
                                )
                            }
                        }
                    }
                    DragMode.VOLUME -> {
                        val volRatio = (currentVolume / maxVolume.toFloat()).coerceIn(0f, 1f)
                        val icon = when {
                            volRatio <= 0f -> Icons.Filled.VolumeMute
                            volRatio < 0.5f -> Icons.Filled.VolumeDown
                            else -> Icons.Filled.VolumeUp
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = "Volume",
                                tint = palette.primaryGlow,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Volume ${(volRatio * 100).toInt()}%",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { volRatio },
                                    color = palette.primaryGlow,
                                    trackColor = Color.White.copy(alpha = 0.15f),
                                    modifier = Modifier
                                        .width(120.dp)
                                        .height(6.dp)
                                        .clip(CircleShape)
                                )
                            }
                        }
                    }
                    DragMode.SEEK -> {
                        val sign = if (seekDeltaMs >= 0) "+" else ""
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$sign${formatDuration(seekDeltaMs)}",
                                color = palette.primaryGlow,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${formatDuration(targetSeekMs)} / ${formatDuration(totalDurationMs)}",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    DragMode.NONE -> {}
                }
            }
        }
    }
}

private fun formatDuration(millis: Long): String {
    val absMs = abs(millis)
    val totalSeconds = absMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
