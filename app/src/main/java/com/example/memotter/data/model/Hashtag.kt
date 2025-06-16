package com.example.memotter.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "hashtags")
data class Hashtag(
    @PrimaryKey
    val tag: String,
    val usageCount: Int = 1,
    val lastUsed: Date = Date()
)