package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.albums.AlbumsScreen
import com.example.ui.audio.AudioLibraryScreen
import com.example.ui.audio.AudioPlayerScreen
import com.example.ui.components.AddToAlbumBottomSheet
import com.example.ui.components.CreateAlbumDialog
import com.example.ui.components.LiquidGlassAudioMiniPlayer
import com.example.ui.components.LiquidGlassMiniPlayer
import com.example.ui.components.LocalFlowBottomBar
import com.example.ui.components.VideoDetailsBottomSheet
import com.example.ui.home.HomeScreen
import com.example.ui.library.LibraryScreen
import com.example.ui.player.LongVideoPlayerScreen
import com.example.ui.search.SearchScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.shorts.ShortsScreen
import com.example.ui.theme.AmbientLiquidBackdrop
import com.example.ui.theme.LiquidGlassSnackbar
import com.example.viewmodel.AudioPlayerViewModel
import com.example.viewmodel.NavTab
import com.example.viewmodel.VideoPlayerViewModel
import kotlinx.coroutines.delay

@Composable
fun LocalFlowApp(
    viewModel: VideoPlayerViewModel,
    audioViewModel: AudioPlayerViewModel,
    modifier: Modifier = Modifier,
    onEnterPip: () -> Unit = {}
) {
    val currentTab by viewModel.currentNavTab.collectAsStateWithLifecycle()
    val currentPlayingVideo by viewModel.currentPlayingVideo.collectAsStateWithLifecycle()
    val isMiniPlayerMode by viewModel.isMiniPlayerMode.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val isSearchOpen by viewModel.isSearchOpen.collectAsStateWithLifecycle()
    val detailsVideo by viewModel.detailsVideo.collectAsStateWithLifecycle()
    val addToAlbumVideo by viewModel.addToAlbumVideo.collectAsStateWithLifecycle()
    val albums by viewModel.albumsWithCount.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val liquidPreset by viewModel.liquidPreset.collectAsStateWithLifecycle()

    // Audio Playback State
    val currentAudioTrack by audioViewModel.currentTrack.collectAsStateWithLifecycle()
    val isAudioPlayerOpen by audioViewModel.isAudioPlayerOpen.collectAsStateWithLifecycle()
    val isMiniAudioPlayer by audioViewModel.isMiniAudioPlayer.collectAsStateWithLifecycle()
    val isAudioPlaying by audioViewModel.isPlaying.collectAsStateWithLifecycle()
    val audioStatusMessage by audioViewModel.statusMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showCreateAlbumDialog by remember { mutableStateOf(false) }

    // Bottom Navigation Bar Auto-Hide State (5-second Inactivity Timer)
    var isBottomBarVisible by remember { mutableStateOf(true) }
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Mutual Playback Coordination (prevent audio & video from overlapping simultaneously)
    LaunchedEffect(isAudioPlaying) {
        if (isAudioPlaying) {
            viewModel.pausePlayback()
        }
    }
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            audioViewModel.pausePlayback()
        }
    }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    LaunchedEffect(audioStatusMessage) {
        audioStatusMessage?.let {
            snackbarHostState.showSnackbar(it)
            audioViewModel.clearStatusMessage()
        }
    }

    // 5-second inactivity auto-hide rule:
    // When in Shorts, Music tab, or when player is active, after 5s of no touch, smoothly slide down bottom bar.
    // When in Home, Library, Albums, Settings, default keep visible.
    val isShortsTab = currentTab == NavTab.SHORTS
    val isMusicTab = currentTab == NavTab.MUSIC
    val isPlayerActive = (currentPlayingVideo != null && !isMiniPlayerMode) || (currentAudioTrack != null && isAudioPlayerOpen)
    val shouldAutoHide = isShortsTab || isMusicTab || isPlayerActive

    LaunchedEffect(lastInteractionTime, shouldAutoHide, currentTab) {
        if (shouldAutoHide) {
            delay(5000L)
            isBottomBarVisible = false
        } else {
            isBottomBarVisible = true
        }
    }

    // Reset timer and show bottom bar on tab selection
    fun onSelectTab(tab: NavTab) {
        lastInteractionTime = System.currentTimeMillis()
        isBottomBarVisible = true
        viewModel.setNavTab(tab)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                // Global passive touch detector (wakes up bottom bar on any screen touch/tap/scroll)
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        if (event.changes.any { it.pressed }) {
                            lastInteractionTime = System.currentTimeMillis()
                            if (!isBottomBarVisible) {
                                isBottomBarVisible = true
                            }
                        }
                    }
                }
            }
    ) {
        // Main App Scaffold with Tabs and Bottom Bar
        AmbientLiquidBackdrop(
            modifier = Modifier.fillMaxSize(),
            preset = liquidPreset
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                snackbarHost = {
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.navigationBarsPadding(),
                        snackbar = { data ->
                            LiquidGlassSnackbar(message = data.visuals.message)
                        }
                    )
                },
                bottomBar = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Floating Video Mini Player (docked above bottom bar)
                        AnimatedVisibility(
                            visible = currentPlayingVideo != null && isMiniPlayerMode,
                            enter = slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
                            ) + fadeIn(),
                            exit = slideOutVertically(
                                targetOffsetY = { it },
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            ) + fadeOut()
                        ) {
                            currentPlayingVideo?.let { playing ->
                                LiquidGlassMiniPlayer(
                                    video = playing,
                                    exoPlayer = viewModel.activeExoPlayer,
                                    isPlaying = isPlaying,
                                    onExpand = {
                                        lastInteractionTime = System.currentTimeMillis()
                                        viewModel.expandPlayer()
                                    },
                                    onTogglePlayPause = {
                                        lastInteractionTime = System.currentTimeMillis()
                                        viewModel.togglePlayPause()
                                    },
                                    onClose = { viewModel.closePlayer() }
                                )
                            }
                        }

                        // Floating Audio Mini Player (docked above bottom bar, active when audio is loaded)
                        AnimatedVisibility(
                            visible = currentAudioTrack != null && isMiniAudioPlayer && (currentPlayingVideo == null || !isMiniPlayerMode),
                            enter = slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
                            ) + fadeIn(),
                            exit = slideOutVertically(
                                targetOffsetY = { it },
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            ) + fadeOut()
                        ) {
                            currentAudioTrack?.let { audioTrack ->
                                LiquidGlassAudioMiniPlayer(
                                    track = audioTrack,
                                    isPlaying = isAudioPlaying,
                                    onExpand = {
                                        lastInteractionTime = System.currentTimeMillis()
                                        audioViewModel.expandPlayer()
                                    },
                                    onTogglePlayPause = {
                                        lastInteractionTime = System.currentTimeMillis()
                                        audioViewModel.togglePlayPause()
                                    },
                                    onSkipNext = {
                                        lastInteractionTime = System.currentTimeMillis()
                                        audioViewModel.skipNext()
                                    },
                                    onClose = { audioViewModel.closePlayer() }
                                )
                            }
                        }

                        // Floating Liquid Glass Navigation Bar with Auto-Hide & Spring Physics
                        AnimatedVisibility(
                            visible = isBottomBarVisible,
                            enter = slideInVertically(
                                initialOffsetY = { it * 2 },
                                animationSpec = spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow)
                            ) + fadeIn(tween(220)),
                            exit = slideOutVertically(
                                targetOffsetY = { it * 2 },
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            ) + fadeOut(tween(180))
                        ) {
                            LocalFlowBottomBar(
                                currentTab = currentTab,
                                onTabSelected = { onSelectTab(it) }
                            )
                        }
                    }
                },
                containerColor = Color.Transparent
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            // When bottom bar is hidden in Shorts or Player, extend full screen edge-to-edge
                            if (isShortsTab && !isBottomBarVisible) {
                                androidx.compose.foundation.layout.PaddingValues(0.dp)
                            } else {
                                innerPadding
                            }
                        )
                ) {
                    // Fluid animated tab-to-tab screen transitions
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = {
                            val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                            (slideInHorizontally(
                                initialOffsetX = { fullWidth -> direction * (fullWidth / 4) },
                                animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
                            ) + fadeIn(tween(240)))
                                .togetherWith(
                                    slideOutHorizontally(
                                        targetOffsetX = { fullWidth -> -direction * (fullWidth / 4) },
                                        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
                                    ) + fadeOut(tween(180))
                                )
                        },
                        label = "tab_screen_transition"
                    ) { tab ->
                        when (tab) {
                            NavTab.HOME -> HomeScreen(viewModel = viewModel)
                            NavTab.SHORTS -> ShortsScreen(viewModel = viewModel)
                            NavTab.MUSIC -> AudioLibraryScreen(viewModel = audioViewModel)
                            NavTab.LIBRARY -> LibraryScreen(viewModel = viewModel)
                            NavTab.ALBUMS -> AlbumsScreen(viewModel = viewModel)
                            NavTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }

        // Overlay 1: Search Screen with Fade & Slide Transition
        AnimatedVisibility(
            visible = isSearchOpen,
            enter = fadeIn(tween(250)) + slideInVertically(initialOffsetY = { it / 6 }),
            exit = fadeOut(tween(200)) + slideOutVertically(targetOffsetY = { it / 6 })
        ) {
            SearchScreen(viewModel = viewModel)
        }

        // Overlay 2: Full Audio Player Screen with Slide & Spring Transition
        AnimatedVisibility(
            visible = currentAudioTrack != null && isAudioPlayerOpen,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)
            ) + fadeIn(tween(250)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ) + fadeOut(tween(200))
        ) {
            currentAudioTrack?.let { track ->
                AudioPlayerScreen(
                    track = track,
                    viewModel = audioViewModel
                )
            }
        }

        // Overlay 3: Full Video Player Screen with Cinema Scale, Vertical Slide & Fade Transition
        AnimatedVisibility(
            visible = currentPlayingVideo != null && !isMiniPlayerMode,
            enter = slideInVertically(
                initialOffsetY = { it / 3 },
                animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
            ) + fadeIn(tween(300)) + scaleIn(initialScale = 0.93f),
            exit = slideOutVertically(
                targetOffsetY = { it / 3 },
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ) + fadeOut(tween(240)) + scaleOut(targetScale = 0.93f)
        ) {
            currentPlayingVideo?.let { video ->
                LongVideoPlayerScreen(
                    video = video,
                    viewModel = viewModel,
                    onEnterPip = onEnterPip
                )
            }
        }

        // Video Details Bottom Sheet
        if (detailsVideo != null) {
            VideoDetailsBottomSheet(
                video = detailsVideo!!,
                onDismiss = { viewModel.closeDetails() },
                onPlay = { viewModel.playVideo(detailsVideo!!) },
                onWatchLaterToggle = { viewModel.toggleWatchLater(detailsVideo!!) },
                onAddToAlbum = {
                    val v = detailsVideo!!
                    viewModel.closeDetails()
                    viewModel.openAddToAlbum(v)
                },
                onDelete = {
                    viewModel.deleteVideo(detailsVideo!!)
                    viewModel.closeDetails()
                }
            )
        }

        // Add to Album Bottom Sheet
        if (addToAlbumVideo != null) {
            AddToAlbumBottomSheet(
                video = addToAlbumVideo!!,
                albums = albums,
                onDismiss = { viewModel.closeAddToAlbum() },
                onAlbumSelected = { albumId, albumName ->
                    viewModel.addVideoToAlbum(albumId, addToAlbumVideo!!.id, albumName)
                },
                onCreateNewAlbum = {
                    viewModel.closeAddToAlbum()
                    showCreateAlbumDialog = true
                }
            )
        }

        // Create Album Dialog
        if (showCreateAlbumDialog) {
            CreateAlbumDialog(
                onDismiss = { showCreateAlbumDialog = false },
                onConfirm = { name ->
                    viewModel.createAlbum(name)
                    showCreateAlbumDialog = false
                }
            )
        }
    }
}
