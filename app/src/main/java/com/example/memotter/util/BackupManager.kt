package com.example.memotter.util

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class BackupManager(private val context: Context) {

    private val fileManager = FileManager(context)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())

    /**
     * 全メモファイルを指定ディレクトリにエクスポート
     */
    fun exportToDirectory(targetDirectory: File): ExportResult {
        return try {
            // エクスポート用のタイムスタンプ付きフォルダを作成
            val exportFolderName = "Memotter_Export_${dateFormat.format(Date())}"
            val exportDir = File(targetDirectory, exportFolderName)
            
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }

            // 全メモファイルを取得
            val memoFiles = fileManager.getMemoFiles()
            var copiedCount = 0

            // 各ファイルをコピー
            memoFiles.forEach { sourceFile ->
                val targetFile = File(exportDir, sourceFile.name)
                if (copyFile(sourceFile, targetFile)) {
                    copiedCount++
                }
            }

            // テンプレートファイルもコピー
            val templateFiles = fileManager.getTemplateFiles()
            if (templateFiles.isNotEmpty()) {
                val templatesDir = File(exportDir, "Templates")
                templatesDir.mkdirs()
                
                templateFiles.forEach { sourceFile ->
                    val targetFile = File(templatesDir, sourceFile.name)
                    if (copyFile(sourceFile, targetFile)) {
                        copiedCount++
                    }
                }
            }

            // 削除されたメモファイルもコピー
            val deletedFile = fileManager.getDeletedMemosFile()
            if (deletedFile.exists()) {
                val targetFile = File(exportDir, deletedFile.name)
                if (copyFile(deletedFile, targetFile)) {
                    copiedCount++
                }
            }

            ExportResult.Success(exportDir.absolutePath, copiedCount)
        } catch (e: Exception) {
            ExportResult.Error(e.message ?: "エクスポートに失敗しました")
        }
    }

    /**
     * 全メモファイルをZIPファイルとしてエクスポート
     */
    fun exportToZip(targetDirectory: File): ExportResult {
        return try {
            val zipFileName = "Memotter_Backup_${dateFormat.format(Date())}.zip"
            val zipFile = File(targetDirectory, zipFileName)
            
            ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->
                var fileCount = 0

                // メモファイルをZIPに追加
                val memoFiles = fileManager.getMemoFiles()
                memoFiles.forEach { file ->
                    addFileToZip(zipOut, file, file.name)
                    fileCount++
                }

                // テンプレートファイルをZIPに追加
                val templateFiles = fileManager.getTemplateFiles()
                templateFiles.forEach { file ->
                    addFileToZip(zipOut, file, "Templates/${file.name}")
                    fileCount++
                }

                // 削除されたメモファイルをZIPに追加
                val deletedFile = fileManager.getDeletedMemosFile()
                if (deletedFile.exists()) {
                    addFileToZip(zipOut, deletedFile, deletedFile.name)
                    fileCount++
                }

                ExportResult.Success(zipFile.absolutePath, fileCount)
            }
        } catch (e: Exception) {
            ExportResult.Error(e.message ?: "ZIPエクスポートに失敗しました")
        }
    }

    /**
     * ファイルをコピーする
     */
    private fun copyFile(source: File, destination: File): Boolean {
        return try {
            FileInputStream(source).use { input ->
                FileOutputStream(destination).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * ファイルをZIPに追加する
     */
    private fun addFileToZip(zipOut: ZipOutputStream, file: File, entryName: String) {
        FileInputStream(file).use { input ->
            val entry = ZipEntry(entryName)
            zipOut.putNextEntry(entry)
            input.copyTo(zipOut)
            zipOut.closeEntry()
        }
    }

    /**
     * エクスポート可能なファイル数を取得
     */
    fun getExportableFileCount(): Int {
        val memoFiles = fileManager.getMemoFiles()
        val templateFiles = fileManager.getTemplateFiles()
        val deletedFile = fileManager.getDeletedMemosFile()
        
        return memoFiles.size + templateFiles.size + if (deletedFile.exists()) 1 else 0
    }

    /**
     * エクスポート結果を表すsealed class
     */
    sealed class ExportResult {
        data class Success(val exportPath: String, val fileCount: Int) : ExportResult()
        data class Error(val message: String) : ExportResult()
    }
}