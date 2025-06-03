package com.rakra.wordsprint

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakra.wordsprint.ui.theme.WordSprintTheme

@Preview(showBackground = true)
@Composable
fun UnitScreenPreview() {
    val practiceStates = listOf(true, true, false, false, false, false)

    WordSprintTheme {
        UnitScreen(
            unitNumber = 2,
            practiceStates = practiceStates
        ) { practiceNum ->
            println("Clicked on practice $practiceNum")
        }
    }
}


@Composable
fun UnitScreen(
    unitNumber: Int = 1,
    practiceStates: List<Boolean>,
    onPracticeClick: (Int) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BACKGROUND_COLOR)
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "יחידה $unitNumber",
            fontSize = 32.sp,
            fontFamily = RUBIK_FONT,
            color = Color.White,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        )

        practiceStates.forEachIndexed { index, isCompleted ->
            val backgroundColor = if (isCompleted) Color(0xFF81C784) else Color(0xFF2C2733)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(backgroundColor)
                    .then(
                        if (!isCompleted) Modifier.clickable { onPracticeClick(index + 1) }
                        else Modifier
                    )
                    .padding(vertical = 16.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "תרגול מספר ${index + 1}",
                    fontSize = 20.sp,
                    fontFamily = RUBIK_FONT,
                    color = Color.White,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
