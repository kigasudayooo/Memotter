package com.example.memotter.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.memotter.data.repository.MemoRepository

class TimelineViewModelFactory(
    private val repository: MemoRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimelineViewModel::class.java)) {
            return TimelineViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}