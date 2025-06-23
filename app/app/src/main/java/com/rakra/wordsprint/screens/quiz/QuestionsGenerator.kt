package com.rakra.wordsprint.screens.quiz

import com.rakra.wordsprint.data.wordsDatabase.WordEntry


fun generateQuestions(quizWords: List<WordEntry>, randomEntries: List<WordEntry>): List<Question> {
    val allOptions = quizWords + randomEntries
    return quizWords.shuffled().map { current ->
        val incorrectMeanings = allOptions
            .filter { it.word != current.word }
            .shuffled()
            .take(3)
            .map { it.meaning }

        val options = (incorrectMeanings + current.meaning).shuffled()

        Question(word = current.word, options = options, correctMeaning = current.meaning)
    }
}