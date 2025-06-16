package com.example.memotter.util

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class FileManager(private val context: Context) {

    companion object {
        private const val MEMO_DIRECTORY = "Memotter"
        private const val TEMPLATE_DIRECTORY = "Templates"
        private const val BACKUP_DIRECTORY = "Backup"
        private const val DELETED_MEMOS_FILE = "削除した投稿.md"
        
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }

    /**
     * Get the main Memotter directory
     */
    fun getMemotterDirectory(): File {
        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val memotterDir = File(documentsDir, MEMO_DIRECTORY)
        if (!memotterDir.exists()) {
            memotterDir.mkdirs()
        }
        return memotterDir
    }

    /**
     * Get the templates directory
     */
    fun getTemplatesDirectory(): File {
        val memotterDir = getMemotterDirectory()
        val templatesDir = File(memotterDir, TEMPLATE_DIRECTORY)
        if (!templatesDir.exists()) {
            templatesDir.mkdirs()
        }
        return templatesDir
    }

    /**
     * Get the backup directory
     */
    fun getBackupDirectory(): File {
        val memotterDir = getMemotterDirectory()
        val backupDir = File(memotterDir, BACKUP_DIRECTORY)
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }
        return backupDir
    }

    /**
     * Get all markdown files in templates directory
     */
    fun getTemplateFiles(): List<File> {
        val templatesDir = getTemplatesDirectory()
        return templatesDir.listFiles { file ->
            file.isFile && file.extension.equals("md", ignoreCase = true)
        }?.toList() ?: emptyList()
    }

    /**
     * Get all markdown files in main directory (excluding templates and backup)
     */
    fun getMemoFiles(): List<File> {
        val memotterDir = getMemotterDirectory()
        return memotterDir.listFiles { file ->
            file.isFile && 
            file.extension.equals("md", ignoreCase = true) &&
            !file.name.startsWith("template", ignoreCase = true) &&
            !file.absolutePath.contains(TEMPLATE_DIRECTORY) &&
            !file.absolutePath.contains(BACKUP_DIRECTORY)
        }?.sortedByDescending { it.lastModified() }?.toList() ?: emptyList()
    }

    /**
     * Read content from a markdown file
     */
    fun readMarkdownFile(file: File): String? {
        return try {
            if (file.exists() && file.canRead()) {
                file.readText(Charsets.UTF_8)
            } else {
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Write content to a markdown file
     */
    fun writeMarkdownFile(file: File, content: String): Boolean {
        return try {
            file.writeText(content, Charsets.UTF_8)
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Get daily file for today
     */
    fun getTodayFile(): File {
        val memotterDir = getMemotterDirectory()
        val today = dateFormat.format(Date())
        return File(memotterDir, "$today.md")
    }

    /**
     * Get the last used file for continuous mode
     */
    fun getLastUsedFile(): File? {
        val memoFiles = getMemoFiles()
        return memoFiles.firstOrNull() // Most recently modified file
    }

    /**
     * Create daily markdown file if it doesn't exist
     */
    fun createDailyFileIfNeeded(): File {
        val todayFile = getTodayFile()
        if (!todayFile.exists()) {
            val content = "# ${dateFormat.format(Date())}\n\n"
            writeMarkdownFile(todayFile, content)
        }
        return todayFile
    }

    /**
     * Append memo to file
     */
    fun appendMemoToFile(file: File, content: String): Boolean {
        return try {
            val timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            val memoEntry = "\n## $timestamp\n\n$content\n"
            
            if (file.exists()) {
                file.appendText(memoEntry, Charsets.UTF_8)
            } else {
                val header = "# ${dateFormat.format(Date())}\n"
                writeMarkdownFile(file, header + memoEntry)
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Get deleted memos file
     */
    fun getDeletedMemosFile(): File {
        val memotterDir = getMemotterDirectory()
        return File(memotterDir, DELETED_MEMOS_FILE)
    }

    /**
     * Move memo to deleted memos file
     */
    fun moveToDeletedMemos(content: String): Boolean {
        val deletedFile = getDeletedMemosFile()
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val deletedEntry = "\n## 削除日時: $timestamp\n\n$content\n\n---\n"
        
        return try {
            if (deletedFile.exists()) {
                deletedFile.appendText(deletedEntry, Charsets.UTF_8)
            } else {
                val header = "# 削除したメモ\n\n削除から7日後に自動削除されます。\n"
                writeMarkdownFile(deletedFile, header + deletedEntry)
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Check if external storage is available for writing
     */
    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    /**
     * Check if external storage is available for reading
     */
    fun isExternalStorageReadable(): Boolean {
        val state = Environment.getExternalStorageState()
        return state == Environment.MEDIA_MOUNTED || state == Environment.MEDIA_MOUNTED_READ_ONLY
    }
}