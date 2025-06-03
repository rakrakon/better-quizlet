package com.rakra.wordsprint.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakra.wordsprint.database.WordDao
import com.rakra.wordsprint.database.WordEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WordViewModel(private val wordDao: WordDao) : ViewModel() {

    private val _words = MutableStateFlow<List<WordEntry>>(emptyList())
    val words: StateFlow<List<WordEntry>> = _words.asStateFlow()

    fun loadWords() {
        viewModelScope.launch {
            _words.value = wordDao.getAllWords()
        }
    }

    fun insertWords(wordList: List<WordEntry>) {
        viewModelScope.launch {
            wordDao.insertAll(wordList)
            loadWords()
        }
    }

    fun insertWord(word: WordEntry) {
        viewModelScope.launch {
            wordDao.insertWord(word)
            loadWords()
        }
    }

    fun updateWord(word: WordEntry) {
        viewModelScope.launch {
            wordDao.updateWord(word)
            loadWords()
        }
    }

    fun deleteWord(word: WordEntry) {
        viewModelScope.launch {
            wordDao.deleteWord(word)
            loadWords()
        }
    }

    fun hasAtLeastNUnknownWords(unit: Int, n: Int = 10, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = wordDao.hasAtLeastNUnknownWords(unit, n)
            callback(result)
        }
    }
}
