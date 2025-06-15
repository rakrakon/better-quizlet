package com.rakra.wordsprint.screens.quiz

import androidx.lifecycle.ViewModel
import com.rakra.wordsprint.data.wordsDatabase.WordEntry

class SharedQuizViewModel : ViewModel() {
    var wordList: List<WordEntry> = emptyList()
}
