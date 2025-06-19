package com.rakra.wordsprint.ui.sfx

import android.content.Context
import android.media.SoundPool
import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember

@Composable
fun rememberSoundPool(context: Context, @RawRes resId: Int): Pair<SoundPool, Int> {
    val soundPool = remember { SoundPool.Builder().setMaxStreams(1).build() }
    val soundId = remember { soundPool.load(context, resId, 1) }

    DisposableEffect(Unit) {
        onDispose { soundPool.release() }
    }

    return Pair(soundPool, soundId)
}

fun playLoadedSound(soundPool: SoundPool, soundId: Int) {
    soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
}