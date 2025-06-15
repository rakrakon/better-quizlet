package com.rakra.wordsprint.data.progressionDatabase

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun rememberProgressionViewModel(): ProgressionViewModel {
    val context = LocalContext.current
    val db = remember { ProgressionDatabase.getDatabase(context) }
    val dao = remember { db.progressionDao() }
    return viewModel(factory = ProgressionViewModelFactory(dao))
}
