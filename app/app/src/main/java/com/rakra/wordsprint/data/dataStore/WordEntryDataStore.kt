package com.rakra.wordsprint.data.dataStore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rakra.wordsprint.data.wordsDatabase.WordEntry
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

val Context.dataStore by preferencesDataStore("word_store")

suspend fun saveWordList(context: Context, words: List<WordEntry>) {
    val json = Json.encodeToString(words)
    val key = stringPreferencesKey("word_list")

    context.dataStore.edit { prefs ->
        prefs[key] = json
    }
}

suspend fun loadWordList(context: Context): List<WordEntry> {
    val key = stringPreferencesKey("word_list")
    val prefs = context.dataStore.data.first()
    val json = prefs[key] ?: return emptyList()

    return Json.decodeFromString(json)
}
