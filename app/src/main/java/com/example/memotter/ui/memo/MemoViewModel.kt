package com.example.memotter.ui.memo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memotter.data.model.Hashtag
import com.example.memotter.data.repository.MemoRepository
import com.example.memotter.util.HashtagExtractor
import kotlinx.coroutines.launch

class MemoViewModel(private val repository: MemoRepository) : ViewModel() {

    private val _hashtags = MutableLiveData<List<Hashtag>>()
    val hashtags: LiveData<List<Hashtag>> = _hashtags

    private val _saveResult = MutableLiveData<Boolean>()
    val saveResult: LiveData<Boolean> = _saveResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    init {
        loadHashtagSuggestions()
    }

    fun onContentChanged(content: String) {
        val hashtags = HashtagExtractor.extractHashtags(content)
        if (hashtags.isNotEmpty()) {
            // Filter suggestions based on current content
            loadHashtagSuggestionsForContent(content)
        } else {
            loadHashtagSuggestions()
        }
    }

    fun saveMemo(content: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val memoId = repository.insertMemo(content)
                _saveResult.value = memoId > 0
            } catch (e: Exception) {
                _saveResult.value = false
                _errorMessage.value = "メモの保存に失敗しました: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadHashtagSuggestions() {
        repository.getAllHashtags().observeForever { hashtagList ->
            _hashtags.value = hashtagList
        }
    }

    private fun loadHashtagSuggestionsForContent(content: String) {
        // Extract partial hashtag at cursor position for auto-complete
        val currentHashtags = HashtagExtractor.extractHashtags(content)
        
        repository.getAllHashtags().observeForever { hashtagList ->
            // Filter out hashtags already used in current content
            val filtered = hashtagList.filter { hashtag ->
                !currentHashtags.contains(hashtag.tag)
            }
            _hashtags.value = filtered
        }
    }
}