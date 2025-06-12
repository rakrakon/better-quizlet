package com.rakra.wordsprint.data.database

import WordViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun rememberWordViewModel(): WordViewModel {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val wordDao = remember { db.wordDao() }
    return viewModel(factory = WordViewModelFactory(wordDao))
}
