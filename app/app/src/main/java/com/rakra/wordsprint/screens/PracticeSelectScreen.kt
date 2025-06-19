package com.rakra.wordsprint.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.rakra.wordsprint.data.progressionDatabase.ProgressStatus
import com.rakra.wordsprint.ui.theme.BACKGROUND_COLOR
import com.rakra.wordsprint.ui.theme.BACK_BUTTON_COLOR
import com.rakra.wordsprint.ui.theme.BUTTON_CONTAINER_COLOR
import com.rakra.wordsprint.ui.theme.COMPLETE_COLOR
import com.rakra.wordsprint.ui.theme.RUBIK_FONT

@Composable
fun PracticeSelectScreen(
    navController: NavHostController,
    unit: Int,
    practiceStates: List<ProgressStatus>,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BACKGROUND_COLOR)
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(horizontal = 12.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = {
                    navController.popBackStack(route = "unit_selection", inclusive = false)
                },
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    modifier = Modifier.size(48.dp),
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Back",
                    tint = BACK_BUTTON_COLOR,
                )
            }

            Text(
                text = "יחידה $unit",
                fontSize = 32.sp,
                fontFamily = RUBIK_FONT,
                color = Color.White,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
            )
        }

        practiceStates.forEachIndexed { index, progress ->
            val backgroundColor =
                if (progress == ProgressStatus.COMPLETED) COMPLETE_COLOR else BUTTON_CONTAINER_COLOR

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(backgroundColor)
                    .then(
                        if (progress == ProgressStatus.NOT_STARTED) {
                            Modifier.clickable {
                                navController.navigate("filtering/$unit/${index + 1}")
                            }
                        } else {
                            Modifier
                        },
                    )
                    .padding(vertical = 16.dp)
                    .padding(horizontal = 16.dp),
            ) {
                Text(
                    text = "תרגול מספר ${index + 1}",
                    fontSize = 20.sp,
                    fontFamily = RUBIK_FONT,
                    color = Color.White,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
