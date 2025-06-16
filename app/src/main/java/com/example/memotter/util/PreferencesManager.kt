package com.example.memotter.util

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    enum class FileMode {
        DAILY, CONTINUOUS
    }
    
    var fileMode: FileMode
        get() = FileMode.valueOf(
            sharedPreferences.getString(KEY_FILE_MODE, FileMode.DAILY.name) 
                ?: FileMode.DAILY.name
        )
        set(value) {
            sharedPreferences.edit()
                .putString(KEY_FILE_MODE, value.name)
                .apply()
        }
    
    var isDarkMode: Boolean
        get() = sharedPreferences.getBoolean(KEY_DARK_MODE, false)
        set(value) {
            sharedPreferences.edit()
                .putBoolean(KEY_DARK_MODE, value)
                .apply()
        }
    
    var fontSize: Float
        get() = sharedPreferences.getFloat(KEY_FONT_SIZE, 16f)
        set(value) {
            sharedPreferences.edit()
                .putFloat(KEY_FONT_SIZE, value)
                .apply()
        }
    
    var customDirectoryPath: String?
        get() = sharedPreferences.getString(KEY_CUSTOM_DIRECTORY, null)
        set(value) {
            sharedPreferences.edit()
                .putString(KEY_CUSTOM_DIRECTORY, value)
                .apply()
        }
    
    var useCustomDirectory: Boolean
        get() = sharedPreferences.getBoolean(KEY_USE_CUSTOM_DIRECTORY, false)
        set(value) {
            sharedPreferences.edit()
                .putBoolean(KEY_USE_CUSTOM_DIRECTORY, value)
                .apply()
        }
    
    fun clearCustomDirectory() {
        customDirectoryPath = null
        useCustomDirectory = false
    }
    
    companion object {
        private const val PREFS_NAME = "memotter_preferences"
        private const val KEY_FILE_MODE = "file_mode"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_FONT_SIZE = "font_size"
        private const val KEY_CUSTOM_DIRECTORY = "custom_directory_path"
        private const val KEY_USE_CUSTOM_DIRECTORY = "use_custom_directory"
    }
}