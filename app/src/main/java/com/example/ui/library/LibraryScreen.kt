package com.example.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.LongVideoFeedCard
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.CardDark
import com.example.ui.theme.CardElevated
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.LibraryFilter
import com.example.viewmodel.VideoPlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: VideoPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val selectedFilter by viewModel.selectedLibraryFilter.collectAsStateWithLifecycle()
    val allVideos by viewModel.allVideos.collectAsStateWithLifecycle()
    val shortVideos by viewModel.shortVideos.collectAsStateWithLifecycle()
    val longVideos by viewModel.longVideos.collectAsStateWithLifecycle()
    val watchLater by viewModel.watchLaterVideos.collectAsStateWithLifecycle()
    val recentlyWatched by viewModel.recentlyWatchedVideos.collectAsStateWithLifecycle()
    val folders by viewModel.folders.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("library_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Library",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.openSearch() }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBlack)
            )
        },
        containerColor = AmoledBlack
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Filter chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val filters = listOf(
                        Pair(LibraryFilter.ALL, "All"),
                        Pair(LibraryFilter.SHORTS, "Shorts"),
                        Pair(LibraryFilter.LONG_VIDEOS, "Long Videos"),
                        Pair(LibraryFilter.RECENTLY_ADDED, "Recently Added")
                    )
                    items(filters) { (filt, label) ->
                        val isSelected = selectedFilter == filt
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setLibraryFilter(filt) },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    color = if (isSelected) AmoledBlack else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanAccent,
                                containerColor = CardElevated
                            ),
                            border = null,
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Summary List Navigation Cards
            if (selectedFilter == LibraryFilter.ALL) {
                item {
                    LibraryNavigationRow(
                        icon = Icons.Default.VideoLibrary,
                        title = "All Videos",
                        countText = "${allVideos.size} videos",
                        onClick = { viewModel.setLibraryFilter(LibraryFilter.ALL) }
                    )
                    LibraryNavigationRow(
                        icon = Icons.Default.PlayCircle,
                        title = "Shorts",
                        countText = "${shortVideos.size} videos",
                        onClick = { viewModel.setLibraryFilter(LibraryFilter.SHORTS) }
                    )
                    LibraryNavigationRow(
                        icon = Icons.Default.Videocam,
                        title = "Long Videos",
                        countText = "${longVideos.size} videos",
                        onClick = { viewModel.setLibraryFilter(LibraryFilter.LONG_VIDEOS) }
                    )
                    LibraryNavigationRow(
                        icon = Icons.Default.NewReleases,
                        title = "Recently Added",
                        countText = "${allVideos.size} videos",
                        onClick = { viewModel.setLibraryFilter(LibraryFilter.RECENTLY_ADDED) }
                    )
                    LibraryNavigationRow(
                        icon = Icons.Default.History,
                        title = "Recently Watched",
                        countText = "${recentlyWatched.size} videos",
                        onClick = { viewModel.setLibraryFilter(LibraryFilter.RECENTLY_WATCHED) }
                    )
                    LibraryNavigationRow(
                        icon = Icons.Default.Bookmark,
                        title = "Watch Later",
                        countText = "${watchLater.size} videos",
                        onClick = { viewModel.setLibraryFilter(LibraryFilter.WATCH_LATER) }
                    )
                    LibraryNavigationRow(
                        icon = Icons.Default.Folder,
                        title = "Folders",
                        countText = "${folders.size} folders",
                        onClick = { viewModel.setLibraryFilter(LibraryFilter.FOLDERS) }
                    )
                }
            } else {
                // Display filtered list
                val filteredVideos = when (selectedFilter) {
                    LibraryFilter.SHORTS -> shortVideos
                    LibraryFilter.LONG_VIDEOS -> longVideos
                    LibraryFilter.WATCH_LATER -> watchLater
                    LibraryFilter.RECENTLY_WATCHED -> recentlyWatched
                    else -> allVideos
                }

                if (filteredVideos.isEmpty()) {
                    item {
                        Text(
                            text = "No videos in this section.",
                            color = TextMuted,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
                } else {
                    items(filteredVideos, key = { "lib_${it.id}" }) { video ->
                        LongVideoFeedCard(
                            video = video,
                            onClick = { viewModel.playVideo(video) },
                            onWatchLaterToggle = { viewModel.toggleWatchLater(video) },
                            onAddToAlbum = { viewModel.openAddToAlbum(video) },
                            onShowDetails = { viewModel.showDetails(video) },
                            onDelete = { viewModel.deleteVideo(video) }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun LibraryNavigationRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    countText: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CardDark,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = CyanAccent,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = countText,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
