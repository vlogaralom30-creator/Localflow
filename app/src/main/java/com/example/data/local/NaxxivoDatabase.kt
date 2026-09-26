package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        VideoEntity::class,
        AlbumEntity::class,
        VideoAlbumCrossRef::class,
        AudioEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class NaxxivoDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
    abstract fun albumDao(): AlbumDao
    abstract fun audioDao(): AudioDao

    companion object {
        @Volatile
        private var INSTANCE: NaxxivoDatabase? = null

        fun getInstance(context: Context): NaxxivoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NaxxivoDatabase::class.java,
                    "localflow_media.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
