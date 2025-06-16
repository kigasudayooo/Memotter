package com.example.memotter.util

import android.content.Context
import android.content.SharedPreferences
import java.io.File

/**
 * 現在開いているファイルの状態を管理するクラス
 */
class CurrentFileManager(context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * 現在開いているファイルのパス
     */
    var currentFilePath: String?
        get() = sharedPreferences.getString(KEY_CURRENT_FILE_PATH, null)
        set(value) {
            sharedPreferences.edit()
                .putString(KEY_CURRENT_FILE_PATH, value)
                .apply()
        }
    
    /**
     * 現在開いているファイルが編集モードかどうか
     */
    var isEditMode: Boolean
        get() = sharedPreferences.getBoolean(KEY_IS_EDIT_MODE, false)
        set(value) {
            sharedPreferences.edit()
                .putBoolean(KEY_IS_EDIT_MODE, value)
                .apply()
        }
    
    /**
     * 現在のファイルを取得
     */
    fun getCurrentFile(): File? {
        return currentFilePath?.let { path ->
            val file = File(path)
            if (file.exists()) file else null
        }
    }
    
    /**
     * 現在のファイルをセット
     */
    fun setCurrentFile(file: File) {
        currentFilePath = file.absolutePath
    }
    
    /**
     * 現在のファイルをクリア
     */
    fun clearCurrentFile() {
        currentFilePath = null
        isEditMode = false
    }
    
    /**
     * 現在開いているファイルがあるかどうか
     */
    fun hasCurrentFile(): Boolean {
        return getCurrentFile() != null
    }
    
    /**
     * 現在のファイル名を取得
     */
    fun getCurrentFileName(): String? {
        return getCurrentFile()?.nameWithoutExtension
    }
    
    companion object {
        private const val PREFS_NAME = "current_file_prefs"
        private const val KEY_CURRENT_FILE_PATH = "current_file_path"
        private const val KEY_IS_EDIT_MODE = "is_edit_mode"
    }
}