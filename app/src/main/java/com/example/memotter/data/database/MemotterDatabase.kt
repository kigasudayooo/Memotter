package com.example.memotter.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.memotter.data.model.Hashtag
import com.example.memotter.data.model.Memo

@Database(
    entities = [Memo::class, Hashtag::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MemotterDatabase : RoomDatabase() {
    abstract fun memoDao(): MemoDao
    abstract fun hashtagDao(): HashtagDao

    companion object {
        @Volatile
        private var INSTANCE: MemotterDatabase? = null

        fun getDatabase(context: Context): MemotterDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MemotterDatabase::class.java,
                    "memotter_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}