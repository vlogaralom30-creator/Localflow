package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for local video metadata operations in LocalFlow.
 */
@Dao
interface VideoDao {

    @Query("SELECT * FROM videos ORDER BY dateAdded DESC")
    fun getAllVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isShort = 1 ORDER BY dateAdded DESC")
    fun getShortVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isShort = 0 ORDER BY dateAdded DESC")
    fun getLongVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE lastPositionMs > 0 AND (durationMs == 0 OR lastPositionMs < (durationMs * 95 / 100)) ORDER BY dateAdded DESC")
    fun getContinueWatchingVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isWatchLater = 1 ORDER BY dateAdded DESC")
    fun getWatchLaterVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isFavorite = 1 ORDER BY dateAdded DESC")
    fun getFavoriteVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE lastPositionMs > 0 OR watchCount > 0 ORDER BY dateAdded DESC")
    fun getRecentlyWatchedVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE id = :id LIMIT 1")
    fun getVideoById(id: Long): Flow<VideoEntity?>

    @Query("SELECT * FROM videos WHERE uri = :uri LIMIT 1")
    suspend fun getVideoByUri(uri: String): VideoEntity?

    @Query("SELECT * FROM videos WHERE title LIKE '%' || :query || '%' OR folderName LIKE '%' || :query || '%' ORDER BY title COLLATE NOCASE ASC")
    fun searchVideos(query: String): Flow<List<VideoEntity>>

    @Query("SELECT DISTINCT folderName FROM videos ORDER BY folderName ASC")
    fun getAllFolders(): Flow<List<String>>

    @Query("SELECT * FROM videos WHERE folderName = :folderName ORDER BY dateAdded DESC")
    fun getVideosByFolder(folderName: String): Flow<List<VideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(videos: List<VideoEntity>)

    @Update
    suspend fun updateVideo(video: VideoEntity)

    @Query("UPDATE videos SET lastPositionMs = :position WHERE id = :id")
    suspend fun updatePlaybackPosition(id: Long, position: Long)

    @Query("UPDATE videos SET watchCount = watchCount + 1 WHERE id = :id")
    suspend fun incrementWatchCount(id: Long)

    @Query("UPDATE videos SET isWatchLater = :isWatchLater WHERE id = :id")
    suspend fun updateWatchLater(id: Long, isWatchLater: Boolean)

    @Query("UPDATE videos SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE videos SET title = :title WHERE id = :id")
    suspend fun updateVideoTitle(id: Long, title: String)

    @Query("DELETE FROM videos WHERE uri LIKE 'http://%' OR uri LIKE 'https://%' OR filePath LIKE '/storage/emulated/0/Movies/The_Last_Horizon%'")
    suspend fun deleteDemoVideos()

    @Delete
    suspend fun deleteVideo(video: VideoEntity)

    @Query("DELETE FROM videos WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM videos")
    suspend fun clearAll()
}
