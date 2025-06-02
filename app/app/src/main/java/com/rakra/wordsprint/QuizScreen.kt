package com.rakra.wordsprint

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
import androidx.compose.foundation.layout.wrapContentWidth
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
import com.rakra.wordsprint.ui.theme.WordSprintTheme

val BUTTON_OUTLINE_COLOR = Color(0xFF241F27)

private val PROGRESS_BAR_COLOR = Color(0xFFA3CEF1)

val wordList = listOf(
    WordMeaning("בְּצַוְותָּא", "ביחד"),
    WordMeaning("אבנט", "חגורה רחבה"),
    WordMeaning("בְּרַם", "אולם, אבל"),
    WordMeaning("גָלְמוּד", "בודד"),
    WordMeaning("דָּהוּי", "שצבעו נחלש והחוויר"),
    WordMeaning("גְּלָלִים", "צואת בעלי החיים")
)

fun generateQuestions(allWords: List<WordMeaning>): List<Question> {
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
        QuizFlow(wordList)
    }
}

@Composable
fun QuizFlow(wordGroup: List<WordMeaning>) {
    val questions = remember { generateQuestions(wordGroup) }
    var questionIndex by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }

    if (questionIndex >= questions.size) {
        LaunchedEffect(Unit) {
            TODO("All questions answered — handle quiz completion here.")
        }
        return
    }

    val currentQuestion = questions[questionIndex]
    val showResult = selectedAnswer != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BACKGROUND_COLOR)
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        Row {
            IconButton(
                onClick = { TODO("Back to main menu") },
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
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
        }


        // Question Screen below
        QuestionScreen(
            word = currentQuestion.word,
            options = currentQuestion.options,
            correctMeaning = currentQuestion.correctMeaning,
            selectedAnswer = selectedAnswer,
            showResult = showResult,
            onOptionSelected = { selectedAnswer = it },
            onNext = {
                selectedAnswer = null
                questionIndex++
            }
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
    onNext: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Main content
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(vertical = 32.dp),
                text = word,
                style = MaterialTheme.typography.headlineLarge,
                fontSize = TextUnit(124.0F, TextUnitType.Sp),
                color = Color.White,
                fontFamily = RUBIK_FONT
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                options.forEach { option ->
                    Button(
                        onClick = { onOptionSelected(option) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                selectedAnswer == null -> Color.Transparent
                                showResult && option == correctMeaning -> Color(0xFF2D4227)
                                selectedAnswer == option -> Color(0xFF4C1E1F)
                                else -> Color.Transparent
                            }),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .border(
                                2.dp,
                                SolidColor(BUTTON_OUTLINE_COLOR),
                                RoundedCornerShape(20.dp)
                            )
                            .height(92.dp)
                    ) {
                        Text(
                            text = option,
                            fontSize = TextUnit(24.0F, TextUnitType.Sp),
                            fontFamily = RUBIK_FONT,
                            color = Color.White
                        )
                    }
                }

                if (showResult) {
                    Button(
                        onClick = { onNext() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6E99DB),
                            contentColor = Color.White,
                        ),
                        modifier = Modifier
                            .width(250.dp)
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 52.dp)
                            .border(
                                2.dp,
                                SolidColor(BUTTON_OUTLINE_COLOR),
                                RoundedCornerShape(70)
                            ),
                        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = "הבא",
                            fontSize = 28.sp,
                            fontFamily = RUBIK_FONT,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}



