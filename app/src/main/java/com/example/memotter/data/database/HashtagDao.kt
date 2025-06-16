package com.example.memotter.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.memotter.data.model.Hashtag

@Dao
interface HashtagDao {
    @Query("SELECT * FROM hashtags ORDER BY usageCount DESC, lastUsed DESC")
    fun getAllHashtags(): LiveData<List<Hashtag>>

    @Query("SELECT * FROM hashtags WHERE tag LIKE '%' || :query || '%' ORDER BY usageCount DESC, lastUsed DESC")
    fun searchHashtags(query: String): LiveData<List<Hashtag>>

    @Query("SELECT * FROM hashtags WHERE tag = :tag")
    suspend fun getHashtag(tag: String): Hashtag?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(hashtag: Hashtag)

    @Query("UPDATE hashtags SET usageCount = usageCount + 1, lastUsed = :lastUsed WHERE tag = :tag")
    suspend fun incrementUsage(tag: String, lastUsed: java.util.Date)

    @Delete
    suspend fun delete(hashtag: Hashtag)
}