package com.rakra.wordsprint.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rakra.wordsprint.viewmodel.WordViewModel

class WordViewModelFactory(private val wordDao: WordDao) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WordViewModel::class.java)) {
            return WordViewModel(wordDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
