package com.example.data.repository

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import com.example.data.local.AlbumDao
import com.example.data.local.AlbumEntity
import com.example.data.local.AlbumWithCount
import com.example.data.local.VideoAlbumCrossRef
import com.example.data.local.VideoDao
import com.example.data.local.VideoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class VideoRepository(
    private val context: Context,
    private val videoDao: VideoDao,
    private val albumDao: AlbumDao
) {
    val allVideos: Flow<List<VideoEntity>> = videoDao.getAllVideos()
    val shortVideos: Flow<List<VideoEntity>> = videoDao.getShortVideos()
    val longVideos: Flow<List<VideoEntity>> = videoDao.getLongVideos()
    val continueWatching: Flow<List<VideoEntity>> = videoDao.getContinueWatchingVideos()
    val watchLater: Flow<List<VideoEntity>> = videoDao.getWatchLaterVideos()
    val favorites: Flow<List<VideoEntity>> = videoDao.getFavoriteVideos()
    val recentlyWatched: Flow<List<VideoEntity>> = videoDao.getRecentlyWatchedVideos()
    val folders: Flow<List<String>> = videoDao.getAllFolders()
    val albumsWithCount: Flow<List<AlbumWithCount>> = albumDao.getAllAlbumsWithCount()
    val allAlbums: Flow<List<AlbumEntity>> = albumDao.getAllAlbums()

    fun searchVideos(query: String): Flow<List<VideoEntity>> = videoDao.searchVideos(query)
    fun getVideosByFolder(folder: String): Flow<List<VideoEntity>> = videoDao.getVideosByFolder(folder)
    fun getVideoById(id: Long): Flow<VideoEntity?> = videoDao.getVideoById(id)
    fun getVideosForAlbum(albumId: Long): Flow<List<VideoEntity>> = albumDao.getVideosForAlbum(albumId)

    /**
     * Scans MediaStore for local videos on device.
     */
    suspend fun scanMediaStore(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            purgeAllDemoData()
            val contentResolver = context.contentResolver
            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.WIDTH,
                MediaStore.Video.Media.HEIGHT
            )

            val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"
            val queryUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

            val videos = mutableListOf<VideoEntity>()
            contentResolver.query(queryUri, projection, null, null, sortOrder)?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
                val dataCol = cursor.getColumnIndex(MediaStore.Video.Media.DATA)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val bucketCol = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                val widthCol = cursor.getColumnIndex(MediaStore.Video.Media.WIDTH)
                val heightCol = cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                    val name = cursor.getString(nameCol) ?: "Video_$id"
                    val title = cursor.getString(titleCol) ?: name
                    val path = if (dataCol != -1) cursor.getString(dataCol) ?: contentUri.toString() else contentUri.toString()
                    val duration = cursor.getLong(durationCol)
                    val size = cursor.getLong(sizeCol)
                    val mime = cursor.getString(mimeCol) ?: "video/mp4"
                    val dateAdded = cursor.getLong(dateCol) * 1000
                    val folder = if (bucketCol != -1) cursor.getString(bucketCol) ?: "Camera" else "Camera"
                    val width = if (widthCol != -1) cursor.getInt(widthCol) else 0
                    val height = if (heightCol != -1) cursor.getInt(heightCol) else 0

                    val isShort = if (width > 0 && height > 0) {
                        height > width
                    } else {
                        duration in 1..65000L
                    }

                    val resolution = if (width > 0 && height > 0) "${width}x${height}" else if (isShort) "1080x1920" else "1920x1080"

                    val existing = videoDao.getVideoByUri(contentUri.toString())
                    val videoEntity = VideoEntity(
                        id = existing?.id ?: 0,
                        uri = contentUri.toString(),
                        filePath = path,
                        title = cleanTitle(title),
                        durationMs = duration,
                        sizeBytes = size,
                        width = width,
                        height = height,
                        isShort = isShort,
                        resolution = resolution,
                        fps = "30.00",
                        mimeType = mime,
                        thumbnailUri = contentUri.toString(),
                        dateAdded = if (dateAdded > 0) dateAdded else System.currentTimeMillis(),
                        folderName = folder,
                        lastPositionMs = existing?.lastPositionMs ?: 0L,
                        watchCount = existing?.watchCount ?: 0,
                        isWatchLater = existing?.isWatchLater ?: false,
                        isFavorite = existing?.isFavorite ?: false
                    )
                    videos.add(videoEntity)
                }
            }

            if (videos.isNotEmpty()) {
                videoDao.insertAll(videos)
            }
            Result.success(videos.size)
        } catch (e: Exception) {
            Log.e("VideoRepository", "Error scanning MediaStore", e)
            Result.failure(e)
        }
    }

    /**
     * Imports an individual video file selected by user via SAF (OpenDocument).
     */
    suspend fun importVideoFromUri(uri: Uri): VideoEntity? = withContext(Dispatchers.IO) {
        try {
            var fileName = "Imported Video"
            var fileSize = 0L

            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIndex != -1) fileName = cursor.getString(nameIndex) ?: fileName
                    if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
                }
            }

            var duration = 0L
            var width = 0
            var height = 0
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, uri)
                duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
                height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
                retriever.release()
            } catch (e: Exception) {
                Log.w("VideoRepository", "Could not extract metadata via retriever", e)
            }

            val isShort = height > width || (duration in 1..65000L && width <= height)
            val resolution = if (width > 0 && height > 0) "${width}x${height}" else if (isShort) "1080x1920" else "1920x1080"
            val mimeType = context.contentResolver.getType(uri) ?: "video/mp4"

            val entity = VideoEntity(
                uri = uri.toString(),
                filePath = uri.path ?: uri.toString(),
                title = cleanTitle(fileName),
                durationMs = duration,
                sizeBytes = fileSize,
                width = width,
                height = height,
                isShort = isShort,
                resolution = resolution,
                fps = "30.00",
                mimeType = mimeType,
                thumbnailUri = uri.toString(),
                dateAdded = System.currentTimeMillis(),
                folderName = "Imports"
            )

            val insertedId = videoDao.insertVideo(entity)
            entity.copy(id = insertedId)
        } catch (e: Exception) {
            Log.e("VideoRepository", "Failed to import video from URI: $uri", e)
            null
        }
    }

    /**
     * Purges any mock or demo video entries from the database, ensuring only real local videos exist.
     */
    suspend fun purgeAllDemoData() = withContext(Dispatchers.IO) {
        try {
            videoDao.deleteDemoVideos()
            albumDao.deleteDemoAlbums()
            albumDao.cleanupOrphanCrossRefs()
        } catch (e: Exception) {
            Log.e("VideoRepository", "Error purging demo data", e)
        }
    }

    suspend fun updatePlaybackPosition(id: Long, positionMs: Long) = withContext(Dispatchers.IO) {
        videoDao.updatePlaybackPosition(id, positionMs)
    }

    suspend fun incrementWatchCount(id: Long) = withContext(Dispatchers.IO) {
        videoDao.incrementWatchCount(id)
    }

    suspend fun toggleWatchLater(id: Long, isWatchLater: Boolean) = withContext(Dispatchers.IO) {
        videoDao.updateWatchLater(id, isWatchLater)
    }

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        videoDao.updateFavorite(id, isFavorite)
    }

    suspend fun renameVideo(id: Long, title: String) = withContext(Dispatchers.IO) {
        videoDao.updateVideoTitle(id, title)
    }

    suspend fun deleteVideo(video: VideoEntity) = withContext(Dispatchers.IO) {
        videoDao.deleteVideo(video)
    }

    suspend fun createAlbum(name: String, coverUri: String? = null): Long = withContext(Dispatchers.IO) {
        albumDao.insertAlbum(AlbumEntity(name = name, coverUri = coverUri))
    }

    suspend fun addVideoToAlbum(albumId: Long, videoId: Long) = withContext(Dispatchers.IO) {
        albumDao.addVideoToAlbum(VideoAlbumCrossRef(albumId, videoId))
    }

    suspend fun removeVideoFromAlbum(albumId: Long, videoId: Long) = withContext(Dispatchers.IO) {
        albumDao.removeVideoFromAlbum(albumId, videoId)
    }

    suspend fun deleteAlbum(album: AlbumEntity) = withContext(Dispatchers.IO) {
        albumDao.deleteAlbum(album)
    }

    private fun cleanTitle(raw: String): String {
        return raw.replace(Regex("\\.(mp4|mkv|avi|mov|flv|webm|3gp)$", RegexOption.IGNORE_CASE), "")
            .replace('_', ' ')
    }
}
