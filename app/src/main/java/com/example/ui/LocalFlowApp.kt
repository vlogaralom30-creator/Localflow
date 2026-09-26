package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    // 1. Full Video Player Screen (Overlays the app when active and not minimized)
    if (currentPlayingVideo != null && !isMiniPlayerMode) {
        LongVideoPlayerScreen(
            video = currentPlayingVideo!!,
            viewModel = viewModel,
            onEnterPip = onEnterPip
        )
        return
    }

    // 2. Full Audio Player Screen (Overlays the app when open and not minimized)
    if (currentAudioTrack != null && isAudioPlayerOpen) {
        AudioPlayerScreen(
            track = currentAudioTrack!!,
            viewModel = audioViewModel
        )
        return
    }

    // 3. Search Screen (Overlays when active)
    if (isSearchOpen) {
        SearchScreen(viewModel = viewModel)
        return
    }

    // 4. Main Scaffold with 6 Bottom Navigation Tabs, Floating Mini Players & Ambient Liquid Glass Backdrop
    AmbientLiquidBackdrop(
        modifier = modifier.fillMaxSize(),
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
                    // Floating Video Mini Player docked right above bottom navigation bar
                    AnimatedVisibility(
                        visible = currentPlayingVideo != null && isMiniPlayerMode,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        currentPlayingVideo?.let { playing ->
                            LiquidGlassMiniPlayer(
                                video = playing,
                                exoPlayer = viewModel.activeExoPlayer,
                                isPlaying = isPlaying,
                                onExpand = { viewModel.expandPlayer() },
                                onTogglePlayPause = { viewModel.togglePlayPause() },
                                onClose = { viewModel.closePlayer() }
                            )
                        }
                    }

                    // Floating Audio Mini Player docked right above bottom navigation bar
                    AnimatedVisibility(
                        visible = currentAudioTrack != null && isMiniAudioPlayer,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        currentAudioTrack?.let { audioTrack ->
                            LiquidGlassAudioMiniPlayer(
                                track = audioTrack,
                                isPlaying = isAudioPlaying,
                                onExpand = { audioViewModel.expandPlayer() },
                                onTogglePlayPause = { audioViewModel.togglePlayPause() },
                                onSkipNext = { audioViewModel.skipNext() },
                                onClose = { audioViewModel.closePlayer() }
                            )
                        }
                    }

                    LocalFlowBottomBar(
                        currentTab = currentTab,
                        onTabSelected = { viewModel.setNavTab(it) }
                    )
                }
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
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
