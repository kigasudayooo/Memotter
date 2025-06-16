package com.example.memotter.ui.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memotter.data.model.Memo
import com.example.memotter.data.repository.MemoRepository
import kotlinx.coroutines.launch

class FavoritesViewModel(private val repository: MemoRepository) : ViewModel() {

    val favoriteMemos: LiveData<List<Memo>> = repository.getFavoriteMemos()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    init {
        refreshFavorites()
    }

    fun refreshFavorites() {
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
}