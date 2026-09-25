package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.NaxxivoApp
import com.example.data.engine.RelatedVideosEngine
import com.example.data.local.AlbumEntity
import com.example.data.local.AlbumWithCount
import com.example.data.local.VideoEntity
import com.example.data.repository.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NavTab {
    HOME,
    SHORTS,
    LIBRARY,
    ALBUMS,
    SETTINGS
}

enum class HomeCategory {
    ALL,
    RECENTLY_ADDED,
    CONTINUE_WATCHING,
    SHORTS,
    LONG_VIDEOS
}

enum class LibraryFilter {
    ALL,
    SHORTS,
    LONG_VIDEOS,
    RECENTLY_ADDED,
    RECENTLY_WATCHED,
    WATCH_LATER,
    FOLDERS
}

enum class VideoSortOrder {
    NEWEST,
    OLDEST,
    NAME,
    DURATION,
    SIZE,
    MOST_WATCHED
}

enum class SearchFilter {
    ALL,
    VIDEOS,
    FOLDERS,
    ALBUMS
}

class VideoPlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VideoRepository = (application as NaxxivoApp).videoRepository

    // Current Bottom Navigation Tab
    private val _currentNavTab = MutableStateFlow(NavTab.HOME)
    val currentNavTab: StateFlow<NavTab> = _currentNavTab.asStateFlow()

    // Home Screen Category Chip
    private val _selectedHomeCategory = MutableStateFlow(HomeCategory.ALL)
    val selectedHomeCategory: StateFlow<HomeCategory> = _selectedHomeCategory.asStateFlow()

    // Library Filter
    private val _selectedLibraryFilter = MutableStateFlow(LibraryFilter.ALL)
    val selectedLibraryFilter: StateFlow<LibraryFilter> = _selectedLibraryFilter.asStateFlow()

    // Search
    private val _isSearchOpen = MutableStateFlow(false)
    val isSearchOpen: StateFlow<Boolean> = _isSearchOpen.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchFilter = MutableStateFlow(SearchFilter.ALL)
    val searchFilter: StateFlow<SearchFilter> = _searchFilter.asStateFlow()

    private val _searchSort = MutableStateFlow(VideoSortOrder.NEWEST)
    val searchSort: StateFlow<VideoSortOrder> = _searchSort.asStateFlow()

    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    // Permission and Scanning
    private val _isPermissionGranted = MutableStateFlow(false)
    val isPermissionGranted: StateFlow<Boolean> = _isPermissionGranted.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Selected short video target index when clicked from shelf/feed
    private val _targetShortVideoId = MutableStateFlow<Long?>(null)
    val targetShortVideoId: StateFlow<Long?> = _targetShortVideoId.asStateFlow()

    // Video streams
    val allVideos: StateFlow<List<VideoEntity>> = repository.allVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shortVideos: StateFlow<List<VideoEntity>> = repository.shortVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val longVideos: StateFlow<List<VideoEntity>> = repository.longVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val continueWatching: StateFlow<List<VideoEntity>> = repository.continueWatching
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchLaterVideos: StateFlow<List<VideoEntity>> = repository.watchLater
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyWatchedVideos: StateFlow<List<VideoEntity>> = repository.recentlyWatched
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val folders: StateFlow<List<String>> = repository.folders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val albumsWithCount: StateFlow<List<AlbumWithCount>> = repository.albumsWithCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAlbums: StateFlow<List<AlbumEntity>> = repository.allAlbums
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected album for detail view
    private val _selectedAlbum = MutableStateFlow<AlbumWithCount?>(null)
    val selectedAlbum: StateFlow<AlbumWithCount?> = _selectedAlbum.asStateFlow()

    val videosForSelectedAlbum: StateFlow<List<VideoEntity>> = _selectedAlbum.flatMapLatest { album ->
        if (album != null) repository.getVideosForAlbum(album.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected folder for filter view
    private val _selectedFolder = MutableStateFlow<String?>(null)
    val selectedFolder: StateFlow<String?> = _selectedFolder.asStateFlow()

    val videosForSelectedFolder: StateFlow<List<VideoEntity>> = _selectedFolder.flatMapLatest { folder ->
        if (folder != null) repository.getVideosByFolder(folder) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Playback: Long Video
    private val _currentPlayingVideo = MutableStateFlow<VideoEntity?>(null)
    val currentPlayingVideo: StateFlow<VideoEntity?> = _currentPlayingVideo.asStateFlow()

    // Smart Related Videos (Computed on the fly)
    val relatedVideos: StateFlow<List<VideoEntity>> = combine(
        _currentPlayingVideo,
        allVideos
    ) { current, all ->
        if (current != null) {
            RelatedVideosEngine.getRelatedVideos(current, all, limit = 8)
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Modal dialogs
    private val _detailsVideo = MutableStateFlow<VideoEntity?>(null)
    val detailsVideo: StateFlow<VideoEntity?> = _detailsVideo.asStateFlow()

    private val _addToAlbumVideo = MutableStateFlow<VideoEntity?>(null)
    val addToAlbumVideo: StateFlow<VideoEntity?> = _addToAlbumVideo.asStateFlow()

    // Player Preferences / Settings
    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _isAutoPlayNext = MutableStateFlow(true)
    val isAutoPlayNext: StateFlow<Boolean> = _isAutoPlayNext.asStateFlow()

    private val _rememberPosition = MutableStateFlow(true)
    val rememberPosition: StateFlow<Boolean> = _rememberPosition.asStateFlow()

    private val _backgroundAudio = MutableStateFlow(false)
    val backgroundAudio: StateFlow<Boolean> = _backgroundAudio.asStateFlow()

    private val _defaultOrientation = MutableStateFlow("Auto")
    val defaultOrientation: StateFlow<String> = _defaultOrientation.asStateFlow()

    // Light / Dark Theme state (Persisted in SharedPreferences)
    private val themePrefs = (application as Application).getSharedPreferences("localflow_theme_prefs", Context.MODE_PRIVATE)
    private val _isDarkTheme = MutableStateFlow(themePrefs.getBoolean("is_dark_theme", true))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleTheme() {
        val next = !_isDarkTheme.value
        _isDarkTheme.value = next
        themePrefs.edit().putBoolean("is_dark_theme", next).apply()
    }

    fun setDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
        themePrefs.edit().putBoolean("is_dark_theme", isDark).apply()
    }

    init {
        viewModelScope.launch {
            repository.purgeAllDemoData()
            checkPermission()
        }
    }

    fun checkPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_VIDEO
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        val granted = ContextCompat.checkSelfPermission(
            getApplication(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
        _isPermissionGranted.value = granted

        if (granted) {
            scanVideos()
        }
    }

    fun onPermissionResult(granted: Boolean) {
        _isPermissionGranted.value = granted
        if (granted) {
            scanVideos()
        } else {
            _statusMessage.value = "Storage permission is required to display your local videos."
        }
    }

    fun scanVideos() {
        viewModelScope.launch {
            _isScanning.value = true
            repository.purgeAllDemoData()
            val result = repository.scanMediaStore()
            _isScanning.value = false
            result.onSuccess { count ->
                _statusMessage.value = if (count > 0) "Found $count local video(s)" else "No videos found in device storage"
            }.onFailure {
                _statusMessage.value = "Storage scan error: ${it.localizedMessage}"
            }
        }
    }

    fun importVideoUri(uri: Uri) {
        viewModelScope.launch {
            _isScanning.value = true
            val imported = repository.importVideoFromUri(uri)
            _isScanning.value = false
            if (imported != null) {
                _statusMessage.value = "Imported \"${imported.title}\""
                playVideo(imported)
            } else {
                _statusMessage.value = "Could not import chosen video file"
            }
        }
    }

    fun setNavTab(tab: NavTab) {
        _currentNavTab.value = tab
    }

    fun setHomeCategory(category: HomeCategory) {
        _selectedHomeCategory.value = category
    }

    fun setLibraryFilter(filter: LibraryFilter) {
        _selectedLibraryFilter.value = filter
    }

    fun selectAlbum(album: AlbumWithCount?) {
        _selectedAlbum.value = album
    }

    fun selectFolder(folder: String?) {
        _selectedFolder.value = folder
    }

    fun playVideo(video: VideoEntity) {
        if (video.isShort) {
            _targetShortVideoId.value = video.id
            _currentNavTab.value = NavTab.SHORTS
        } else {
            _currentPlayingVideo.value = video
            viewModelScope.launch {
                repository.incrementWatchCount(video.id)
            }
        }
    }

    fun clearTargetShortVideo() {
        _targetShortVideoId.value = null
    }

    fun closePlayer() {
        _currentPlayingVideo.value = null
    }

    fun updatePlaybackPosition(id: Long, positionMs: Long) {
        if (_rememberPosition.value) {
            viewModelScope.launch {
                repository.updatePlaybackPosition(id, positionMs)
            }
        }
    }

    fun toggleWatchLater(video: VideoEntity) {
        viewModelScope.launch {
            val newVal = !video.isWatchLater
            repository.toggleWatchLater(video.id, newVal)
            _statusMessage.value = if (newVal) "Saved to Watch Later" else "Removed from Watch Later"
        }
    }

    fun toggleFavorite(video: VideoEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(video.id, !video.isFavorite)
        }
    }

    fun deleteVideo(video: VideoEntity) {
        viewModelScope.launch {
            repository.deleteVideo(video)
            if (_currentPlayingVideo.value?.id == video.id) {
                _currentPlayingVideo.value = null
            }
            _statusMessage.value = "Removed from LocalFlow"
        }
    }

    fun showDetails(video: VideoEntity) {
        _detailsVideo.value = video
    }

    fun closeDetails() {
        _detailsVideo.value = null
    }

    fun openAddToAlbum(video: VideoEntity) {
        _addToAlbumVideo.value = video
    }

    fun closeAddToAlbum() {
        _addToAlbumVideo.value = null
    }

    fun createAlbum(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.createAlbum(name)
            _statusMessage.value = "Created album \"$name\""
        }
    }

    fun addVideoToAlbum(albumId: Long, videoId: Long, albumName: String) {
        viewModelScope.launch {
            repository.addVideoToAlbum(albumId, videoId)
            _addToAlbumVideo.value = null
            _statusMessage.value = "Added to $albumName"
        }
    }

    // Search functions
    fun openSearch() {
        _isSearchOpen.value = true
    }

    fun closeSearch() {
        _isSearchOpen.value = false
        _searchQuery.value = ""
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearchFilter(filter: SearchFilter) {
        _searchFilter.value = filter
    }

    fun setSearchSort(sort: VideoSortOrder) {
        _searchSort.value = sort
    }

    fun applyRecentSearch(term: String) {
        _searchQuery.value = term
    }

    fun clearRecentSearches() {
        _recentSearches.value = emptyList()
    }

    // Settings
    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
    }

    fun toggleAutoPlayNext() {
        _isAutoPlayNext.value = !_isAutoPlayNext.value
    }

    fun toggleRememberPosition() {
        _rememberPosition.value = !_rememberPosition.value
    }

    fun toggleBackgroundAudio() {
        _backgroundAudio.value = !_backgroundAudio.value
    }

    fun setDefaultOrientation(orientation: String) {
        _defaultOrientation.value = orientation
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
