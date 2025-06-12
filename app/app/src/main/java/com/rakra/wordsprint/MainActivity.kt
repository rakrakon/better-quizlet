package com.rakra.wordsprint

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rakra.wordsprint.data.dataStore.loadWordList
import com.rakra.wordsprint.data.dataStore.saveWordList
import com.rakra.wordsprint.data.database.Status
import com.rakra.wordsprint.data.database.WordEntry
import com.rakra.wordsprint.data.database.rememberWordViewModel
import com.rakra.wordsprint.screens.MainPage
import com.rakra.wordsprint.screens.MemorizationScreen
import com.rakra.wordsprint.screens.WordFilteringScreen
import com.rakra.wordsprint.screens.PracticeSelectScreen
import com.rakra.wordsprint.screens.UnitSelectScreen
import com.rakra.wordsprint.screens.quiz.QuizFlow
import com.rakra.wordsprint.screens.quiz.SharedQuizViewModel
import com.rakra.wordsprint.ui.theme.WordSprintTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WordSprintTheme {
                AppNavHost()
            }
        }
    }

    @Composable
    fun AppNavHost() {
        val navController = rememberNavController()
        val databaseViewModel = rememberWordViewModel()
        val sharedQuizViewModel: SharedQuizViewModel = viewModel()

        NavHost(
            navController = navController,
            startDestination = "main"
        ) {
            composable("main") {
                MainPage(navController)
            }
            composable("unit_selection") {
                UnitSelectScreen(navController)
            }
            composable(
                route = "unit_screen/{unitNumber}",
                arguments = listOf(navArgument("unitNumber") { type = NavType.IntType })
            ) { backStackEntry ->
                val unit = backStackEntry.arguments?.getInt("unitNumber") ?: 1

                // TODO: Make This actually work :[
                val practiceStates = remember { List(10) { false } }

                PracticeSelectScreen(
                    navController = navController,
                    unit = unit,
                    practiceStates = practiceStates,
                )
            }
            composable(
                route = "filtering/{unit}",
                arguments = listOf(navArgument("unit") { type = NavType.IntType })
            ) { backStackEntry ->
                val unit = backStackEntry.arguments?.getInt("unit") ?: 0
                WordFilteringScreen(
                    navController = navController,
                    unit = unit,
                    databaseViewModel = databaseViewModel,
                )
            }
            composable(
                route = "memorization/{unit}",
                arguments = listOf(
                    navArgument("unit") { type = NavType.IntType },

                    )
            ) { backStackEntry ->
                val unit = backStackEntry.arguments?.getInt("unit") ?: 0

                val wordsState = databaseViewModel.getWordsByStatus(unit, Status.UNKNOWN)
                val words = wordsState.collectAsState().value

                if (words.isNotEmpty()) {
                    sharedQuizViewModel.wordList = words
                    MemorizationScreen(
                        navController = navController,
                        unit = unit,
                        words = words
                    )
                } else {
                    CircularProgressIndicator()
                }
            }
            composable(
                route = "quiz/{unit}/{isFirst}/{mistakes}",
                arguments = listOf(
                    navArgument("unit") { type = NavType.IntType },
                    navArgument("isFirst") { type = NavType.BoolType },
                    navArgument("mistakes") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val context = LocalContext.current

                val unit = backStackEntry.arguments?.getInt("unit") ?: 0
                val isFirst = backStackEntry.arguments?.getBoolean("isFirst") ?: true
                val mistakes = backStackEntry.arguments?.getInt("mistakes") ?: 0

                val newWords = sharedQuizViewModel.wordList

                if (mistakes > 1 && isFirst) {
                    MemorizationScreen(
                        navController = navController,
                        unit = unit,
                        words = newWords
                    )
                } else {
                    // Collect your randomWordsFlow from databaseViewModel as State
                    val knownWords by databaseViewModel.randomWordsFlow.collectAsState(initial = emptyList())

                    // Load recentWords from DataStore
                    var recentWords by remember { mutableStateOf<List<WordEntry>>(emptyList()) }
                    LaunchedEffect(Unit) {
                        recentWords = loadWordList(context)
                    }

                    val combinedWords = remember(newWords, recentWords, knownWords) {
                        val initialList = (newWords + recentWords).distinctBy { it.id }.toMutableList()

                        if (initialList.size < 25) {
                            val needed = 25 - initialList.size

                            val additionalWords = knownWords
                                .filter { knownWord -> initialList.none { it.id == knownWord.id } }
                                .take(needed)

                            initialList.addAll(additionalWords)
                        }

                        initialList
                    }

                    var onCompletion = suspend { }
                    if (!isFirst) {
                        onCompletion = {
                            saveWordList(context, newWords)
                        }
                    }

                    QuizFlow(
                        navController = navController,
                        wordGroup = combinedWords,
                        unit = unit,
                        onCompletion = onCompletion
                    )
                }
            }
        }
    }
}
