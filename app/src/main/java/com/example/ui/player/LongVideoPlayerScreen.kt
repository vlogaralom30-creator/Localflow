package com.example.ui.player

import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.local.VideoEntity
import com.example.ui.components.DeleteConfirmationDialog
import com.example.ui.components.RelatedVideoCard
import com.example.ui.components.RenameVideoDialog
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.BorderDark
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LiquidGlassButton
import com.example.ui.theme.LocalLiquidPreset
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.getPaletteForPreset
import com.example.ui.theme.liquidGlass
import com.example.viewmodel.VideoPlayerViewModel
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun LongVideoPlayerScreen(
    video: VideoEntity,
    viewModel: VideoPlayerViewModel,
    modifier: Modifier = Modifier,
    onEnterPip: () -> Unit = {}
) {
    val context = LocalContext.current
    val relatedVideos by viewModel.relatedVideos.collectAsStateWithLifecycle()
    val isBgAudio by viewModel.backgroundAudio.collectAsStateWithLifecycle()
    val isFullscreen by viewModel.isFullscreen.collectAsStateWithLifecycle()
    val preset by viewModel.liquidPreset.collectAsStateWithLifecycle()
    val currentSpeed by viewModel.playbackSpeed.collectAsStateWithLifecycle()
    val palette = getPaletteForPreset(preset)

    BackHandler {
        if (isFullscreen) {
            viewModel.setFullscreen(false)
        } else {
            viewModel.minimizePlayer()
        }
    }

    // Shared ExoPlayer instance managed centrally
    val exoPlayer = remember(video.id) {
        viewModel.getOrCreatePlayer(context, video)
    }

    var isPlaying by remember(video.id) { mutableStateOf(exoPlayer.isPlaying) }
    var isBuffering by remember(video.id) { mutableStateOf(true) }
    var currentPosition by remember(video.id) { mutableLongStateOf(exoPlayer.currentPosition) }
    var totalDuration by remember(video.id) { mutableLongStateOf(video.durationMs.coerceAtLeast(1L)) }
    var controlsVisible by remember { mutableStateOf(true) }
    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderRatio by remember { mutableFloatStateOf(0f) }
    var resizeModeIndex by remember { mutableStateOf(0) } // FIT, ZOOM, FILL
    var showSpeedMenu by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    DisposableEffect(exoPlayer, video.id) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                isBuffering = state == Player.STATE_BUFFERING
                if (state == Player.STATE_READY) {
                    val dur = exoPlayer.duration
                    if (dur > 0) totalDuration = dur
                } else if (state == Player.STATE_ENDED) {
                    isPlaying = false
                    viewModel.updatePlaybackPosition(video.id, 0L)
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            viewModel.updatePlaybackPosition(video.id, exoPlayer.currentPosition)
            exoPlayer.removeListener(listener)
        }
    }

    // Polling playback progress
    LaunchedEffect(isPlaying, isDraggingSlider, video.id) {
        while (isPlaying && !isDraggingSlider) {
            val pos = exoPlayer.currentPosition
            val dur = exoPlayer.duration
            if (pos >= 0) currentPosition = pos
            if (dur > 0) totalDuration = dur
            delay(400)
        }
    }

    // Auto-hide controls
    LaunchedEffect(controlsVisible, isPlaying) {
        if (controlsVisible && isPlaying) {
            delay(4500)
            controlsVisible = false
        }
    }

    val resizeModes = listOf(
        AspectRatioFrameLayout.RESIZE_MODE_FIT,
        AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
        AspectRatioFrameLayout.RESIZE_MODE_FILL
    )
    val resizeLabels = listOf("FIT", "ZOOM", "FILL")

    val durationSafe = totalDuration.coerceAtLeast(1L)
    val currentScrubRatio = if (isDraggingSlider) sliderRatio else (currentPosition.toFloat() / durationSafe.toFloat()).coerceIn(0f, 1f)
    val remainingMs = (durationSafe - currentPosition).coerceAtLeast(0L)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .testTag("long_video_player_screen")
    ) {
        if (isFullscreen) {
            // ==========================================
            // FULLSCREEN CINEMA MODE (Landscape visionOS)
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // 1. Video Surface
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = false
                            resizeMode = resizeModes[resizeModeIndex]
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    update = { view ->
                        if (view.player != exoPlayer) {
                            view.player = exoPlayer
                        }
                        view.resizeMode = resizeModes[resizeModeIndex]
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // 2. Gesture Controls Overlay (Brightness, Volume, Seek, Double-tap)
                PlayerGestureOverlay(
                    isPlaying = isPlaying,
                    currentPositionMs = currentPosition,
                    totalDurationMs = durationSafe,
                    onSingleTap = { controlsVisible = !controlsVisible },
                    onDoubleTapSeek = { delta ->
                        viewModel.seekBy(delta)
                        currentPosition = exoPlayer.currentPosition
                    },
                    onSeekConfirm = { target ->
                        exoPlayer.seekTo(target)
                        currentPosition = target
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // 3. Buffering Indicator
                if (isBuffering) {
                    CircularProgressIndicator(
                        color = palette.primaryGlow,
                        strokeWidth = 3.dp,
                        modifier = Modifier.align(Alignment.Center).size(56.dp)
                    )
                }

                // 4. Floating VisionOS / Liquid Glass Controls Overlay
                AnimatedVisibility(
                    visible = controlsVisible,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f))
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        // Top Bar: Back, Title, Quality Pill, PiP, Resize, More
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Back / Exit Fullscreen
                            LiquidGlassButton(
                                onClick = { viewModel.setFullscreen(false) },
                                modifier = Modifier.size(42.dp),
                                shape = CircleShape
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Exit Fullscreen",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // Title & Quality Badge
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = video.title,
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TechBadge(text = video.qualityBadge())
                                    Spacer(modifier = Modifier.width(6.dp))
                                    TechBadge(text = video.formattedSize())
                                }
                            }

                            // PiP Button
                            LiquidGlassButton(
                                onClick = { onEnterPip() },
                                modifier = Modifier.size(42.dp),
                                shape = CircleShape
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PictureInPictureAlt,
                                    contentDescription = "Picture in Picture",
                                    tint = Color.White,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Aspect Ratio Mode Pill
                            LiquidGlassButton(
                                onClick = { resizeModeIndex = (resizeModeIndex + 1) % resizeModes.size },
                                modifier = Modifier.height(38.dp),
                                shape = RoundedCornerShape(19.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AspectRatio,
                                        contentDescription = "Aspect ratio",
                                        tint = palette.primaryGlow,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = resizeLabels[resizeModeIndex],
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Exit Fullscreen Button
                            LiquidGlassButton(
                                onClick = { viewModel.setFullscreen(false) },
                                modifier = Modifier.size(42.dp),
                                shape = CircleShape
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CloseFullscreen,
                                    contentDescription = "Exit Fullscreen",
                                    tint = palette.primaryGlow,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Center Playback Controls (Matching VisionOS Liquid Glass Reference)
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalArrangement = Arrangement.spacedBy(36.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rewind 10s
                            LiquidGlassButton(
                                onClick = {
                                    viewModel.seekBy(-10000L)
                                    currentPosition = exoPlayer.currentPosition
                                },
                                modifier = Modifier.size(54.dp),
                                shape = CircleShape
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.FastRewind,
                                    contentDescription = "Rewind 10s",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            // Prominent Center Play/Pause Glass Bulb
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

                            // Forward 10s
                            LiquidGlassButton(
                                onClick = {
                                    viewModel.seekBy(10000L)
                                    currentPosition = exoPlayer.currentPosition
                                },
                                modifier = Modifier.size(54.dp),
                                shape = CircleShape
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.FastForward,
                                    contentDescription = "Forward 10s",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        // Bottom Control Deck (Scrubber Bar + Action Pills)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                        ) {
                            // Scrubber Slider
                            Slider(
                                value = currentScrubRatio,
                                onValueChange = {
                                    isDraggingSlider = true
                                    sliderRatio = it
                                },
                                onValueChangeFinished = {
                                    val targetPos = (sliderRatio * durationSafe).toLong()
                                    exoPlayer.seekTo(targetPos)
                                    currentPosition = targetPos
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
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatTime(if (isDraggingSlider) (sliderRatio * durationSafe).toLong() else currentPosition),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                // Pills Deck: Speed, Audio, Watch Later
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Speed Selector Pill
                                    Box {
                                        LiquidGlassButton(
                                            onClick = { showSpeedMenu = true },
                                            modifier = Modifier.height(34.dp),
                                            shape = RoundedCornerShape(17.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Speed,
                                                    contentDescription = "Speed",
                                                    tint = palette.primaryGlow,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${currentSpeed}x",
                                                    color = TextPrimary,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                        DropdownMenu(
                                            expanded = showSpeedMenu,
                                            onDismissRequest = { showSpeedMenu = false }
                                        ) {
                                            listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { spd ->
                                                DropdownMenuItem(
                                                    text = { Text("${spd}x") },
                                                    onClick = {
                                                        viewModel.setPlaybackSpeed(spd)
                                                        showSpeedMenu = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // Background Audio Pill
                                    LiquidGlassButton(
                                        onClick = { viewModel.toggleBackgroundAudio() },
                                        modifier = Modifier.height(34.dp),
                                        shape = RoundedCornerShape(17.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Headphones,
                                                contentDescription = "Background Audio",
                                                tint = if (isBgAudio) palette.primaryGlow else TextMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isBgAudio) "Audio On" else "Audio",
                                                color = if (isBgAudio) palette.primaryGlow else TextPrimary,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "-${formatTime(remainingMs)}",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // ==========================================
            // PORTRAIT FEED MODE (16:9 + Action Row + Related Videos)
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // 1. 16:9 Video Canvas with Gestures
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color.Black)
                ) {
                    key(video.id) {
                        AndroidView(
                            factory = { ctx ->
                                PlayerView(ctx).apply {
                                    player = exoPlayer
                                    useController = false
                                    resizeMode = resizeModes[resizeModeIndex]
                                    layoutParams = FrameLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                }
                            },
                            update = { view ->
                                if (view.player != exoPlayer) {
                                    view.player = exoPlayer
                                }
                                view.resizeMode = resizeModes[resizeModeIndex]
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Gesture Controls Overlay
                    PlayerGestureOverlay(
                        isPlaying = isPlaying,
                        currentPositionMs = currentPosition,
                        totalDurationMs = durationSafe,
                        onSingleTap = { controlsVisible = !controlsVisible },
                        onDoubleTapSeek = { delta ->
                            viewModel.seekBy(delta)
                            currentPosition = exoPlayer.currentPosition
                        },
                        onSeekConfirm = { target ->
                            exoPlayer.seekTo(target)
                            currentPosition = target
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Buffering Indicator
                    if (isBuffering) {
                        CircularProgressIndicator(
                            color = palette.primaryGlow,
                            strokeWidth = 3.dp,
                            modifier = Modifier.align(Alignment.Center).size(50.dp)
                        )
                    }

                    // Portrait Controls Overlay
                    androidx.compose.animation.AnimatedVisibility(
                        visible = controlsVisible,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.55f))
                        ) {
                            // Top Row: Minimize, Aspect Ratio, Speed, Fullscreen
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.TopCenter)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Minimize to Mini-player
                                LiquidGlassButton(
                                    onClick = { viewModel.minimizePlayer() },
                                    modifier = Modifier.size(40.dp),
                                    shape = CircleShape
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.KeyboardArrowDown,
                                        contentDescription = "Minimize Player",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                // Aspect ratio mode
                                LiquidGlassButton(
                                    onClick = { resizeModeIndex = (resizeModeIndex + 1) % resizeModes.size },
                                    modifier = Modifier.size(40.dp),
                                    shape = CircleShape
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AspectRatio,
                                        contentDescription = "Aspect ratio",
                                        tint = Color.White,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // PiP Button
                                LiquidGlassButton(
                                    onClick = { onEnterPip() },
                                    modifier = Modifier.size(40.dp),
                                    shape = CircleShape
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PictureInPictureAlt,
                                        contentDescription = "PiP",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Fullscreen Toggle
                                LiquidGlassButton(
                                    onClick = { viewModel.setFullscreen(true) },
                                    modifier = Modifier.size(40.dp),
                                    shape = CircleShape
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Fullscreen,
                                        contentDescription = "Fullscreen",
                                        tint = palette.primaryGlow,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            // Center Controls
                            Row(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalArrangement = Arrangement.spacedBy(28.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LiquidGlassButton(
                                    onClick = {
                                        viewModel.seekBy(-10000L)
                                        currentPosition = exoPlayer.currentPosition
                                    },
                                    modifier = Modifier.size(48.dp),
                                    shape = CircleShape
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.FastRewind,
                                        contentDescription = "Rewind 10s",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }

                                // Prominent Play/Pause
                                LiquidGlassButton(
                                    onClick = { viewModel.togglePlayPause() },
                                    modifier = Modifier.size(64.dp),
                                    shape = CircleShape,
                                    isProminent = true
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                        contentDescription = if (isPlaying) "Pause" else "Play",
                                        tint = Color.Black,
                                        modifier = Modifier.size(34.dp)
                                    )
                                }

                                LiquidGlassButton(
                                    onClick = {
                                        viewModel.seekBy(10000L)
                                        currentPosition = exoPlayer.currentPosition
                                    },
                                    modifier = Modifier.size(48.dp),
                                    shape = CircleShape
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.FastForward,
                                        contentDescription = "Forward 10s",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }

                            // Bottom Scrubber Bar
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Slider(
                                    value = currentScrubRatio,
                                    onValueChange = {
                                        isDraggingSlider = true
                                        sliderRatio = it
                                    },
                                    onValueChangeFinished = {
                                        val targetPos = (sliderRatio * durationSafe).toLong()
                                        exoPlayer.seekTo(targetPos)
                                        currentPosition = targetPos
                                        isDraggingSlider = false
                                    },
                                    colors = SliderDefaults.colors(
                                        thumbColor = palette.primaryGlow,
                                        activeTrackColor = palette.primaryGlow,
                                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier.fillMaxWidth().height(20.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = formatTime(if (isDraggingSlider) (sliderRatio * durationSafe).toLong() else currentPosition),
                                        color = Color.White,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "-${formatTime(remainingMs)}",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Video Info, Action Row & Related Videos
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = video.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TechBadge(text = video.qualityBadge())
                            if (video.fps != null) TechBadge(text = video.fps)
                            TechBadge(text = video.formattedSize())
                            TechBadge(text = "MP4")
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = palette.primaryGlow.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "📁 ${video.folderName}",
                                    color = palette.primaryGlow,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PlayerActionButton(
                                icon = if (video.isWatchLater) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                label = "Watch Later",
                                tint = if (video.isWatchLater) palette.primaryGlow else TextSecondary,
                                onClick = { viewModel.toggleWatchLater(video) }
                            )
                            PlayerActionButton(
                                icon = Icons.Filled.PlaylistAdd,
                                label = "Add to Album",
                                onClick = { viewModel.openAddToAlbum(video) }
                            )
                            PlayerActionButton(
                                icon = Icons.Filled.Headphones,
                                label = "Background",
                                tint = if (isBgAudio) palette.primaryGlow else TextSecondary,
                                onClick = { viewModel.toggleBackgroundAudio() }
                            )
                            PlayerActionButton(
                                icon = Icons.Filled.Share,
                                label = "Share",
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = video.mimeType ?: "video/*"
                                        val uri = Uri.parse(video.uri)
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        putExtra(Intent.EXTRA_SUBJECT, video.title)
                                        putExtra(Intent.EXTRA_TEXT, "Watch \"${video.title}\" on LocalFlow")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Video via"))
                                }
                            )
                            Box {
                                PlayerActionButton(
                                    icon = Icons.Filled.MoreVert,
                                    label = "More",
                                    onClick = { showMoreMenu = true }
                                )

                                DropdownMenu(
                                    expanded = showMoreMenu,
                                    onDismissRequest = { showMoreMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Rename Video") },
                                        onClick = {
                                            showMoreMenu = false
                                            showRenameDialog = true
                                        },
                                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null, tint = palette.primaryGlow) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Add to Album") },
                                        onClick = {
                                            showMoreMenu = false
                                            viewModel.openAddToAlbum(video)
                                        },
                                        leadingIcon = { Icon(Icons.Filled.PlaylistAdd, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Details & Info") },
                                        onClick = {
                                            showMoreMenu = false
                                            viewModel.showDetails(video)
                                        },
                                        leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Remove Video", color = ErrorRed) },
                                        onClick = {
                                            showMoreMenu = false
                                            showDeleteConfirmDialog = true
                                        },
                                        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = ErrorRed) }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = BorderDark)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Up Next • Related Videos",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (relatedVideos.isEmpty()) {
                        item {
                            Text(
                                text = "No other related videos found in this folder.",
                                color = TextMuted,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    } else {
                        items(relatedVideos, key = { it.id }) { related ->
                            RelatedVideoCard(
                                video = related,
                                onClick = { viewModel.playVideo(related) }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }

    if (showRenameDialog) {
        RenameVideoDialog(
            initialTitle = video.title,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newTitle ->
                viewModel.renameVideo(video.id, newTitle)
                showRenameDialog = false
            }
        )
    }

    if (showDeleteConfirmDialog) {
        DeleteConfirmationDialog(
            videoTitle = video.title,
            onDismiss = { showDeleteConfirmDialog = false },
            onConfirm = {
                viewModel.deleteVideo(video)
                showDeleteConfirmDialog = false
            }
        )
    }
}

@Composable
private fun TechBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color.White.copy(alpha = 0.08f),
        modifier = Modifier.border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
    ) {
        Text(
            text = text,
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun PlayerActionButton(
    icon: ImageVector,
    label: String,
    tint: Color = TextSecondary,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .liquidGlass(
                    shape = CircleShape,
                    borderColor = Color.White.copy(alpha = 0.18f),
                    glassAlpha = if (isPressed) 0.35f else 0.15f
                )
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = tint,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
