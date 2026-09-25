package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.albums.AlbumsScreen
import com.example.ui.components.AddToAlbumBottomSheet
import com.example.ui.components.CreateAlbumDialog
import com.example.ui.components.LocalFlowBottomBar
import com.example.ui.components.VideoDetailsBottomSheet
import com.example.ui.home.HomeScreen
import com.example.ui.library.LibraryScreen
import com.example.ui.player.LongVideoPlayerScreen
import com.example.ui.search.SearchScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.shorts.ShortsScreen
import com.example.ui.theme.AmoledBlack
import com.example.viewmodel.NavTab
import com.example.viewmodel.VideoPlayerViewModel

@Composable
fun LocalFlowApp(
    viewModel: VideoPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentNavTab.collectAsStateWithLifecycle()
    val currentPlayingVideo by viewModel.currentPlayingVideo.collectAsStateWithLifecycle()
    val isSearchOpen by viewModel.isSearchOpen.collectAsStateWithLifecycle()
    val detailsVideo by viewModel.detailsVideo.collectAsStateWithLifecycle()
    val addToAlbumVideo by viewModel.addToAlbumVideo.collectAsStateWithLifecycle()
    val albums by viewModel.albumsWithCount.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showCreateAlbumDialog by remember { mutableStateOf(false) }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    // 1. Long Video Player Screen (Overlays the feed)
    if (currentPlayingVideo != null) {
        LongVideoPlayerScreen(
            video = currentPlayingVideo!!,
            viewModel = viewModel
        )
        return
    }

    // 2. Search Screen (Overlays when active)
    if (isSearchOpen) {
        SearchScreen(viewModel = viewModel)
        return
    }

    // 3. Main Scaffold with 5 Bottom Navigation Tabs
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBlack),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            LocalFlowBottomBar(
                currentTab = currentTab,
                onTabSelected = { viewModel.setNavTab(it) }
            )
        },
        containerColor = AmoledBlack
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavTab.HOME -> HomeScreen(viewModel = viewModel)
                NavTab.SHORTS -> ShortsScreen(viewModel = viewModel)
                NavTab.LIBRARY -> LibraryScreen(viewModel = viewModel)
                NavTab.ALBUMS -> AlbumsScreen(viewModel = viewModel)
                NavTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
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
            onCreateNewAlbum = { showCreateAlbumDialog = true }
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
