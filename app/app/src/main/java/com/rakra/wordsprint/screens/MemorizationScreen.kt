package com.rakra.wordsprint.screens

import WordViewModel
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.rakra.wordsprint.data.wordsDatabase.Status
import com.rakra.wordsprint.ui.animations.ClickHint
import com.rakra.wordsprint.data.wordsDatabase.WordEntry
import com.rakra.wordsprint.ui.animations.SwipeRightHint
import com.rakra.wordsprint.ui.theme.BACKGROUND_COLOR
import com.rakra.wordsprint.ui.theme.BACK_BUTTON_COLOR
import com.rakra.wordsprint.ui.theme.BUTTON_CONTENT_COLOR
import com.rakra.wordsprint.ui.theme.BUTTON_OUTLINE_COLOR
import com.rakra.wordsprint.ui.theme.RUBIK_FONT
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val EXIT_ANIMATION_DURATION_MS = 300

@Composable
fun MemorizationScreen(
    navController: NavHostController,
    unit: Int,
    practice: Int,
    initialWords: List<WordEntry>,
    databaseViewModel: WordViewModel,
    onContinue: (words: List<WordEntry>, knownWords: List<WordEntry>) -> Unit,
) {
    val expandedMap = remember { mutableStateMapOf<String, Boolean>() }
    var showHints by remember { mutableStateOf(true) }
    var visibleWords by remember { mutableStateOf<List<WordEntry>>(emptyList()) }
    val activeSwipeWord = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        visibleWords = initialWords
    }

    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val visibilityMap = remember(visibleWords) {
        mutableStateMapOf<String, Boolean>().apply {
            visibleWords.forEach {
                this[it.word] = this[it.word] ?: true
            }
        }
    }

    val dismissedWords = remember { mutableStateSetOf<Int>() }
    val dismissedWordEntries = remember { mutableStateListOf<WordEntry>() }

    Box(modifier = Modifier.fillMaxSize()) {
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
                        tint = BACK_BUTTON_COLOR,
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

            visibleWords.forEach { wordEntry ->
                key(wordEntry.id) {
                    val dismissState = remember(wordEntry.word) {
                        SwipeToDismissBoxState(
                            initialValue = SwipeToDismissBoxValue.Settled,
                            density = density,
                            confirmValueChange = { target ->
                                if (activeSwipeWord.value != null && activeSwipeWord.value != wordEntry.word) return@SwipeToDismissBoxState false

                                when (target) {
                                    SwipeToDismissBoxValue.EndToStart -> false
                                    SwipeToDismissBoxValue.StartToEnd -> {
                                        activeSwipeWord.value = wordEntry.word
                                        showHints = false

                                        if (visibilityMap[wordEntry.word] == true) {
                                            visibilityMap[wordEntry.word] = false
                                            scope.launch {
                                                delay(EXIT_ANIMATION_DURATION_MS.toLong())

                                                dismissedWords.add(wordEntry.id)
                                                dismissedWordEntries.add(wordEntry)
                                                val index = visibleWords.indexOf(wordEntry)

                                                val filler = databaseViewModel
                                                    .getWordsByStatus(unit, Status.NOT_SELECTED)
                                                    .first { it.isNotEmpty() }
                                                    .filterNot {
                                                        dismissedWords.contains(it.id) || visibleWords.contains(it)
                                                    }
                                                    .shuffled()
                                                    .first()

                                                Log.d("DEBUG", "ADDED WORD ENTRY: $filler")
                                                visibilityMap.remove(wordEntry.word)

                                                visibleWords = visibleWords.toMutableList().apply {
                                                    set(index, filler)
                                                }

                                                visibilityMap[filler.word] = true
                                                activeSwipeWord.value = null
                                            }
                                        } else {
                                            activeSwipeWord.value = null
                                        }
                                        true
                                    }

                                    else -> true
                                }
                            },
                            positionalThreshold = { it * 0.7f },
                        )
                    }

                    val target = dismissState.targetValue

                    AnimatedVisibility(
                        visible = visibilityMap[wordEntry.word] ?: false,
                        exit = shrinkVertically(
                            animationSpec = tween(durationMillis = EXIT_ANIMATION_DURATION_MS),
                            shrinkTowards = Alignment.Top
                        ) + fadeOut(
                            animationSpec = tween(EXIT_ANIMATION_DURATION_MS),
                        ),
                        modifier = Modifier
                            .animateContentSize(animationSpec = tween(durationMillis = EXIT_ANIMATION_DURATION_MS))
                            .zIndex(if (activeSwipeWord.value == wordEntry.word) 1f else 0f),
                    ) {
                        Column {
                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = activeSwipeWord.value == null || activeSwipeWord.value == wordEntry.word,
                                enableDismissFromEndToStart = false,
                                backgroundContent = {
                                    val color = when (target) {
                                        SwipeToDismissBoxValue.StartToEnd -> Color(0xFF81C784)
                                        else -> Color.Transparent
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(color),
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
                                                showHints = false
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
                                },
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            Button(
                onClick = {
                    onContinue(visibleWords, dismissedWordEntries)
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

        if (showHints) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 8.dp, top = 108.dp),
                verticalArrangement = Arrangement.spacedBy((-32).dp),
                horizontalAlignment = Alignment.Start
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                ) {
                    ClickHint()
                }

                Spacer(modifier = Modifier.height(56.dp))

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .offset(y = (-12).dp)
                ) {
                    SwipeRightHint()
                }
            }
        }
    }
}
