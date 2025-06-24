package com.rakra.wordsprint

import WordViewModel
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rakra.wordsprint.data.dataStore.loadWordList
import com.rakra.wordsprint.data.dataStore.saveWordList
import com.rakra.wordsprint.data.progressionDatabase.ProgressStatus
import com.rakra.wordsprint.data.progressionDatabase.ProgressionViewModel
import com.rakra.wordsprint.data.progressionDatabase.rememberProgressionViewModel
import com.rakra.wordsprint.data.wordsDatabase.Status
import com.rakra.wordsprint.data.wordsDatabase.WordEntry
import com.rakra.wordsprint.data.wordsDatabase.rememberWordViewModel
import com.rakra.wordsprint.screens.MainPage
import com.rakra.wordsprint.screens.MemorizationScreen
import com.rakra.wordsprint.screens.PracticeSelectScreen
import com.rakra.wordsprint.screens.UnitSelectScreen
import com.rakra.wordsprint.screens.quiz.QuizFlow
import com.rakra.wordsprint.screens.quiz.generateQuestions
import com.rakra.wordsprint.ui.theme.WordSprintTheme
import kotlinx.coroutines.flow.first

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

        NavHost(
            navController = navController,
            startDestination = "main",
        ) {
            composable("main") {
                MainPage(navController)
            }
            composable("unit_selection") {
                UnitSelectScreen(navController, progressionViewModel)
            }
            composable(
                route = "unit_screen/{unitNumber}",
                arguments = listOf(navArgument("unitNumber") { type = NavType.IntType }),
            ) { backStackEntry ->
                val unit = backStackEntry.arguments?.getInt("unitNumber") ?: FIRST_QUIZ_MAX_MISTAKES

                val practiceStates by progressionViewModel.loadPracticesForUnit(unit)
                    .collectAsState()

                Log.d("NAVIGATION", "PRACTICE STATES: $practiceStates")
                val practiceList = practiceStates.map { entry -> entry.completion }

                PracticeSelectScreen(
                    navController = navController,
                    unit = unit,
                    practiceStates = practiceList,
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

                var currentWords by remember { mutableStateOf<List<WordEntry>>(emptyList()) }
                var initialized by remember { mutableStateOf(false) }

                val notSelectedWords by databaseViewModel.getWordsByStatus(
                    unit,
                    Status.NOT_SELECTED
                )
                    .collectAsState()


                LaunchedEffect(notSelectedWords) {
                    val progressEntry = progressionViewModel.getEntrySuspend(unit, practice)
                    Log.d(
                        "DEBUG",
                        "WORD IDS FOR UNIT $unit PRACTICE $practice\n ${progressEntry!!.quizWordsIds}"
                    )
                    Log.d("DEBUG", "IS_EMPTY: ${progressEntry.quizWordsIds.isEmpty()}")
                    currentWords = if (progressEntry.quizWordsIds.isEmpty()) {
                        notSelectedWords.take(10)
                    } else {
                        databaseViewModel.fetchWordsByIds(progressEntry.quizWordsIds)
                    }
                    initialized = true
                    Log.d(
                        "NAVIGATION",
                        "MEMORIZATION SCREEN INITIALIZED WITH WORDS:\b$currentWords"
                    )
                }


                if (initialized && currentWords.isNotEmpty()) {
                    MemorizationScreen(
                        navController = navController,
                        unit = unit,
                        practice = practice,
                        initialWords = currentWords,
                        onContinue = navigateToQuizFirstTime(
                            databaseViewModel,
                            progressionViewModel,
                            unit,
                            practice,
                            navController
                        ),
                        databaseViewModel = databaseViewModel
                    )
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

                val newWords by produceState(initialValue = emptyList<WordEntry>(), unit, practice) {
                    val progressEntry = progressionViewModel.getEntrySuspend(unit, practice)
                    value = databaseViewModel.fetchWordsByIds(progressEntry!!.quizWordsIds)
                }

                Log.d("DEBUG", "WORDS FOR QUIZ:\n $newWords")

                if (isFirst && mistakes > FIRST_QUIZ_MAX_MISTAKES) {
                    MemorizationScreen(
                        navController = navController,
                        unit = unit,
                        initialWords = newWords,
                        practice = practice,
                        onContinue = navigateToQuizFirstTime(
                            databaseViewModel,
                            progressionViewModel,
                            unit,
                            practice,
                            navController,
                        ),
                        databaseViewModel = databaseViewModel
                    )
                    return@composable
                }

                // Collect your randomWordsFlow from databaseViewModel as State
                val knownWords by databaseViewModel.knownWordsFlow.collectAsState(initial = emptyList())

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

                val randomEntries by produceState(initialValue = emptyList(), unit) {
                    value = databaseViewModel.getRandomizedWordsFlow(unit).first()
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
                            progressionViewModel.getEntry(unit, practice) {
                                progressionViewModel.update(
                                    it!!.copy(completion = ProgressStatus.COMPLETED),
                                )
                            }
                        }
                    }

                val questions = remember(isFirst, newWords, combinedWords, randomEntries) {
                    generateQuestions(
                        quizWords = if (isFirst) newWords else combinedWords,
                        randomEntries = randomEntries.filterNot { wordEntry -> newWords.contains(wordEntry) }
                    )
                }

                if (questions.isNotEmpty()) {
                    QuizFlow(
                        navController = navController,
                        questions = questions,
                        unit = unit,
                        practice = practice,
                        onCompletion = onCompletion,
                    )
                }
            }
        }
    }

    @Composable
    private fun navigateToQuizFirstTime(
        databaseViewModel: WordViewModel,
        progressionViewModel: ProgressionViewModel,
        unit: Int,
        practice: Int,
        navController: NavHostController
    ) = { words: List<WordEntry>, knownWords: List<WordEntry> ->
        Log.d("NAVIGATION", "NAVIGATION TO QUIZ TRIGGERED!")

        // Update Word Database
        val quizWordsIds = mutableListOf<Int>()
        words.forEach { wordEntry: WordEntry ->
            databaseViewModel.updateWord(wordEntry.copy(status = Status.UNKNOWN))
            quizWordsIds.add(wordEntry.id)
        }

        // Update progression
        progressionViewModel.getEntry(unit, practice) {
            progressionViewModel.update(it!!.copy(quizWordsIds = quizWordsIds))
        }

        knownWords.forEach { wordEntry ->
            databaseViewModel.updateWord(wordEntry.copy(status =Status.KNOWN))
        }

        // Will Always be the first quiz after the memorization screen, Also there will be 0 mistakes.
        navController.navigate("quiz/$unit/$practice/true/0")
    }
}
