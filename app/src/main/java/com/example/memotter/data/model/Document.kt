package com.example.memotter.data.model

import java.io.File
import java.util.Date

data class Document(
    val file: File?,
    val name: String,
    val content: String = "",
    val isModified: Boolean = false,
    val lastSaved: Date? = null,
    val isNew: Boolean = file == null
) {
    val displayName: String
        get() = if (isNew) "新しいドキュメント" else name
        
    val extension: String
        get() = file?.extension ?: "md"
        
    val sizeInBytes: Long
        get() = file?.length() ?: content.toByteArray().size.toLong()
        
    val formattedSize: String
        get() {
            val kb = sizeInBytes / 1024.0
            return if (kb < 1) "${sizeInBytes}B" else "%.1fKB".format(kb)
        }
}

data class DocumentInfo(
    val file: File,
    val name: String,
    val size: Long,
    val lastModified: Date,
    val preview: String
) {
    val formattedSize: String
        get() {
            val kb = size / 1024.0
            return if (kb < 1) "${size}B" else "%.1fKB".format(kb)
        }
}