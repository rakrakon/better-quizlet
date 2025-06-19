package com.rakra.wordsprint.ui.animations

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.rakra.wordsprint.R

private const val SWIPE_HINT_ANIMATION_ITERATION = 7

@Composable
fun ClickHint(
    modifier: Modifier = Modifier
) {
    HintAnimation(
        modifier = modifier,
        animationRes = R.raw.click
    )
}

@Composable
fun SwipeLeftHint(
    modifier: Modifier = Modifier
) {
    HintAnimation(
        modifier = modifier,
        animationRes = R.raw.swipe_left
    )
}

@Composable
fun SwipeRightHint(
    modifier: Modifier = Modifier
) {
    HintAnimation(
        modifier = modifier,
        animationRes = R.raw.swipe_right
    )
}

@Composable
private fun HintAnimation(
    modifier: Modifier = Modifier,
    animationRes: Int
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animationRes))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = SWIPE_HINT_ANIMATION_ITERATION
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
        )
    }
}