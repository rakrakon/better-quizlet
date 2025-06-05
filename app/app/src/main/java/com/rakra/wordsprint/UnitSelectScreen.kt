package com.rakra.wordsprint

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.rakra.wordsprint.ui.theme.BACKGROUND_COLOR
import com.rakra.wordsprint.ui.theme.BUTTON_CONTAINER_COLOR
import com.rakra.wordsprint.ui.theme.BUTTON_CONTENT_COLOR
import com.rakra.wordsprint.ui.theme.RUBIK_FONT

@Composable
fun UnitSelectScreen(navController: NavHostController) {
    val unitList = (1..10).map { "יחידה $it" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BACKGROUND_COLOR)
            .padding(WindowInsets.statusBars.asPaddingValues()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = {
            item {
                Text(
                    text = "למידת מילים",
                    fontSize = 42.sp,
                    fontFamily = RUBIK_FONT,
                    color = Color.White,
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            items(unitList) { unit ->
                Button(
                    onClick = {
                        val num = unit.removePrefix("יחידה ").toInt()
                        navController.navigate("unit_screen/$num")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BUTTON_CONTAINER_COLOR,
                        contentColor = BUTTON_CONTENT_COLOR,
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 8.dp)
                        .border(
                            2.dp,
                            SolidColor(BUTTON_OUTLINE_COLOR),
                            RoundedCornerShape(20.dp)
                        ),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = unit,
                        fontSize = 20.sp,
                        fontFamily = RUBIK_FONT,
                        color = Color.White,
                    )
                }
            }
        }
    )
}
