package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class AlbumWithCount(
    val id: Long,
    val name: String,
    val coverUri: String?,
    val createdAt: Long,
    val videoCount: Int
)

@Dao
interface AlbumDao {
    @Query("""
        SELECT a.id, a.name, a.coverUri, a.createdAt, COUNT(x.videoId) as videoCount 
        FROM albums a 
        LEFT JOIN video_album_cross_ref x ON a.id = x.albumId 
        GROUP BY a.id 
        ORDER BY a.name ASC
    """)
    fun getAllAlbumsWithCount(): Flow<List<AlbumWithCount>>

    @Query("SELECT * FROM albums ORDER BY name ASC")
    fun getAllAlbums(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE id = :id LIMIT 1")
    fun getAlbumById(id: Long): Flow<AlbumEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: AlbumEntity): Long

    @Update
    suspend fun updateAlbum(album: AlbumEntity)

    @Delete
    suspend fun deleteAlbum(album: AlbumEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addVideoToAlbum(crossRef: VideoAlbumCrossRef)

    @Query("DELETE FROM video_album_cross_ref WHERE albumId = :albumId AND videoId = :videoId")
    suspend fun removeVideoFromAlbum(albumId: Long, videoId: Long)

    @Query("""
        SELECT v.* FROM videos v
        INNER JOIN video_album_cross_ref x ON v.id = x.videoId
        WHERE x.albumId = :albumId
        ORDER BY x.addedAt DESC
    """)
    fun getVideosForAlbum(albumId: Long): Flow<List<VideoEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM video_album_cross_ref WHERE albumId = :albumId AND videoId = :videoId)")
    suspend fun isVideoInAlbum(albumId: Long, videoId: Long): Boolean

    @Query("DELETE FROM albums WHERE coverUri LIKE 'http://%' OR coverUri LIKE 'https://%'")
    suspend fun deleteDemoAlbums()

    @Query("DELETE FROM video_album_cross_ref WHERE videoId NOT IN (SELECT id FROM videos)")
    suspend fun cleanupOrphanCrossRefs()
}
