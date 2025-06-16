package com.rakra.wordsprint.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.rakra.wordsprint.data.wordsDatabase.WordEntry
import com.rakra.wordsprint.ui.theme.BACKGROUND_COLOR
import com.rakra.wordsprint.ui.theme.BUTTON_CONTENT_COLOR
import com.rakra.wordsprint.ui.theme.BUTTON_OUTLINE_COLOR
import com.rakra.wordsprint.ui.theme.RUBIK_FONT

@Composable
fun MemorizationScreen(
    navController: NavHostController,
    unit: Int,
    practice: Int,
    words: List<WordEntry>,
) {
    val expandedMap = remember { mutableStateMapOf<String, Boolean>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BACKGROUND_COLOR)
            .verticalScroll(rememberScrollState())
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(horizontal = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = {
                    navController.popBackStack(route = "unit_screen/$unit", inclusive = false)
                },
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Back",
                    tint = Color.Black,
                    modifier = Modifier.size(48.dp),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "שינון מילים",
                fontSize = 32.sp,
                fontFamily = RUBIK_FONT,
                color = Color.White,
                textAlign = TextAlign.End,
                modifier = Modifier.padding(end = 12.dp),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        words.forEach { wordEntry ->
            key(wordEntry.word) {
                Column {
                    val isExpanded = expandedMap[wordEntry.word] ?: false

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .background(Color(0xFF2C2733))
                            .clickable {
                                expandedMap[wordEntry.word] = !isExpanded
                            }
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                    ) {
                        Text(
                            text = wordEntry.word,
                            fontSize = 24.sp,
                            fontFamily = RUBIK_FONT,
                            color = Color.White,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        AnimatedVisibility(visible = isExpanded) {
                            Text(
                                text = wordEntry.meaning,
                                fontSize = 18.sp,
                                fontFamily = RUBIK_FONT,
                                color = Color.LightGray,
                                textAlign = TextAlign.End,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Button(
            onClick = {
                Log.d("NAVIGATION", "NAVIGATION TO QUIZ TRIGGERED!")

                // Will Always be the first quiz after the memorization screen, Also there will be 0 mistakes.
                navController.navigate("quiz/$unit/$practice/true/0")
            },
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
                text = "!שיננתי, לתרגול",
                fontSize = 28.sp,
                fontFamily = RUBIK_FONT,
                color = Color.White,
            )
        }
    }
}
