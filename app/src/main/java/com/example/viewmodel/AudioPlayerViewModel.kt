package com.example.viewmodel

import android.app.Application
import android.content.ComponentName
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.NaxxivoApp
import com.example.data.local.AudioEntity
import com.example.data.local.VideoEntity
import com.example.data.repository.AudioRepository
import com.example.service.AudioPlaybackService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class AudioRepeatMode {
    OFF,
    ALL,
    ONE
}

enum class AudioFilterTab {
    ALL,
    FAVORITES,
    RECENT
}

class AudioPlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AudioRepository = (application as NaxxivoApp).audioRepository

    // Audio library streams
    val allAudioTracks: StateFlow<List<AudioEntity>> = repository.allAudioTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteAudioTracks: StateFlow<List<AudioEntity>> = repository.favoriteAudioTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayedAudioTracks: StateFlow<List<AudioEntity>> = repository.recentlyPlayedAudioTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state
    private val _currentFilterTab = MutableStateFlow(AudioFilterTab.ALL)
    val currentFilterTab: StateFlow<AudioFilterTab> = _currentFilterTab.asStateFlow()

    private val _isAudioPlayerOpen = MutableStateFlow(false)
    val isAudioPlayerOpen: StateFlow<Boolean> = _isAudioPlayerOpen.asStateFlow()

    private val _isMiniAudioPlayer = MutableStateFlow(false)
    val isMiniAudioPlayer: StateFlow<Boolean> = _isMiniAudioPlayer.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Playback state
    private val _currentTrack = MutableStateFlow<AudioEntity?>(null)
    val currentTrack: StateFlow<AudioEntity?> = _currentTrack.asStateFlow()

    private val _audioQueue = MutableStateFlow<List<AudioEntity>>(emptyList())
    val audioQueue: StateFlow<List<AudioEntity>> = _audioQueue.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(1L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(AudioRepeatMode.OFF)
    val repeatMode: StateFlow<AudioRepeatMode> = _repeatMode.asStateFlow()

    // 24-Band Animated Spectrum Equalizer Frequencies for Music Vibe
    private val _visualizerBands = MutableStateFlow(List(24) { 0.2f })
    val visualizerBands: StateFlow<List<Float>> = _visualizerBands.asStateFlow()

    private val _sleepTimerRemainingMinutes = MutableStateFlow<Int?>(null)
    val sleepTimerRemainingMinutes: StateFlow<Int?> = _sleepTimerRemainingMinutes.asStateFlow()

    private var sleepTimerJob: Job? = null
    private var progressJob: Job? = null
    private var visualizerJob: Job? = null

    // MediaController connected to background AudioPlaybackService
    private var mediaController: MediaController? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(playing: Boolean) {
            _isPlaying.value = playing
            if (playing) {
                startProgressPolling()
                startVisualizerAnimation()
            } else {
                stopVisualizerAnimation()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            _isBuffering.value = playbackState == Player.STATE_BUFFERING
            val controller = mediaController ?: return
            if (playbackState == Player.STATE_READY) {
                val dur = controller.duration
                if (dur > 0) _durationMs.value = dur
            } else if (playbackState == Player.STATE_ENDED) {
                handleTrackEnded()
            }
        }
    }

    init {
        initMediaController()
        scanAudio()
    }

    private fun initMediaController() {
        val context = getApplication<Application>()
        val sessionToken = SessionToken(context, ComponentName(context, AudioPlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            try {
                val controller = controllerFuture.get()
                controller.addListener(playerListener)
                mediaController = controller
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun scanAudio() {
        viewModelScope.launch {
            _isScanning.value = true
            val res = repository.scanMediaStoreAudio()
            _isScanning.value = false
            res.onSuccess { count ->
                if (count > 0) _statusMessage.value = "Found $count audio track(s)"
            }
        }
    }

    fun setFilterTab(tab: AudioFilterTab) {
        _currentFilterTab.value = tab
    }

    fun playTrack(track: AudioEntity, queue: List<AudioEntity> = emptyList()) {
        _currentTrack.value = track
        _audioQueue.value = if (queue.isNotEmpty()) queue else listOf(track)
        _isAudioPlayerOpen.value = true
        _isMiniAudioPlayer.value = false

        viewModelScope.launch {
            repository.incrementPlayCount(track.id)
        }

        val controller = mediaController ?: return
        val metadata = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artist)
            .setAlbumTitle(track.album)
            .setArtworkUri(track.albumArtUri?.let { Uri.parse(it) })
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(track.uri))
            .setMediaMetadata(metadata)
            .build()

        controller.setMediaItem(mediaItem)
        if (track.lastPositionMs > 0) {
            controller.seekTo(track.lastPositionMs)
        }
        controller.prepare()
        controller.play()
    }

    fun playVideoAsAudio(video: VideoEntity) {
        viewModelScope.launch {
            val audioTrack = repository.importVideoAsAudio(video)
            playTrack(audioTrack)
        }
    }

    fun togglePlayPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            controller.play()
        }
    }

    fun skipNext() {
        val queue = _audioQueue.value
        val current = _currentTrack.value
        if (queue.isEmpty() || current == null) return

        val nextTrack = if (_isShuffle.value) {
            queue.random()
        } else {
            val idx = queue.indexOfFirst { it.id == current.id }
            if (idx in 0 until queue.size - 1) {
                queue[idx + 1]
            } else if (_repeatMode.value == AudioRepeatMode.ALL) {
                queue.firstOrNull()
            } else {
                null
            }
        }

        if (nextTrack != null) {
            playTrack(nextTrack, queue)
        }
    }

    fun skipPrevious() {
        val controller = mediaController ?: return
        if (controller.currentPosition > 3000) {
            controller.seekTo(0)
            return
        }

        val queue = _audioQueue.value
        val current = _currentTrack.value
        if (queue.isEmpty() || current == null) return

        val idx = queue.indexOfFirst { it.id == current.id }
        val prevTrack = if (idx > 0) queue[idx - 1] else queue.lastOrNull()
        if (prevTrack != null) {
            playTrack(prevTrack, queue)
        }
    }

    private fun handleTrackEnded() {
        when (_repeatMode.value) {
            AudioRepeatMode.ONE -> {
                mediaController?.seekTo(0)
                mediaController?.play()
            }
            AudioRepeatMode.ALL -> {
                skipNext()
            }
            AudioRepeatMode.OFF -> {
                val queue = _audioQueue.value
                val current = _currentTrack.value
                val idx = queue.indexOfFirst { it.id == current?.id }
                if (idx in 0 until queue.size - 1) {
                    skipNext()
                } else {
                    _isPlaying.value = false
                }
            }
        }
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
        _currentPositionMs.value = positionMs
    }

    fun seekBy(deltaMs: Long) {
        val controller = mediaController ?: return
        val target = (controller.currentPosition + deltaMs).coerceIn(0L, _durationMs.value)
        controller.seekTo(target)
        _currentPositionMs.value = target
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            AudioRepeatMode.OFF -> AudioRepeatMode.ALL
            AudioRepeatMode.ALL -> AudioRepeatMode.ONE
            AudioRepeatMode.ONE -> AudioRepeatMode.OFF
        }
    }

    fun toggleFavorite(track: AudioEntity) {
        viewModelScope.launch {
            val next = !track.isFavorite
            repository.toggleFavorite(track.id, next)
            if (_currentTrack.value?.id == track.id) {
                _currentTrack.value = _currentTrack.value?.copy(isFavorite = next)
            }
        }
    }

    fun setSleepTimer(minutes: Int?) {
        sleepTimerJob?.cancel()
        _sleepTimerRemainingMinutes.value = minutes
        if (minutes != null && minutes > 0) {
            sleepTimerJob = viewModelScope.launch {
                var remaining = minutes
                while (remaining > 0) {
                    delay(60000L)
                    remaining--
                    _sleepTimerRemainingMinutes.value = remaining
                }
                mediaController?.pause()
                _sleepTimerRemainingMinutes.value = null
                _statusMessage.value = "Sleep timer paused playback"
            }
        }
    }

    fun minimizePlayer() {
        _isAudioPlayerOpen.value = false
        _isMiniAudioPlayer.value = true
    }

    fun expandPlayer() {
        _isAudioPlayerOpen.value = true
        _isMiniAudioPlayer.value = false
    }

    fun closePlayer() {
        mediaController?.pause()
        _currentTrack.value = null
        _isAudioPlayerOpen.value = false
        _isMiniAudioPlayer.value = false
        stopProgressPolling()
        stopVisualizerAnimation()
    }

    private fun startProgressPolling() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (true) {
                val controller = mediaController
                if (controller != null && controller.isPlaying) {
                    val pos = controller.currentPosition
                    val dur = controller.duration
                    if (pos >= 0) _currentPositionMs.value = pos
                    if (dur > 0) _durationMs.value = dur
                }
                delay(400)
            }
        }
    }

    private fun stopProgressPolling() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun startVisualizerAnimation() {
        visualizerJob?.cancel()
        visualizerJob = viewModelScope.launch {
            while (true) {
                // Generate dynamic organic spectrum wave movement for music visualizer
                _visualizerBands.value = List(24) { i ->
                    val base = 0.25f + 0.65f * Random.nextFloat()
                    // Higher energy on bass and mids (lower indices)
                    val weight = if (i < 8) 1.0f else if (i < 16) 0.8f else 0.65f
                    (base * weight).coerceIn(0.1f, 1.0f)
                }
                delay(100)
            }
        }
    }

    private fun stopVisualizerAnimation() {
        visualizerJob?.cancel()
        visualizerJob = null
        _visualizerBands.value = List(24) { 0.15f }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        stopProgressPolling()
        stopVisualizerAnimation()
        mediaController?.removeListener(playerListener)
        mediaController?.release()
        mediaController = null
    }
}
