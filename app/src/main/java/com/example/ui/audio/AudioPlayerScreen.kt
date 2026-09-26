package com.example.ui.audio

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AudioEntity
import com.example.ui.components.LiveMusicVisualizer
import com.example.ui.components.SpinningGlassVinylDisc
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.LiquidGlassButton
import com.example.ui.theme.LocalLiquidPreset
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.getPaletteForPreset
import com.example.ui.theme.liquidGlass
import com.example.viewmodel.AudioPlayerViewModel
import com.example.viewmodel.AudioRepeatMode

@Composable
fun AudioPlayerScreen(
    track: AudioEntity,
    viewModel: AudioPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val isBuffering by viewModel.isBuffering.collectAsStateWithLifecycle()
    val currentPositionMs by viewModel.currentPositionMs.collectAsStateWithLifecycle()
    val durationMs by viewModel.durationMs.collectAsStateWithLifecycle()
    val isShuffle by viewModel.isShuffle.collectAsStateWithLifecycle()
    val repeatMode by viewModel.repeatMode.collectAsStateWithLifecycle()
    val visualizerBands by viewModel.visualizerBands.collectAsStateWithLifecycle()
    val sleepTimer by viewModel.sleepTimerRemainingMinutes.collectAsStateWithLifecycle()
    val preset = LocalLiquidPreset.current
    val palette = getPaletteForPreset(preset)

    BackHandler {
        viewModel.minimizePlayer()
    }

    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderRatio by remember { mutableFloatStateOf(0f) }
    var showTimerMenu by remember { mutableStateOf(false) }

    val safeDuration = if (durationMs > 0) durationMs else track.durationMs.coerceAtLeast(1L)
    val scrubRatio = if (isDraggingSlider) sliderRatio else (currentPositionMs.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f)
    val remainingMs = (safeDuration - currentPositionMs).coerceAtLeast(0L)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("audio_player_screen")
    ) {
        // Dynamic Ambient Glow behind the Vinyl Disc
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.Center)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            palette.primaryGlow.copy(alpha = if (isPlaying) 0.35f else 0.12f),
                            palette.deepAccent.copy(alpha = if (isPlaying) 0.20f else 0.05f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Bar Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Minimize Button
                LiquidGlassButton(
                    onClick = { viewModel.minimizePlayer() },
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Minimize Player",
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // "Now Playing" Center Glass Capsule
                Box(
                    modifier = Modifier
                        .liquidGlass(
                            shape = RoundedCornerShape(16.dp),
                            borderColor = palette.primaryGlow.copy(alpha = 0.4f),
                            glassAlpha = 0.2f
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Now Playing",
                        color = palette.primaryGlow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Sleep Timer Pill
                    Box {
                        LiquidGlassButton(
                            onClick = { showTimerMenu = true },
                            modifier = Modifier.size(42.dp),
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Timer,
                                contentDescription = "Sleep Timer",
                                tint = if (sleepTimer != null) palette.primaryGlow else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showTimerMenu,
                            onDismissRequest = { showTimerMenu = false }
                        ) {
                            listOf(15, 30, 45, 60).forEach { mins ->
                                DropdownMenuItem(
                                    text = { Text("$mins Minutes") },
                                    onClick = {
                                        viewModel.setSleepTimer(mins)
                                        showTimerMenu = false
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Turn Off Timer") },
                                onClick = {
                                    viewModel.setSleepTimer(null)
                                    showTimerMenu = false
                                }
                            )
                        }
                    }

                    // Favorite Button
                    LiquidGlassButton(
                        onClick = { viewModel.toggleFavorite(track) },
                        modifier = Modifier.size(42.dp),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = if (track.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (track.isFavorite) Color(0xFFFF4081) else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Spinning Frosted Glass Vinyl Record
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                SpinningGlassVinylDisc(
                    albumArtUri = track.albumArtUri,
                    isPlaying = isPlaying,
                    size = 250.dp
                )

                if (isBuffering) {
                    CircularProgressIndicator(
                        color = palette.primaryGlow,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Track Metadata
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = track.title,
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${track.artist} • ${track.album}",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quality & Audio Format Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    modifier = Modifier.liquidGlass(
                        shape = RoundedCornerShape(8.dp),
                        borderColor = Color.White.copy(alpha = 0.25f),
                        glassAlpha = 0.15f
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AUDIO HD",
                            color = palette.primaryGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• ${track.formattedSize()}",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Music Vibe Animated Visualizer
            LiveMusicVisualizer(
                frequencies = visualizerBands,
                isPlaying = isPlaying,
                barHeight = 44.dp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Specular Scrubber & Timeline
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = scrubRatio,
                    onValueChange = {
                        isDraggingSlider = true
                        sliderRatio = it
                    },
                    onValueChangeFinished = {
                        val target = (sliderRatio * safeDuration).toLong()
                        viewModel.seekTo(target)
                        isDraggingSlider = false
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = palette.primaryGlow,
                        activeTrackColor = palette.primaryGlow,
                        inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatDuration(if (isDraggingSlider) (sliderRatio * safeDuration).toLong() else currentPositionMs),
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "-${formatDuration(remainingMs)}",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 6. Action Controls: Shuffle, Prev, Play/Pause Bulb, Next, Repeat
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle
                LiquidGlassButton(
                    onClick = { viewModel.toggleShuffle() },
                    modifier = Modifier.size(46.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffle) palette.primaryGlow else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Previous
                LiquidGlassButton(
                    onClick = { viewModel.skipPrevious() },
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = "Previous Track",
                        tint = TextPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Center Prominent Play/Pause Bulb
                LiquidGlassButton(
                    onClick = { viewModel.togglePlayPause() },
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    isProminent = true
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(38.dp)
                    )
                }

                // Next
                LiquidGlassButton(
                    onClick = { viewModel.skipNext() },
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Next Track",
                        tint = TextPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Repeat Mode
                LiquidGlassButton(
                    onClick = { viewModel.toggleRepeat() },
                    modifier = Modifier.size(46.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = if (repeatMode == AudioRepeatMode.ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                        contentDescription = "Repeat",
                        tint = if (repeatMode != AudioRepeatMode.OFF) palette.primaryGlow else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
