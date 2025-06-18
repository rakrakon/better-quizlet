package com.rakra.wordsprint.data.progressionDatabase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ProgressionViewModelFactory(
    private val dao: ProgressionDao,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProgressionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProgressionViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
