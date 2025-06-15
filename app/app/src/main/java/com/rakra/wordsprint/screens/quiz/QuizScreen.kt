package com.rakra.wordsprint.screens.quiz

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.rakra.wordsprint.AutoResizedText
import com.rakra.wordsprint.data.wordsDatabase.WordEntry
import com.rakra.wordsprint.ui.theme.BACKGROUND_COLOR
import com.rakra.wordsprint.ui.theme.BUTTON_CONTAINER_COLOR
import com.rakra.wordsprint.ui.theme.BUTTON_CONTENT_COLOR
import com.rakra.wordsprint.ui.theme.BUTTON_OUTLINE_COLOR
import com.rakra.wordsprint.ui.theme.PROGRESS_BAR_COLOR
import com.rakra.wordsprint.ui.theme.RUBIK_FONT
import com.rakra.wordsprint.ui.theme.WordSprintTheme

fun generateQuestions(allWords: List<WordEntry>): List<Question> {
    return allWords.shuffled().map { current ->
        val incorrectMeanings = allWords
            .filter { it.word != current.word }
            .shuffled()
            .take(3)
            .map { it.meaning }

        val options = (incorrectMeanings + current.meaning).shuffled()

        Question(word = current.word, options = options, correctMeaning = current.meaning)
    }
}

@Preview(showBackground = true)
@Composable
fun QuizPreview() {
    WordSprintTheme {
//        QuizFlow(rememberNavController(), listOf())
    }
}

@Composable
fun QuizFlow(
    navController: NavHostController,
    wordGroup: List<WordEntry>,
    unit: Int,
    onCompletion: suspend () -> Unit = {},
) {
    val questions = remember(wordGroup) { generateQuestions(wordGroup) }
    var questionIndex by remember(wordGroup) { mutableIntStateOf(0) }
    var selectedAnswer by remember(wordGroup) { mutableStateOf<String?>(null) }
    var mistakes by remember(wordGroup) { mutableIntStateOf(0) }

    Log.d("DEBUG/QUIZ", "Quiz Initialized with word group:\n $wordGroup")
    Log.d("DEBUG/QUIZ", "MISTAKES:$mistakes")

    if (questionIndex >= questions.size) {
        LaunchedEffect(Unit) {
            onCompletion.invoke()
        }
        return
    }

    val currentQuestion = questions[questionIndex]
    val showResult = selectedAnswer != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BACKGROUND_COLOR)
            .padding(WindowInsets.statusBars.asPaddingValues()),
    ) {
        Row {
            IconButton(
                onClick = {
                    navController.popBackStack(route = "unit_screen/$unit", inclusive = false)
                },
            ) {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Back",
                )
            }

            // Progress bar
            LinearProgressIndicator(
                progress = {
                    (questionIndex + if (showResult) 1 else 0) / questions.size.toFloat()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 13.dp, end = 12.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(10.dp)),
                color = PROGRESS_BAR_COLOR,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            )
        }

        // Question Screen
        QuestionScreen(
            word = currentQuestion.word,
            options = currentQuestion.options,
            correctMeaning = currentQuestion.correctMeaning,
            selectedAnswer = selectedAnswer,
            showResult = showResult,
            onOptionSelected = {
                selectedAnswer = it
                if (it != currentQuestion.correctMeaning) {
                    mistakes++
                }
            },
            onNext = {
                selectedAnswer = null
                questionIndex++
            },
        )
    }
}

@Composable
fun QuestionScreen(
    word: String,
    options: List<String>,
    correctMeaning: String,
    selectedAnswer: String?,
    onOptionSelected: (String) -> Unit,
    showResult: Boolean,
    onNext: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        // Main content
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AutoResizedText(
                modifier = Modifier.padding(vertical = 32.dp),
                text = word,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                options.forEach { option ->
                    Button(
                        onClick = { onOptionSelected(option) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                selectedAnswer == null -> BUTTON_CONTAINER_COLOR
                                showResult && option == correctMeaning -> Color(0xFF2D4227)
                                selectedAnswer == option -> Color(0xFF4C1E1F)
                                else -> BUTTON_CONTAINER_COLOR
                            },
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .border(
                                2.dp,
                                SolidColor(BUTTON_OUTLINE_COLOR),
                                RoundedCornerShape(20.dp),
                            )
                            .height(92.dp),
                    ) {
                        Text(
                            text = option,
                            fontSize = TextUnit(24.0F, TextUnitType.Sp),
                            fontFamily = RUBIK_FONT,
                            color = Color.White,
                        )
                    }
                }

                if (showResult) {
                    Button(
                        onClick = { onNext() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6E99DB),
                            contentColor = BUTTON_CONTENT_COLOR,
                        ),
                        modifier = Modifier
                            .width(250.dp)
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 52.dp)
                            .border(
                                2.dp,
                                SolidColor(BUTTON_OUTLINE_COLOR),
                                RoundedCornerShape(70),
                            ),
                        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
                    ) {
                        Text(
                            text = "הבא",
                            fontSize = 28.sp,
                            fontFamily = RUBIK_FONT,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}
