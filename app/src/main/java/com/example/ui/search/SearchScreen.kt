package com.example.ui.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import com.example.ui.theme.AmbientLiquidBackdrop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.LongVideoFeedCard
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.CardElevated
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
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

    AmbientLiquidBackdrop(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("search_screen"),
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { viewModel.closeSearch() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                    },
                    title = {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search videos, folders, albums...", color = TextMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            trailingIcon = {
                                if (query.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_input_field")
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
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        TextButton(onClick = { viewModel.clearRecentSearches() }) {
                            Text("Clear", color = CyanAccent, fontSize = 12.sp)
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(recentSearches) { term ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = CardElevated,
                                modifier = Modifier.clickable { viewModel.applyRecentSearch(term) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
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
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val filters = listOf(
                        Pair(SearchFilter.ALL, "All"),
                        Pair(SearchFilter.VIDEOS, "Videos"),
                        Pair(SearchFilter.FOLDERS, "Folders"),
                        Pair(SearchFilter.ALBUMS, "Albums")
                    )
                    items(filters) { (filt, label) ->
                        val isSelected = searchFilter == filt
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setSearchFilter(filt) },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanAccent,
                                containerColor = CardElevated
                            ),
                            border = null
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Sort By Chips
            item {
                Text(
                    text = "Sort By",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
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
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setSearchSort(srt) },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanAccent,
                                containerColor = CardElevated
                            ),
                            border = null
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Search Results
            if (matchingVideos.isEmpty()) {
                item {
                    Text(
                        text = "No matching videos found.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
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
