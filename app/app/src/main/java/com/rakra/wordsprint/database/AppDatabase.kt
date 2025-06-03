package com.rakra.wordsprint.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
                    "words_database"
                )
                    .addCallback(
                        initCallback(context)
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
                    val json = Json { ignoreUnknownKeys = true }

                    for (i in 1..10) {
                        val path = "fallback_words/unit_$i.json"
                        try {
                            context.assets.open(path).use { input ->
                                val text = input.bufferedReader()
                                    .use(BufferedReader::readText)
                                val unitWords =
                                    json.decodeFromString<List<WordEntry>>(text)
                                wordList += unitWords
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    getDatabase(context).wordDao().insertAll(wordList)
                }
            }
        }
    }
}