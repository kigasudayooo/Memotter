package com.example.memotter.util

import android.content.Context
import com.example.memotter.data.model.Document
import com.example.memotter.data.model.DocumentInfo
import java.io.File
import java.util.Date

class DocumentManager(private val context: Context) {
    
    val fileManager = FileManager(context)
    private var currentDocument: Document? = null
    
    // Current document management
    fun getCurrentDocument(): Document? = currentDocument
    
    fun createNewDocument(): Document {
        val newDoc = Document(
            file = null,
            name = "新しいドキュメント",
            content = "",
            isModified = false,
            isNew = true
        )
        currentDocument = newDoc
        return newDoc
    }
    
    fun openDocument(file: File): Document? {
        val content = fileManager.readMarkdownFile(file)
        return if (content != null) {
            val doc = Document(
                file = file,
                name = file.nameWithoutExtension,
                content = content,
                isModified = false,
                lastSaved = Date(file.lastModified()),
                isNew = false
            )
            currentDocument = doc
            doc
        } else {
            null
        }
    }
    
    fun updateDocumentContent(content: String): Document? {
        return currentDocument?.let { doc ->
            val updatedDoc = doc.copy(
                content = content,
                isModified = doc.content != content
            )
            currentDocument = updatedDoc
            updatedDoc
        }
    }
    
    fun saveDocument(document: Document): Boolean {
        val file = document.file ?: return false
        return if (fileManager.writeMarkdownFile(file, document.content)) {
            currentDocument = document.copy(
                isModified = false,
                lastSaved = Date()
            )
            true
        } else {
            false
        }
    }
    
    fun saveDocumentAs(document: Document, fileName: String, directory: File? = null): File? {
        val saveDir = directory ?: fileManager.getMemotterDirectory()
        val extension = if (fileName.endsWith(".md") || fileName.endsWith(".txt")) "" else ".md"
        val file = File(saveDir, fileName + extension)
        
        return if (fileManager.writeMarkdownFile(file, document.content)) {
            currentDocument = document.copy(
                file = file,
                name = file.nameWithoutExtension,
                isModified = false,
                lastSaved = Date(),
                isNew = false
            )
            file
        } else {
            null
        }
    }
    
    // Document listing
    fun getAllDocuments(): List<DocumentInfo> {
        return fileManager.getMemoFiles().mapNotNull { file ->
            try {
                val content = fileManager.readMarkdownFile(file) ?: ""
                val preview = content.lines()
                    .take(3)
                    .joinToString(" ")
                    .take(100)
                    .trim()
                
                DocumentInfo(
                    file = file,
                    name = file.nameWithoutExtension,
                    size = file.length(),
                    lastModified = Date(file.lastModified()),
                    preview = preview.ifEmpty { "空のドキュメント" }
                )
            } catch (e: Exception) {
                null
            }
        }.sortedByDescending { it.lastModified }
    }
    
    fun getTodayDocuments(): List<DocumentInfo> {
        val today = Date()
        val todayStart = Date(today.year, today.month, today.date)
        val todayEnd = Date(todayStart.time + 24 * 60 * 60 * 1000)
        
        return getAllDocuments().filter { doc ->
            doc.lastModified >= todayStart && doc.lastModified < todayEnd
        }
    }
    
    fun getRecentDocuments(days: Int = 7): List<DocumentInfo> {
        val cutoffDate = Date(System.currentTimeMillis() - days * 24 * 60 * 60 * 1000L)
        return getAllDocuments().filter { doc ->
            doc.lastModified >= cutoffDate
        }
    }
    
    fun searchDocuments(query: String): List<DocumentInfo> {
        val lowerQuery = query.lowercase()
        return getAllDocuments().filter { doc ->
            doc.name.lowercase().contains(lowerQuery) ||
            doc.preview.lowercase().contains(lowerQuery)
        }
    }
    
    // Utility methods
    fun hasUnsavedChanges(): Boolean {
        return currentDocument?.isModified == true
    }
    
    fun getDocumentTitle(): String {
        return currentDocument?.displayName ?: "Memotter"
    }
    
    fun isValidFileName(fileName: String): Boolean {
        if (fileName.isBlank()) return false
        
        // Check for invalid characters
        val invalidChars = listOf('/', '\\', ':', '*', '?', '"', '<', '>', '|')
        if (invalidChars.any { fileName.contains(it) }) return false
        
        // Check if file already exists
        val dir = fileManager.getMemotterDirectory()
        val file = File(dir, fileName + ".md")
        return !file.exists()
    }
    
    fun getDuplicateFileName(baseName: String): String {
        val dir = fileManager.getMemotterDirectory()
        var counter = 1
        var fileName: String
        
        do {
            fileName = if (counter == 1) baseName else "$baseName ($counter)"
            val file = File(dir, "$fileName.md")
            if (!file.exists()) break
            counter++
        } while (counter < 100)
        
        return fileName
    }
}