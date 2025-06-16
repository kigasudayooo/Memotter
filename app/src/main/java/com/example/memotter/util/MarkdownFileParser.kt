package com.example.memotter.util

import com.example.memotter.data.model.Memo
import java.text.SimpleDateFormat
import java.util.*

/**
 * マークダウンファイルをパースしてメモオブジェクトに変換するクラス
 */
class MarkdownFileParser {
    
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    /**
     * マークダウンファイルの内容をメモリストに変換
     */
    fun parseFileToMemos(content: String, filePath: String): List<Memo> {
        val memos = mutableListOf<Memo>()
        val lines = content.split("\n")
        
        var currentDate: Date? = null
        var currentTime: Date? = null
        var currentContent = StringBuilder()
        var memoId = 1L
        
        for (line in lines) {
            when {
                // 日付ヘッダー（# 2025-06-16）
                line.startsWith("# ") && line.length > 2 -> {
                    // 前のメモを保存
                    if (currentContent.isNotEmpty() && currentTime != null) {
                        val memo = createMemo(memoId++, currentContent.toString().trim(), currentTime, filePath)
                        memos.add(memo)
                        currentContent.clear()
                    }
                    
                    // 新しい日付を解析
                    val dateStr = line.substring(2).trim()
                    currentDate = try {
                        dateFormat.parse(dateStr)
                    } catch (e: Exception) {
                        Date() // パースに失敗した場合は現在日付
                    }
                }
                
                // 時刻ヘッダー（## 14:30）
                line.startsWith("## ") && line.length > 3 -> {
                    // 前のメモを保存
                    if (currentContent.isNotEmpty() && currentTime != null) {
                        val memo = createMemo(memoId++, currentContent.toString().trim(), currentTime, filePath)
                        memos.add(memo)
                        currentContent.clear()
                    }
                    
                    // 新しい時刻を解析
                    val timeStr = line.substring(3).trim()
                    currentTime = try {
                        val parsedTime = timeFormat.parse(timeStr)
                        // 現在の日付と時刻を組み合わせ
                        val calendar = Calendar.getInstance().apply {
                            if (currentDate != null) {
                                this.time = currentDate
                            }
                            val timeCalendar = Calendar.getInstance().apply { time = parsedTime }
                            set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                            set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
                        }
                        calendar.time
                    } catch (e: Exception) {
                        Date() // パースに失敗した場合は現在時刻
                    }
                }
                
                // 空行はスキップ
                line.isBlank() -> {
                    // 内容がある場合は空行も保持
                    if (currentContent.isNotEmpty()) {
                        currentContent.append("\n")
                    }
                }
                
                // メモ内容
                else -> {
                    if (currentContent.isNotEmpty()) {
                        currentContent.append("\n")
                    }
                    currentContent.append(line)
                }
            }
        }
        
        // 最後のメモを保存
        if (currentContent.isNotEmpty() && currentTime != null) {
            val memo = createMemo(memoId, currentContent.toString().trim(), currentTime, filePath)
            memos.add(memo)
        }
        
        return memos.reversed() // 新しい順にソート
    }
    
    /**
     * メモリストからマークダウンファイル内容を生成
     */
    fun memosToFileContent(memos: List<Memo>): String {
        if (memos.isEmpty()) return ""
        
        val content = StringBuilder()
        var currentDate: String? = null
        
        // 古い順にソート
        val sortedMemos = memos.sortedBy { it.createdAt }
        
        for (memo in sortedMemos) {
            val memoDate = dateFormat.format(memo.createdAt)
            val memoTime = timeFormat.format(memo.createdAt)
            
            // 日付ヘッダーを追加（日付が変わった場合）
            if (currentDate != memoDate) {
                if (content.isNotEmpty()) {
                    content.append("\n\n")
                }
                content.append("# $memoDate\n\n")
                currentDate = memoDate
            }
            
            // 時刻ヘッダーとメモ内容を追加
            content.append("## $memoTime\n\n")
            content.append(memo.content)
            content.append("\n\n")
        }
        
        return content.toString().trim()
    }
    
    private fun createMemo(id: Long, content: String, createdAt: Date, filePath: String): Memo {
        val hashtags = HashtagExtractor.extractHashtags(content)
        return Memo(
            id = id,
            content = content,
            createdAt = createdAt,
            updatedAt = createdAt,
            hashtags = hashtags.joinToString(","),
            filePath = filePath,
            isFavorite = false,
            isDeleted = false
        )
    }
}