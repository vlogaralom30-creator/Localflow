package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioDao {
    @Query("SELECT * FROM audio_tracks ORDER BY dateAdded DESC")
    fun getAllAudioTracks(): Flow<List<AudioEntity>>

    @Query("SELECT * FROM audio_tracks WHERE isFavorite = 1 ORDER BY dateAdded DESC")
    fun getFavoriteAudioTracks(): Flow<List<AudioEntity>>

    @Query("SELECT * FROM audio_tracks WHERE playCount > 0 ORDER BY lastPositionMs DESC LIMIT 20")
    fun getRecentlyPlayedAudioTracks(): Flow<List<AudioEntity>>

    @Query("SELECT * FROM audio_tracks WHERE id = :id LIMIT 1")
    fun getAudioTrackById(id: Long): Flow<AudioEntity?>

    @Query("SELECT * FROM audio_tracks WHERE uri = :uri LIMIT 1")
    suspend fun getAudioTrackByUri(uri: String): AudioEntity?

    @Query("SELECT * FROM audio_tracks WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR album LIKE '%' || :query || '%' ORDER BY title ASC")
    fun searchAudioTracks(query: String): Flow<List<AudioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tracks: List<AudioEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: AudioEntity): Long

    @Update
    suspend fun updateTrack(track: AudioEntity)

    @Delete
    suspend fun deleteTrack(track: AudioEntity)

    @Query("UPDATE audio_tracks SET isFavorite = :isFav WHERE id = :id")
    suspend fun toggleFavorite(id: Long, isFav: Boolean)

    @Query("UPDATE audio_tracks SET lastPositionMs = :positionMs WHERE id = :id")
    suspend fun updatePosition(id: Long, positionMs: Long)

    @Query("UPDATE audio_tracks SET playCount = playCount + 1 WHERE id = :id")
    suspend fun incrementPlayCount(id: Long)

    @Query("DELETE FROM audio_tracks")
    suspend fun deleteAll()
}
