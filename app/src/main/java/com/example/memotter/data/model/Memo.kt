package com.example.memotter.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "memos")
data class Memo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val createdAt: Date,
    val updatedAt: Date = createdAt,
    val isFavorite: Boolean = false,
    val isDeleted: Boolean = false,
    val hashtags: String = "", // Comma-separated hashtags
    val filePath: String? = null // For file-based storage option
)