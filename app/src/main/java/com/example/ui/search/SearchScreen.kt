package com.example.ui.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.LongVideoFeedCard
import com.example.ui.theme.AmbientLiquidBackdrop
import com.example.ui.theme.LiquidGlassCircleButton
import com.example.ui.theme.LiquidGlassDimens
import com.example.ui.theme.LiquidGlassFilterChip
import com.example.ui.theme.LiquidGlassSearchField
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.LocalLiquidPreset
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.getPaletteForPreset
import com.example.ui.theme.liquidGlass
import com.example.viewmodel.SearchFilter
import com.example.viewmodel.VideoPlayerViewModel
import com.example.viewmodel.VideoSortOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: VideoPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchFilter by viewModel.searchFilter.collectAsStateWithLifecycle()
    val searchSort by viewModel.searchSort.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    val allVideos by viewModel.allVideos.collectAsStateWithLifecycle()
    val preset by viewModel.liquidPreset.collectAsStateWithLifecycle()

    val isDark = LocalIsDarkTheme.current
    val palette = getPaletteForPreset(preset)

    BackHandler {
        viewModel.closeSearch()
    }

    // Filter and sort videos according to active query
    val matchingVideos = allVideos.filter {
        query.isBlank() || it.title.contains(query, ignoreCase = true) || it.folderName.contains(query, ignoreCase = true)
    }.let { list ->
        when (searchSort) {
            VideoSortOrder.NEWEST -> list.sortedByDescending { it.dateAdded }
            VideoSortOrder.OLDEST -> list.sortedBy { it.dateAdded }
            VideoSortOrder.NAME -> list.sortedBy { it.title.lowercase() }
            VideoSortOrder.DURATION -> list.sortedByDescending { it.durationMs }
            VideoSortOrder.SIZE -> list.sortedByDescending { it.sizeBytes }
            VideoSortOrder.MOST_WATCHED -> list.sortedByDescending { it.watchCount }
        }
    }

    AmbientLiquidBackdrop(
        modifier = modifier.fillMaxSize(),
        preset = preset
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("search_screen"),
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        LiquidGlassCircleButton(
                            onClick = { viewModel.closeSearch() },
                            size = 38.dp,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    title = {
                        LiquidGlassSearchField(
                            value = query,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = "Search videos, folders...",
                            onClear = { viewModel.setSearchQuery("") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 8.dp)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Recent Searches (If query is blank)
                if (query.isBlank() && recentSearches.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Searches",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            TextButton(onClick = { viewModel.clearRecentSearches() }) {
                                Text("Clear", color = palette.primaryGlow, fontSize = 12.sp)
                            }
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(recentSearches) { term ->
                                val chipShape = RoundedCornerShape(LiquidGlassDimens.RadiusPill)
                                Box(
                                    modifier = Modifier
                                        .clip(chipShape)
                                        .liquidGlass(
                                            shape = chipShape,
                                            isDark = isDark
                                        )
                                        .clickable { viewModel.applyRecentSearch(term) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.History, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = term, color = TextPrimary, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Search Filters Chips
                item {
                    Text(
                        text = "Search Filters",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val filters = listOf(
                            Pair(SearchFilter.ALL, "All"),
                            Pair(SearchFilter.VIDEOS, "Videos"),
                            Pair(SearchFilter.FOLDERS, "Folders"),
                            Pair(SearchFilter.ALBUMS, "Albums")
                        )
                        items(filters) { (filt, label) ->
                            val isSelected = searchFilter == filt
                            LiquidGlassFilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setSearchFilter(filt) },
                                label = label
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Sort By Chips
                item {
                    Text(
                        text = "Sort By",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val sorts = listOf(
                            Pair(VideoSortOrder.NEWEST, "Newest"),
                            Pair(VideoSortOrder.OLDEST, "Oldest"),
                            Pair(VideoSortOrder.NAME, "Name"),
                            Pair(VideoSortOrder.DURATION, "Duration"),
                            Pair(VideoSortOrder.SIZE, "Size"),
                            Pair(VideoSortOrder.MOST_WATCHED, "Most Watched")
                        )
                        items(sorts) { (srt, label) ->
                            val isSelected = searchSort == srt
                            LiquidGlassFilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setSearchSort(srt) },
                                label = label
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Search Results
                if (matchingVideos.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = palette.primaryGlow.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (query.isBlank()) "Search your local library" else "No matching videos found for \"$query\"",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    items(matchingVideos, key = { "search_${it.id}" }) { video ->
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

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}
