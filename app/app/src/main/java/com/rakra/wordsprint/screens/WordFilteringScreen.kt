package com.rakra.wordsprint.screens

import WordViewModel
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.rakra.wordsprint.FIRST_QUIZ_SIZE
import com.rakra.wordsprint.ui.animations.ClickHint
import com.rakra.wordsprint.ui.animations.SwipeLeftHint
import com.rakra.wordsprint.ui.animations.SwipeRightHint
import com.rakra.wordsprint.data.wordsDatabase.Status
import com.rakra.wordsprint.data.wordsDatabase.WordEntry
import com.rakra.wordsprint.ui.theme.BACKGROUND_COLOR
import com.rakra.wordsprint.ui.theme.BACK_BUTTON_COLOR
import com.rakra.wordsprint.ui.theme.PROGRESS_BAR_COLOR
import com.rakra.wordsprint.ui.theme.RUBIK_FONT
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

const val EXIT_ANIMATION_DURATION_MS = 300
const val INITIAL_WORDS_COUNT = 50

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordFilteringScreen(
    navController: NavHostController,
    unit: Int,
    practice: Int,
    databaseViewModel: WordViewModel,
) {
    var hasNavigated by remember { mutableStateOf(false) }
    val unknownWords = remember { mutableStateListOf<WordEntry>() }

    LaunchedEffect(unknownWords.size) {
        if (hasNavigated) return@LaunchedEffect

        Log.d("DEBUG", "UNKNOWN WORDS SIZE ${unknownWords.size}")

        // Handle overflow of more than 10 words
        if (unknownWords.size > FIRST_QUIZ_SIZE) {
            repeat(unknownWords.size - FIRST_QUIZ_SIZE) {
                unknownWords.removeAt(unknownWords.lastIndex)
            }
        }

        if (unknownWords.size == FIRST_QUIZ_SIZE) {
            hasNavigated = true
            databaseViewModel.insertWords(unknownWords)
            Log.d("NAVIGATION", "NAVIGATION TO MEMORIZATION TRIGGERED!")
            navController.navigate("memorization/$unit/$practice")
        }
    }

    val density = LocalDensity.current

    var visibleWords by remember { mutableStateOf<List<WordEntry>>(emptyList()) }

    LaunchedEffect(Unit) {
        visibleWords =
            databaseViewModel.getWordsByStatus(unit, Status.NOT_SELECTED)
                .first { it.isNotEmpty() }
    }

    val visibilityMap = remember(visibleWords) {
        mutableStateMapOf<String, Boolean>().apply {
            visibleWords.forEach {
                this[it.word] = this[it.word] ?: true
            }
        }
    }

    val expandedMap = remember { mutableStateMapOf<String, Boolean>() }
    var showHints by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    val unknownWordsSize by remember {
        derivedStateOf { unknownWords.size }
    }

    val dismissedWords = remember { mutableStateSetOf<String>() }

    var isRefreshing by remember { mutableStateOf(false) }
    val state = rememberPullToRefreshState()
    val onRefresh: () -> Unit = {
        isRefreshing = true
        scope.launch {
            Log.d("REFRESH", "WORDS REFRESH TRIGGERED")

            val fillerWords = databaseViewModel.getWordsByStatus(unit, Status.NOT_SELECTED)
                .first { it.isNotEmpty() }

            val visibleCount = visibilityMap.values.count { it }
            val neededCount = INITIAL_WORDS_COUNT - visibleCount
            val wordEntriesToAdd = fillerWords
                .filterNot { dismissedWords.contains(it.word) }
                .take(neededCount)

            wordEntriesToAdd.forEach {
                Log.d("DEBUG", "ADDED WORD ENTRY: $it")
                visibilityMap[it.word] = true
            }
            visibleWords = visibleWords + wordEntriesToAdd
            visibleWords = visibleWords.filterNot { dismissedWords.contains(it.word) }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullToRefresh(
                state = state,
                isRefreshing = isRefreshing,
                onRefresh = onRefresh
            )
    ) {
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
                Spacer(modifier = Modifier.size(8.dp))

                IconButton(
                    onClick = { navController.popBackStack() },
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
                    text = "פילטור מילים",
                    fontSize = 32.sp,
                    fontFamily = RUBIK_FONT,
                    color = Color.White,
                    textAlign = TextAlign.End,
                    modifier = Modifier.padding(end = 12.dp),
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { (unknownWordsSize / FIRST_QUIZ_SIZE.toFloat()).coerceAtMost(1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(10.dp)),
                color = PROGRESS_BAR_COLOR,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            )

            Spacer(modifier = Modifier.height(12.dp))

            visibleWords.forEach { wordEntry ->
                val dismissState = remember(wordEntry.word) {
                    SwipeToDismissBoxState(
                        initialValue = SwipeToDismissBoxValue.Settled,
                        density = density,
                        confirmValueChange = { target ->
                            showHints = false

                            if (visibilityMap[wordEntry.word] == true) {
                                visibilityMap[wordEntry.word] = false
                                dismissedWords.add(wordEntry.word)

                                scope.launch {
                                    delay(EXIT_ANIMATION_DURATION_MS.toLong())
                                    visibilityMap.remove(wordEntry.word)

                                    when (target) {
                                        SwipeToDismissBoxValue.StartToEnd -> {
                                            databaseViewModel.updateWord(wordEntry.copy(status = Status.KNOWN))
                                        }

                                        SwipeToDismissBoxValue.EndToStart -> {
                                            Log.d(
                                                "FILTERING",
                                                "ADD ${wordEntry.word} TO UNKNOWN"
                                            )
                                            unknownWords.add(wordEntry.copy(status = Status.UNKNOWN))
                                        }

                                        else -> {}
                                    }
                                }
                            }
                            true
                        },
                        positionalThreshold = { it * 0.7f },
                    )
                }

                val target = dismissState.targetValue

                AnimatedVisibility(
                    visible = visibilityMap[wordEntry.word] ?: false,
                    exit = shrinkVertically(animationSpec = tween(durationMillis = EXIT_ANIMATION_DURATION_MS)) + fadeOut(
                        animationSpec = tween(EXIT_ANIMATION_DURATION_MS),
                    ),
                    modifier = Modifier.animateContentSize(),
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
                    SwipeLeftHint()
                }
                Box(modifier = Modifier.size(120.dp)) {
                    SwipeRightHint()
                }
            }
        }
    }
}
