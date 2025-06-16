package com.example.memotter.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.memotter.data.model.Memo

@Dao
interface MemoDao {
    @Query("SELECT * FROM memos WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllMemos(): LiveData<List<Memo>>

    @Query("SELECT * FROM memos WHERE isDeleted = 0 AND isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteMemos(): LiveData<List<Memo>>

    @Query("SELECT * FROM memos WHERE isDeleted = 1 ORDER BY updatedAt DESC")
    fun getDeletedMemos(): LiveData<List<Memo>>

    @Query("SELECT * FROM memos WHERE id = :id")
    suspend fun getMemoById(id: Long): Memo?

    @Query("SELECT * FROM memos WHERE isDeleted = 0 AND (content LIKE '%' || :query || '%' OR hashtags LIKE '%' || :query || '%') ORDER BY createdAt DESC")
    fun searchMemos(query: String): LiveData<List<Memo>>

    @Query("SELECT * FROM memos WHERE isDeleted = 0 AND hashtags LIKE '%' || :hashtag || '%' ORDER BY createdAt DESC")
    fun getMemosByHashtag(hashtag: String): LiveData<List<Memo>>

    @Insert
    suspend fun insert(memo: Memo): Long

    @Update
    suspend fun update(memo: Memo)

    @Delete
    suspend fun delete(memo: Memo)

    @Query("UPDATE memos SET isDeleted = 1, updatedAt = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: Long, deletedAt: java.util.Date)

    @Query("UPDATE memos SET isDeleted = 0, updatedAt = :restoredAt WHERE id = :id")
    suspend fun restore(id: Long, restoredAt: java.util.Date)

    @Query("DELETE FROM memos WHERE isDeleted = 1 AND updatedAt < :cutoffDate")
    suspend fun permanentlyDeleteOldMemos(cutoffDate: java.util.Date)

    @Query("UPDATE memos SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean, updatedAt: java.util.Date)
}