package com.example.memotter.data.repository

import androidx.lifecycle.LiveData
import com.example.memotter.data.database.HashtagDao
import com.example.memotter.data.database.MemoDao
import com.example.memotter.data.model.Hashtag
import com.example.memotter.data.model.Memo
import com.example.memotter.util.HashtagExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

class MemoRepository(
    private val memoDao: MemoDao,
    private val hashtagDao: HashtagDao
) {
    fun getAllMemos(): LiveData<List<Memo>> = memoDao.getAllMemos()

    fun getFavoriteMemos(): LiveData<List<Memo>> = memoDao.getFavoriteMemos()

    fun getDeletedMemos(): LiveData<List<Memo>> = memoDao.getDeletedMemos()

    fun searchMemos(query: String): LiveData<List<Memo>> = memoDao.searchMemos(query)

    fun getMemosByHashtag(hashtag: String): LiveData<List<Memo>> = memoDao.getMemosByHashtag(hashtag)

    fun getAllHashtags(): LiveData<List<Hashtag>> = hashtagDao.getAllHashtags()

    fun searchHashtags(query: String): LiveData<List<Hashtag>> = hashtagDao.searchHashtags(query)

    suspend fun getMemoById(id: Long): Memo? = withContext(Dispatchers.IO) {
        memoDao.getMemoById(id)
    }

    suspend fun insertMemo(content: String, filePath: String? = null): Long = withContext(Dispatchers.IO) {
        val now = Date()
        val hashtags = HashtagExtractor.extractHashtags(content)
        val hashtagsString = hashtags.joinToString(",")

        val memo = Memo(
            content = content,
            createdAt = now,
            updatedAt = now,
            hashtags = hashtagsString,
            filePath = filePath
        )

        val memoId = memoDao.insert(memo)

        // Update hashtag usage
        hashtags.forEach { tag ->
            val existingHashtag = hashtagDao.getHashtag(tag)
            if (existingHashtag != null) {
                hashtagDao.incrementUsage(tag, now)
            } else {
                hashtagDao.insert(Hashtag(tag = tag, usageCount = 1, lastUsed = now))
            }
        }

        memoId
    }

    suspend fun updateMemo(memo: Memo): Unit = withContext(Dispatchers.IO) {
        val updatedMemo = memo.copy(updatedAt = Date())
        val hashtags = HashtagExtractor.extractHashtags(updatedMemo.content)
        val hashtagsString = hashtags.joinToString(",")
        val finalMemo = updatedMemo.copy(hashtags = hashtagsString)

        memoDao.update(finalMemo)

        // Update hashtag usage
        val now = Date()
        hashtags.forEach { tag ->
            val existingHashtag = hashtagDao.getHashtag(tag)
            if (existingHashtag != null) {
                hashtagDao.incrementUsage(tag, now)
            } else {
                hashtagDao.insert(Hashtag(tag = tag, usageCount = 1, lastUsed = now))
            }
        }
    }

    suspend fun softDeleteMemo(id: Long): Unit = withContext(Dispatchers.IO) {
        memoDao.softDelete(id, Date())
    }

    suspend fun restoreMemo(id: Long): Unit = withContext(Dispatchers.IO) {
        memoDao.restore(id, Date())
    }

    suspend fun permanentlyDeleteMemo(memo: Memo): Unit = withContext(Dispatchers.IO) {
        memoDao.delete(memo)
    }

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean): Unit = withContext(Dispatchers.IO) {
        memoDao.updateFavoriteStatus(id, isFavorite, Date())
    }

    suspend fun cleanupOldDeletedMemos(): Unit = withContext(Dispatchers.IO) {
        val cutoffDate = Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000) // 7 days ago
        memoDao.permanentlyDeleteOldMemos(cutoffDate)
    }
}