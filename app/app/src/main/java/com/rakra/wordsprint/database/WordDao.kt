package com.rakra.wordsprint.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface WordDao {
    @Query("SELECT * FROM words")
    suspend fun getAllWords(): List<WordEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<WordEntry>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: WordEntry)

    @Update
    suspend fun updateWord(word: WordEntry)

    @Delete
    suspend fun deleteWord(word: WordEntry)

    @Query("SELECT COUNT(*) FROM words WHERE unit = :unit AND status = :unknownStatus")
    suspend fun countUnknownWordsInUnit(unit: Int, unknownStatus: String = "UNKNOWN"): Int

    suspend fun hasAtLeastNUnknownWords(unit: Int, n: Int = 10): Boolean {
        val count = countUnknownWordsInUnit(unit)
        return count >= n
    }
}