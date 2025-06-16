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
import com.rakra.wordsprint.data.progressionDatabase.ProgressStatus
import com.rakra.wordsprint.data.progressionDatabase.rememberProgressionViewModel
import com.rakra.wordsprint.data.wordsDatabase.Status
import com.rakra.wordsprint.data.wordsDatabase.WordEntry
import com.rakra.wordsprint.data.wordsDatabase.rememberWordViewModel
import com.rakra.wordsprint.screens.MainPage
import com.rakra.wordsprint.screens.MemorizationScreen
import com.rakra.wordsprint.screens.PracticeSelectScreen
import com.rakra.wordsprint.screens.UnitSelectScreen
import com.rakra.wordsprint.screens.WordFilteringScreen
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
        val progressionViewModel = rememberProgressionViewModel()
        val sharedQuizViewModel: SharedQuizViewModel = viewModel()

        NavHost(
            navController = navController,
            startDestination = "main",
        ) {
            composable("main") {
                MainPage(navController)
            }
            composable("unit_selection") {
                UnitSelectScreen(navController)
            }
            composable(
                route = "unit_screen/{unitNumber}",
                arguments = listOf(navArgument("unitNumber") { type = NavType.IntType }),
            ) { backStackEntry ->
                val unit = backStackEntry.arguments?.getInt("unitNumber") ?: FIRST_QUIZ_MAX_MISTAKES

                val practiceStates by progressionViewModel.loadPracticesForUnit(unit)
                    .collectAsState()

                val practiceList = remember(practiceStates) {
                    practiceStates.map { entry -> entry.completion }
                }

                PracticeSelectScreen(
                    navController = navController,
                    unit = unit,
                    practiceStates = practiceList,
                )
            }
            composable(
                route = "filtering/{unit}/{practice}",
                arguments = listOf(
                    navArgument("unit") { type = NavType.IntType },
                    navArgument("practice") { type = NavType.IntType }
                ),
            ) { backStackEntry ->
                val unit = backStackEntry.arguments?.getInt("unit") ?: 0
                val practice = backStackEntry.arguments?.getInt("practice") ?: 0

                val hasEnoughUnknown by databaseViewModel.hasAtLeastNUnknownWords(unit)
                    .collectAsState()

                if (hasEnoughUnknown) {
                    navController.navigate("memorization/$unit/$practice")
                    return@composable
                }

                WordFilteringScreen(
                    navController = navController,
                    unit = unit,
                    databaseViewModel = databaseViewModel,
                    practice = practice
                )
            }
            composable(
                route = "memorization/{unit}/{practice}",
                arguments = listOf(
                    navArgument("unit") { type = NavType.IntType },
                    navArgument("practice") { type = NavType.IntType },
                ),
            ) { backStackEntry ->
                val unit = backStackEntry.arguments?.getInt("unit") ?: 0
                val practice = backStackEntry.arguments?.getInt("practice") ?: 0

                val wordsState = databaseViewModel.getWordsByStatus(unit, Status.UNKNOWN)
                val words = wordsState.collectAsState().value

                if (words.isNotEmpty()) {
                    sharedQuizViewModel.wordList = words
                    MemorizationScreen(
                        navController = navController,
                        unit = unit,
                        practice = practice,
                        words = words,
                    )
                } else {
                    CircularProgressIndicator()
                }
            }
            composable(
                route = "quiz/{unit}/{practice}/{isFirst}/{mistakes}",
                arguments = listOf(
                    navArgument("unit") { type = NavType.IntType },
                    navArgument("practice") { type = NavType.IntType },
                    navArgument("isFirst") { type = NavType.BoolType },
                    navArgument("mistakes") { type = NavType.IntType },
                ),
            ) { backStackEntry ->
                val context = LocalContext.current

                val unit = backStackEntry.arguments?.getInt("unit") ?: 0
                val practice = backStackEntry.arguments?.getInt("practice") ?: 0
                val isFirst = backStackEntry.arguments?.getBoolean("isFirst") ?: true
                val mistakes = backStackEntry.arguments?.getInt("mistakes") ?: 0

                val newWords = sharedQuizViewModel.wordList

                if (isFirst && mistakes > FIRST_QUIZ_MAX_MISTAKES) {
                    MemorizationScreen(
                        navController = navController,
                        unit = unit,
                        words = newWords,
                        practice = practice
                    )
                    return@composable
                }

                // Collect your randomWordsFlow from databaseViewModel as State
                val knownWords by databaseViewModel.randomWordsFlow.collectAsState(initial = emptyList())

                // Load recentWords from DataStore
                var recentWords by remember { mutableStateOf<List<WordEntry>>(emptyList()) }
                LaunchedEffect(Unit) {
                    recentWords = loadWordList(context)
                }

                val combinedWords = remember(newWords, recentWords, knownWords) {
                    val initialList =
                        (newWords + recentWords).distinctBy { it.id }.toMutableList()

                    if (initialList.size < SECOND_QUIZ_WORD_SIZE) {
                        val needed = SECOND_QUIZ_WORD_SIZE - initialList.size

                        val additionalWords = knownWords
                            .filter { knownWord -> initialList.none { it.id == knownWord.id } }
                            .take(needed)

                        initialList.addAll(additionalWords)
                    }

                    initialList
                }

                val onCompletion: suspend () -> Unit =
                    if (isFirst || mistakes > SECOND_QUIZ_MAX_MISTAKES) {
                        {
                            navController.navigate("quiz/$unit/$practice/false/$mistakes") // False To signal first quiz completed
                        }
                    } else {
                        {
                            saveWordList(context, newWords)
                            navController.popBackStack(
                                route = "unit_screen/$unit",
                                inclusive = false,
                            )

                            recentWords.forEach { wordEntry ->
                                databaseViewModel.updateWord(
                                    wordEntry.copy(status = Status.KNOWN),
                                )
                            }
                            newWords.forEach { wordEntry ->
                                databaseViewModel.updateWord(
                                    wordEntry.copy(status = Status.RECENT),
                                )
                            }

                            // Update Progression
                            progressionViewModel.getEntry(unit, practice, {
                                progressionViewModel.update(
                                    it!!.copy(completion = ProgressStatus.COMPLETED)
                                )
                            })
                        }
                    }

                QuizFlow(
                    navController = navController,
                    wordGroup = combinedWords,
                    unit = unit,
                    onCompletion = onCompletion,
                )
            }
        }
    }
}
