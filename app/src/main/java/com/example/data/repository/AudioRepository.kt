package com.example.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.example.data.local.AudioDao
import com.example.data.local.AudioEntity
import com.example.data.local.VideoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AudioRepository(
    private val context: Context,
    private val audioDao: AudioDao
) {
    val allAudioTracks: Flow<List<AudioEntity>> = audioDao.getAllAudioTracks()
    val favoriteAudioTracks: Flow<List<AudioEntity>> = audioDao.getFavoriteAudioTracks()
    val recentlyPlayedAudioTracks: Flow<List<AudioEntity>> = audioDao.getRecentlyPlayedAudioTracks()

    fun searchAudioTracks(query: String): Flow<List<AudioEntity>> = audioDao.searchAudioTracks(query)
    fun getAudioTrackById(id: Long): Flow<AudioEntity?> = audioDao.getAudioTrackById(id)

    suspend fun toggleFavorite(id: Long, isFav: Boolean) = audioDao.toggleFavorite(id, isFav)
    suspend fun updatePosition(id: Long, pos: Long) = audioDao.updatePosition(id, pos)
    suspend fun incrementPlayCount(id: Long) = audioDao.incrementPlayCount(id)

    /**
     * Scans MediaStore for audio music files on device.
     */
    suspend fun scanMediaStoreAudio(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.ALBUM_ID
            )

            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 10000"
            val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"
            val queryUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

            val tracks = mutableListOf<AudioEntity>()
            contentResolver.query(queryUri, projection, selection, null, sortOrder)?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val albumIdCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)

                val artBaseUri = Uri.parse("content://media/external/audio/albumart")

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    val title = cursor.getString(titleCol) ?: "Audio Track $id"
                    val artist = if (artistCol != -1) cursor.getString(artistCol) ?: "Unknown Artist" else "Unknown Artist"
                    val album = if (albumCol != -1) cursor.getString(albumCol) ?: "Unknown Album" else "Unknown Album"
                    val duration = cursor.getLong(durationCol)
                    val size = cursor.getLong(sizeCol)
                    val mime = cursor.getString(mimeCol) ?: "audio/mpeg"
                    val dateAdded = cursor.getLong(dateCol) * 1000

                    val albumId = if (albumIdCol != -1) cursor.getLong(albumIdCol) else -1L
                    val artUri = if (albumId > 0) ContentUris.withAppendedId(artBaseUri, albumId).toString() else null

                    val existing = audioDao.getAudioTrackByUri(contentUri.toString())

                    val entity = AudioEntity(
                        id = existing?.id ?: 0,
                        uri = contentUri.toString(),
                        title = title,
                        artist = if (artist == "<unknown>") "Unknown Artist" else artist,
                        album = if (album == "<unknown>") "Unknown Album" else album,
                        durationMs = duration,
                        sizeBytes = size,
                        albumArtUri = artUri,
                        mimeType = mime,
                        dateAdded = dateAdded,
                        isFavorite = existing?.isFavorite ?: false,
                        lastPositionMs = existing?.lastPositionMs ?: 0L,
                        playCount = existing?.playCount ?: 0
                    )
                    tracks.add(entity)
                }
            }

            if (tracks.isNotEmpty()) {
                audioDao.insertAll(tracks)
            }
            Result.success(tracks.size)
        } catch (e: Exception) {
            Log.e("AudioRepository", "Error scanning MediaStore Audio", e)
            Result.failure(e)
        }
    }

    /**
     * Bridges a video to audio so it can be played in Audio Vibe mode.
     */
    suspend fun importVideoAsAudio(video: VideoEntity): AudioEntity = withContext(Dispatchers.IO) {
        val existing = audioDao.getAudioTrackByUri(video.uri)
        if (existing != null) {
            return@withContext existing
        }

        val newTrack = AudioEntity(
            uri = video.uri,
            title = video.title,
            artist = "LocalFlow Video Audio",
            album = video.folderName,
            durationMs = video.durationMs,
            sizeBytes = video.sizeBytes,
            albumArtUri = video.thumbnailUri ?: video.uri,
            mimeType = "audio/mp4",
            dateAdded = System.currentTimeMillis()
        )
        val id = audioDao.insertTrack(newTrack)
        newTrack.copy(id = id)
    }
}
