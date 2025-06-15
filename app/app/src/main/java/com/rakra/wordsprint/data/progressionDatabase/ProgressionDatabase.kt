package com.rakra.wordsprint.data.progressionDatabase

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rakra.wordsprint.NUMBER_OF_UNITS
import com.rakra.wordsprint.data.math.getRoundedUpTens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.BufferedReader

@Database(
    entities = [ProgressionEntry::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(ProgressStatusConverter::class)
abstract class ProgressionDatabase : RoomDatabase() {

    abstract fun progressionDao(): ProgressionDao

    companion object {
        @Volatile
        private var INSTANCE: ProgressionDatabase? = null

        fun getDatabase(context: Context): ProgressionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ProgressionDatabase::class.java,
                    "progression_database",
                )
                    .addCallback(initCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private fun initCallback(context: Context) = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                CoroutineScope(Dispatchers.IO).launch {
                    val initialEntries = mutableListOf<ProgressionEntry>()

                    for (unitNumber in 1..NUMBER_OF_UNITS) {
                        val path = "fallback_words/unit_$unitNumber.json"
                        try {
                            context.assets.open(path).use { input ->
                                val text = input.bufferedReader()
                                    .use(BufferedReader::readText)
                                val rawMap: Map<String, String> = Json.decodeFromString(text)
                                val count = rawMap.size
                                val numberOfPracticesInUnit = getRoundedUpTens(count)
                                for (practiceNumber in 1..numberOfPracticesInUnit) {
                                    Log.d("PROGRESSION_DB", "ADD ENTRY FOR PRACTICE NUM $practiceNumber IN UNIT $unitNumber")
                                    initialEntries.add(
                                        ProgressionEntry(
                                            practiceNum = practiceNumber,
                                            unit = unitNumber,
                                        ),
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    getDatabase(context).progressionDao().insertAll(initialEntries)
                }
            }
        }
    }
}
