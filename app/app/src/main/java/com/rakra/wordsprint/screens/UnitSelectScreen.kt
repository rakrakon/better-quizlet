package com.rakra.wordsprint.screens

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.rakra.wordsprint.NUMBER_OF_UNITS
import com.rakra.wordsprint.data.progressionDatabase.ProgressionViewModel
import com.rakra.wordsprint.ui.theme.BACKGROUND_COLOR
import com.rakra.wordsprint.ui.theme.BUTTON_CONTAINER_COLOR
import com.rakra.wordsprint.ui.theme.RUBIK_FONT

@Composable
fun UnitSelectScreen(navController: NavHostController, progressionViewModel: ProgressionViewModel) {
    val unitList = (1..NUMBER_OF_UNITS).map { "יחידה $it" }

    val progressByUnit by produceState<Map<Int, Float>>(initialValue = emptyMap()) {
        value = progressionViewModel.getProgressionMap()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BACKGROUND_COLOR)
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = "בחירת יחידה",
            fontSize = 32.sp,
            fontFamily = RUBIK_FONT,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
        )

        unitList.forEach { unit ->
            val num = unit.removePrefix("יחידה ").toInt()
            val progress = progressByUnit[num] ?: 0f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(BUTTON_CONTAINER_COLOR)
                    .clickable {
                        navController.navigate("unit_screen/$num")
                    }
                    .padding(vertical = 12.dp, horizontal = 16.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = unit,
                        fontSize = 20.sp,
                        fontFamily = RUBIK_FONT,
                        color = Color.White,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    androidx.compose.material3.LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(MaterialTheme.shapes.small),
                        color = Color(0xFF4CAF50),
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
