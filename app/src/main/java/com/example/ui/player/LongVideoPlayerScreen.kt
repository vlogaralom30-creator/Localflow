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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.local.VideoEntity
import com.example.ui.components.DeleteConfirmationDialog
import com.example.ui.components.RelatedVideoCard
import com.example.ui.components.RenameVideoDialog
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LiquidGlassButton
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.VideoPlayerViewModel
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun LongVideoPlayerScreen(
    video: VideoEntity,
    viewModel: VideoPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val relatedVideos by viewModel.relatedVideos.collectAsStateWithLifecycle()
    val isBgAudio by viewModel.backgroundAudio.collectAsStateWithLifecycle()

    BackHandler {
        viewModel.closePlayer()
    }

    var isPlaying by remember(video.id) { mutableStateOf(true) }
    var isBuffering by remember(video.id) { mutableStateOf(true) }
    var currentPosition by remember(video.id) { mutableLongStateOf(video.lastPositionMs) }
    var totalDuration by remember(video.id) { mutableLongStateOf(video.durationMs.coerceAtLeast(1L)) }
    var controlsVisible by remember { mutableStateOf(true) }
    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderRatio by remember { mutableFloatStateOf(0f) }
    var resizeModeIndex by remember { mutableStateOf(0) } // FIT, ZOOM, FILL
    var showSpeedMenu by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }

    // ExoPlayer creation keyed to video.id and video.uri
    val exoPlayer = remember(video.id, video.uri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(video.uri)))
            if (video.lastPositionMs > 0) {
                seekTo(video.lastPositionMs)
            }
            prepare()
            playWhenReady = true
        }
    }

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
            val finalPos = exoPlayer.currentPosition
            viewModel.updatePlaybackPosition(video.id, finalPos)
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Polling playback progress
    LaunchedEffect(isPlaying, isDraggingSlider, video.id) {
        while (isPlaying && !isDraggingSlider) {
            val pos = exoPlayer.currentPosition
            val dur = exoPlayer.duration
            if (pos >= 0) currentPosition = pos
            if (dur > 0) totalDuration = dur
            delay(500)
        }
    }

    // Auto-hide controls
    LaunchedEffect(controlsVisible, isPlaying) {
        if (controlsVisible && isPlaying) {
            delay(4000)
            controlsVisible = false
        }
    }

    val resizeModes = listOf(
        AspectRatioFrameLayout.RESIZE_MODE_FIT,
        AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
        AspectRatioFrameLayout.RESIZE_MODE_FILL
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .statusBarsPadding()
            .testTag("long_video_player_screen")
    ) {
        // 1. 16:9 ExoPlayer Player Surface (With key(video.id) to guarantee surface reconnection on video switch)
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
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            controlsVisible = !controlsVisible
                        }
                )
            }

            // Buffering Indicator
            if (isBuffering) {
                CircularProgressIndicator(
                    color = CyanAccent,
                    strokeWidth = 3.dp,
                    modifier = Modifier.align(Alignment.Center).size(50.dp)
                )
            }

            // Controls Overlay
            AnimatedVisibility(
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
                    // Top Bar Controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LiquidGlassButton(
                            onClick = { viewModel.closePlayer() },
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.weight(1f))

                        // Aspect ratio mode
                        LiquidGlassButton(
                            onClick = { resizeModeIndex = (resizeModeIndex + 1) % resizeModes.size },
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.AspectRatio, contentDescription = "Aspect ratio", tint = Color.White, modifier = Modifier.size(19.dp))
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Playback Speed
                        Box {
                            LiquidGlassButton(
                                onClick = { showSpeedMenu = true },
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape
                            ) {
                                Icon(Icons.Default.Speed, contentDescription = "Speed", tint = Color.White, modifier = Modifier.size(19.dp))
                            }
                            DropdownMenu(
                                expanded = showSpeedMenu,
                                onDismissRequest = { showSpeedMenu = false }
                            ) {
                                listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { spd ->
                                    DropdownMenuItem(
                                        text = { Text("${spd}x") },
                                        onClick = {
                                            playbackSpeed = spd
                                            exoPlayer.setPlaybackSpeed(spd)
                                            showSpeedMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Center Rewind / Play-Pause / Forward (Liquid Glass Controls)
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(28.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LiquidGlassButton(
                            onClick = {
                                val target = (exoPlayer.currentPosition - 10000L).coerceAtLeast(0L)
                                exoPlayer.seekTo(target)
                                currentPosition = target
                            },
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.FastRewind, contentDescription = "Rewind 10s", tint = Color.White, modifier = Modifier.size(26.dp))
                        }

                        // Prominent Center Liquid Play/Pause Button
                        LiquidGlassButton(
                            onClick = {
                                if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                            },
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape,
                            isProminent = true
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.Black,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        LiquidGlassButton(
                            onClick = {
                                val target = (exoPlayer.currentPosition + 10000L).coerceAtMost(totalDuration)
                                exoPlayer.seekTo(target)
                                currentPosition = target
                            },
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.FastForward, contentDescription = "Forward 10s", tint = Color.White, modifier = Modifier.size(26.dp))
                        }
                    }

                    // Bottom Scrubber Bar
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        val durationSafe = totalDuration.coerceAtLeast(1L)
                        val ratio = if (isDraggingSlider) sliderRatio else (currentPosition.toFloat() / durationSafe.toFloat()).coerceIn(0f, 1f)

                        Slider(
                            value = ratio,
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
                                thumbColor = CyanAccent,
                                activeTrackColor = CyanAccent,
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
                                text = formatTime(totalDuration),
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // 2. Video Info, Action Row & Smooth Scrollable Related Videos Feed
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            // Title & Technical Badges
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
                        color = CyanAccent.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "📁 ${video.folderName}",
                            color = CyanAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Row Buttons: Watch Later, Add to Album, Background, Share, More
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlayerActionButton(
                        icon = if (video.isWatchLater) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        label = "Watch Later",
                        tint = if (video.isWatchLater) CyanAccent else TextSecondary,
                        onClick = { viewModel.toggleWatchLater(video) }
                    )
                    PlayerActionButton(
                        icon = Icons.Default.PlaylistAdd,
                        label = "Add to Album",
                        onClick = { viewModel.openAddToAlbum(video) }
                    )
                    PlayerActionButton(
                        icon = Icons.Default.Headphones,
                        label = "Background",
                        tint = if (isBgAudio) CyanAccent else TextSecondary,
                        onClick = { viewModel.toggleBackgroundAudio() }
                    )
                    // Direct Share Button (WhatsApp, FB, Messenger, Telegram, etc.)
                    PlayerActionButton(
                        icon = Icons.Default.Share,
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
                    // More Button with Dropdown (Rename, Delete, Info, Add to Album)
                    Box {
                        PlayerActionButton(
                            icon = Icons.Default.MoreVert,
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
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = CyanAccent) }
                            )
                            DropdownMenuItem(
                                text = { Text("Add to Album") },
                                onClick = {
                                    showMoreMenu = false
                                    viewModel.openAddToAlbum(video)
                                },
                                leadingIcon = { Icon(Icons.Default.PlaylistAdd, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Details & Info") },
                                onClick = {
                                    showMoreMenu = false
                                    viewModel.showDetails(video)
                                },
                                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Remove Video", color = ErrorRed) },
                                onClick = {
                                    showMoreMenu = false
                                    showDeleteConfirmDialog = true
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = BorderDark)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Related Videos",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Smooth Related Videos List Items
            if (relatedVideos.isEmpty()) {
                item {
                    Text(
                        text = "No other related videos found in local library.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(relatedVideos, key = { "rel_${it.id}" }) { rel ->
                    RelatedVideoCard(
                        video = rel,
                        onClick = { viewModel.playVideo(rel) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    // Rename Video Dialog
    if (showRenameDialog) {
        RenameVideoDialog(
            initialTitle = video.title,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName ->
                viewModel.renameVideo(video.id, newName)
                showRenameDialog = false
            }
        )
    }

    // Delete Video Confirmation Dialog
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
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x331E293B))
            .border(
                width = 0.8.dp,
                brush = Brush.verticalGradient(
                    listOf(Color(0x4DFFFFFF), Color(0x18FFFFFF))
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PlayerActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color = TextSecondary,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "action_btn_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x38FFFFFF),
                            Color(0x2418253A),
                            Color(0x400A0F1A)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x66FFFFFF),
                            Color(0x1AFFFFFF),
                            Color(0x3300E5FF)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Normal)
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
