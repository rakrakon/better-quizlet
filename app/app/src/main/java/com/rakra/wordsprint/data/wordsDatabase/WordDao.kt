package com.rakra.wordsprint.data.wordsDatabase

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
    fun countUnknownWordsInUnit(unit: Int, unknownStatus: String): Flow<Int>

    @Query("SELECT * FROM words WHERE unit = :unit AND status = :status ORDER BY RANDOM() LIMIT 50")
    fun getAllWords(unit: Int, status: Status): Flow<List<WordEntry>>

    @Query("SELECT * FROM words WHERE status = :status ORDER BY RANDOM() LIMIT 20")
    fun getWordsUnitless(status: Status): Flow<List<WordEntry>>

    @Query("SELECT * FROM words WHERE unit = :unit ORDER BY RANDOM() LIMIT 75")
    fun getAllWordsInUnit(unit: Int): Flow<List<WordEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<WordEntry>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: WordEntry)

    @Update
    suspend fun updateWord(word: WordEntry)

    @Delete
    suspend fun deleteWord(word: WordEntry)
}
