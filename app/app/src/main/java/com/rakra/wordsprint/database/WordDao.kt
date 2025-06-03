package com.rakra.wordsprint.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Query("SELECT * FROM words")
    fun getAllWords(): Flow<List<WordEntry>>

    @Query("SELECT COUNT(*) FROM words WHERE unit = :unit AND status = :unknownStatus")
    fun countUnknownWordsInUnit(unit: Int, unknownStatus: String = "UNKNOWN"): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<WordEntry>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: WordEntry)

    @Update
    suspend fun updateWord(word: WordEntry)

    @Delete
    suspend fun deleteWord(word: WordEntry)
}