package com.example.memotter.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memotter.data.model.Hashtag
import com.example.memotter.data.model.Memo
import com.example.memotter.data.repository.MemoRepository
import kotlinx.coroutines.launch

class SearchViewModel(private val repository: MemoRepository) : ViewModel() {

    private val _searchResults = MutableLiveData<List<Memo>>()
    val searchResults: LiveData<List<Memo>> = _searchResults

    val popularHashtags: LiveData<List<Hashtag>> = repository.getAllHashtags()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private var currentSearchQuery = ""

    fun searchMemos(query: String) {
        currentSearchQuery = query
        repository.searchMemos(query).observeForever { memos ->
            _searchResults.value = memos
        }
    }

    fun searchByHashtag(hashtag: String) {
        currentSearchQuery = hashtag
        repository.getMemosByHashtag(hashtag).observeForever { memos ->
            _searchResults.value = memos
        }
    }

    fun clearSearch() {
        currentSearchQuery = ""
        _searchResults.value = emptyList()
    }

    fun toggleFavorite(memoId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            try {
                repository.toggleFavorite(memoId, isFavorite)
                // Refresh current search if active
                if (currentSearchQuery.isNotEmpty()) {
                    refreshCurrentSearch()
                }
            } catch (e: Exception) {
                _errorMessage.value = "お気に入りの更新に失敗しました: ${e.message}"
            }
        }
    }

    private fun refreshCurrentSearch() {
        if (currentSearchQuery.isNotEmpty()) {
            searchMemos(currentSearchQuery)
        }
    }
}