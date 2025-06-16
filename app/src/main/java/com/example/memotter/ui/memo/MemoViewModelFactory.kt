package com.example.memotter.ui.memo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.memotter.data.repository.MemoRepository

class MemoViewModelFactory(
    private val repository: MemoRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MemoViewModel::class.java)) {
            return MemoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}