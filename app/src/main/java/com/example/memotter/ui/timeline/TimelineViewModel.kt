package com.example.memotter.ui.timeline

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memotter.data.model.Memo
import com.example.memotter.data.repository.MemoRepository
import kotlinx.coroutines.launch

class TimelineViewModel(private val repository: MemoRepository) : ViewModel() {

    val memos: LiveData<List<Memo>> = repository.getAllMemos()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    init {
        refreshMemos()
    }

    fun refreshMemos() {
        _isLoading.value = true
        // The LiveData from repository will automatically update
        _isLoading.value = false
    }

    fun toggleFavorite(memoId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            try {
                repository.toggleFavorite(memoId, isFavorite)
            } catch (e: Exception) {
                _errorMessage.value = "お気に入りの更新に失敗しました: ${e.message}"
            }
        }
    }

    fun deleteMemo(memoId: Long) {
        viewModelScope.launch {
            try {
                repository.softDeleteMemo(memoId)
            } catch (e: Exception) {
                _errorMessage.value = "メモの削除に失敗しました: ${e.message}"
            }
        }
    }

    fun cleanupOldDeletedMemos() {
        viewModelScope.launch {
            try {
                repository.cleanupOldDeletedMemos()
            } catch (e: Exception) {
                _errorMessage.value = "古いメモのクリーンアップに失敗しました: ${e.message}"
            }
        }
    }
}