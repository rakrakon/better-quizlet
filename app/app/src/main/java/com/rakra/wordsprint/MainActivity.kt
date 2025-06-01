package com.rakra.wordsprint

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.rakra.wordsprint.ui.theme.WordSprintTheme

val ALEF_FONT = FontFamily(
    Font(R.font.alef_regular, FontWeight.Normal)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WordSprintTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WordSprintTheme {
        Greeting("Android")
    }
}

val wordList = listOf(
    WordMeaning("שלום", "ברכה פשוטה"),
    WordMeaning("אבנט", "חגורה רחבה"),
    WordMeaning("תינוק", "ילד קטן"),
    WordMeaning("כסא", "ריהוט לישיבה"),
    WordMeaning("עכבר", "חיית מכרסם"),
    WordMeaning("מגדל", "בניין גבוה")
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

    Column(modifier = Modifier.fillMaxWidth()) {
        // Progress bar
        LinearProgressIndicator(
            progress = {
                (questionIndex + if (showResult) 1 else 0) / questions.size.toFloat()
            },
            modifier = Modifier.fillMaxWidth().padding(top = 56.dp, start = 8.dp, end = 12.dp).height(12.dp)
            ,
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )

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
    val isCorrect = selectedAnswer == correctMeaning

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Main content
        Column(
            modifier = Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = word,
                style = MaterialTheme.typography.headlineLarge,
                fontSize = TextUnit(124.0F, TextUnitType.Sp),
                fontFamily = ALEF_FONT
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                options.forEach { option ->
                    Button(
                        onClick = { onOptionSelected(option) },
                        enabled = selectedAnswer == null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                selectedAnswer == null -> MaterialTheme.colorScheme.primary
                                option == correctMeaning -> MaterialTheme.colorScheme.secondary
                                option == selectedAnswer -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.primary
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = option,
                            fontSize = TextUnit(24.0F, TextUnitType.Sp),
                            fontFamily = ALEF_FONT
                        )
                    }
                }
            }
        }

        // Overlayed result
        if (showResult) {
            val infiniteTransition = rememberInfiniteTransition()
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(
                        color = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isCorrect) "!נכון מאד" else ":( לא נכון",
                    color = Color.White,
                    fontSize = TextUnit(40f, TextUnitType.Sp),
                    fontFamily = ALEF_FONT
                )

                if (!isCorrect) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "התשובה הנכונה היא: $correctMeaning",
                        color = Color.White,
                        fontSize = TextUnit(20f, TextUnitType.Sp),
                        fontFamily = ALEF_FONT
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "המשך",
                    fontSize = TextUnit(32f, TextUnitType.Sp),
                    fontFamily = ALEF_FONT,
                    color = Color.White,
                    modifier = Modifier
                        .alpha(alpha)
                        .clickable { onNext() }
                )
            }
        }
    }
}



