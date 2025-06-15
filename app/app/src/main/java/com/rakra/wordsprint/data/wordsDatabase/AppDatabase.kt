package com.rakra.wordsprint.data.wordsDatabase

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rakra.wordsprint.NUMBER_OF_UNITS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.BufferedReader

@Database(entities = [WordEntry::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "words_database",
                )
                    .addCallback(
                        initCallback(context),
                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private fun initCallback(context: Context) = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                CoroutineScope(Dispatchers.IO).launch {
                    val wordList = mutableListOf<WordEntry>()

                    for (i in 1..NUMBER_OF_UNITS) {
                        val path = "fallback_words/unit_$i.json"
                        try {
                            context.assets.open(path).use { input ->
                                val text = input.bufferedReader()
                                    .use(BufferedReader::readText)
                                val rawMap: Map<String, String> = Json.decodeFromString(text)
                                val words: List<WordEntry> = rawMap.map { (word, meaning) ->
                                    WordEntry(word = word, meaning = meaning, unit = i)
                                }
                                wordList += words
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    getDatabase(context).wordDao().insertAll(wordList)
                    Log.d("DB", "Inserted ${wordList.size}")
                }
            }
        }
    }
}
