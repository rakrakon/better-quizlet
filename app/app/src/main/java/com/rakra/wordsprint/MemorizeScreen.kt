package com.rakra.wordsprint

import WordViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rakra.wordsprint.database.AppDatabase
import com.rakra.wordsprint.database.Status
import com.rakra.wordsprint.database.WordEntry
import com.rakra.wordsprint.database.WordViewModelFactory
import com.rakra.wordsprint.ui.theme.BACKGROUND_COLOR
import com.rakra.wordsprint.ui.theme.RUBIK_FONT
import com.rakra.wordsprint.ui.theme.WordSprintTheme
import kotlinx.coroutines.delay

@Preview(showBackground = true)
@Composable
fun MemorizationPreview() {

    WordSprintTheme {
        val context = LocalContext.current
        val db = remember { AppDatabase.getDatabase(context) }
        val wordDao = remember { db.wordDao() }
        val viewModel: WordViewModel = viewModel(factory = WordViewModelFactory(wordDao))

        val wordsState by viewModel.wordsState.collectAsState()

        MemorizationScreen(wordsState,1, viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemorizationScreen(wordsWithMeanings: List<WordEntry>, unit: Int, viewModel: WordViewModel) {
    val hasEnoughUnknown by viewModel.hasAtLeastNUnknownWords(unit).collectAsState()

    val density = LocalDensity.current
    val visibleWords = remember { wordsWithMeanings.toMutableStateList() }

    val visibilityMap = remember {
        mutableStateMapOf<String, Boolean>().apply {
            wordsWithMeanings.forEach { this[it.word] = true }
        }
    }

    val expandedMap = remember { mutableStateMapOf<String, Boolean>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BACKGROUND_COLOR)
            .verticalScroll(rememberScrollState())
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(horizontal = 12.dp)
    ) {
        Text(
            text = "שינון מילים",
            fontSize = 32.sp,
            fontFamily = RUBIK_FONT,
            color = Color.White,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        visibleWords.forEach { wordEntry ->
            val dismissState = remember(wordEntry.word) {
                SwipeToDismissBoxState(
                    initialValue = SwipeToDismissBoxValue.Settled,
                    density = density,
                    confirmValueChange = { true },
                    positionalThreshold = { it * 0.5f }
                )
            }

            val target = dismissState.targetValue
            val swiped = target != SwipeToDismissBoxValue.Settled

            if (swiped && visibilityMap[wordEntry.word] == true) {
                LaunchedEffect(wordEntry.word) {
                    visibilityMap[wordEntry.word] = false
                    delay(300)
                    visibleWords.remove(wordEntry)
                    visibilityMap.remove(wordEntry.word)

                    when (target) {
                        SwipeToDismissBoxValue.StartToEnd -> {
                            viewModel.updateWord(
                                wordEntry.apply {
                                    status = Status.KNOWN
                                }
                            )
                        }

                        SwipeToDismissBoxValue.EndToStart -> {
                            viewModel.updateWord(
                                wordEntry.apply {
                                    status = Status.UNKNOWN
                                }
                            )
                        }

                        else -> {}
                    }
                }
            }

            AnimatedVisibility(
                visible = visibilityMap[wordEntry.word] ?: true,
                exit = shrinkVertically(animationSpec = tween(durationMillis = 300)) +
                        fadeOut(animationSpec = tween(300)),
                modifier = Modifier.animateContentSize()
            ) {
                Column {
                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            val color = when (target) {
                                SwipeToDismissBoxValue.StartToEnd -> Color(0xFF81C784) // Green
                                SwipeToDismissBoxValue.EndToStart -> Color(0xFFE57373) // Red
                                else -> Color.Transparent
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color)
                            )
                        },
                        content = {
                            val isExpanded = expandedMap[wordEntry.word] ?: false

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(Color(0xFF2C2733))
                                    .clickable {
                                        expandedMap[wordEntry.word] = !isExpanded
                                    }
                                    .padding(vertical = 8.dp, horizontal = 12.dp)
                            ) {
                                Text(
                                    text = wordEntry.word,
                                    fontSize = 24.sp,
                                    fontFamily = RUBIK_FONT,
                                    color = Color.White,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
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
                                            .padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
