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
import androidx.compose.foundation.border
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

@Preview(showBackground = true)
@Composable
fun QuizPreview() {
    WordSprintTheme {
        QuizScreen()
    }
}

@Composable
fun QuizScreen() {
    val word = "אַבְנֵט"
    val options = listOf("חגורה רחבה", "משהו כלשהו", " משהו כלשהו 2", "משהו כלשהו 3").shuffled()
    val correctAnswer = "חגורה רחבה"

    var selectedAnswer by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = word,
            style = MaterialTheme.typography.headlineLarge,
            fontSize = TextUnit(124.0F, TextUnitType.Sp),
            fontFamily = ALEF_FONT
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column {
                options.forEach { option ->
                    Button(
                        onClick = { selectedAnswer = option },
                        enabled = selectedAnswer == null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                selectedAnswer == null -> MaterialTheme.colorScheme.primary
                                option == correctAnswer && selectedAnswer != null -> MaterialTheme.colorScheme.secondary
                                option == selectedAnswer -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.primary
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(text = option, fontSize = TextUnit(24.0F, TextUnitType.Sp), fontFamily = ALEF_FONT)
                    }
                }
            }

            selectedAnswer?.let {
                val isCorrect = it == correctAnswer

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
                        .size(width = 280.dp, height = 200.dp)
                        .background(
                            color = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text = if (isCorrect) "!נכון מאד" else ":( לא נכון",
                        color = Color.White,
                        fontSize = TextUnit(40f, TextUnitType.Sp),
                        fontFamily = ALEF_FONT,
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Text(
                        text = "המשך",
                        fontSize = TextUnit(32f, TextUnitType.Sp),
                        fontFamily = ALEF_FONT,
                        color = Color.White,
                        modifier = Modifier
                            .alpha(alpha)
                            .clickable {
                                // TODO: Implement logic to load next question or reset state
                            }
                    )
                }
            }

        }
    }
}
