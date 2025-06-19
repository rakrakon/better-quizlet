package com.rakra.wordsprint.screens.quiz

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakra.wordsprint.ui.theme.BUTTON_CONTAINER_COLOR
import com.rakra.wordsprint.ui.theme.BUTTON_CONTENT_COLOR
import com.rakra.wordsprint.ui.theme.RUBIK_FONT

@Composable
fun QuizCompletionDialog(
    correctAnswers: Int,
    totalQuestions: Int,
    onContinue: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { /* Disable dismiss on outside click */ },
        title = {
            Text(
                text = "!כל הכבוד",
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
                fontFamily = RUBIK_FONT,
            )
        },
        text = {
            Text(
                text = ".סיימת את השאלון\n" +
                    ".צדקת ב - $correctAnswers מתוך $totalQuestions שאלות",
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
                fontFamily = RUBIK_FONT,
            )
        },
        confirmButton = {
            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BUTTON_CONTAINER_COLOR,
                    contentColor = BUTTON_CONTENT_COLOR,
                ),
                shape = RoundedCornerShape(70),
                modifier = Modifier
                    .padding(8.dp),
            ) {
                Text(
                    text = "הבא",
                    fontSize = 20.sp,
                    fontFamily = RUBIK_FONT,
                )
            }
        },
    )
}
