package com.rakra.wordsprint.data.progressionDatabase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ProgressionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<ProgressionEntry>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ProgressionEntry)

    @Update
    suspend fun update(entry: ProgressionEntry)

    @Delete
    suspend fun delete(entry: ProgressionEntry)

    @Query("SELECT * FROM progression WHERE unit = :unit AND practiceNum = :practiceNum")
    suspend fun getByUnitAndPractice(unit: Int, practiceNum: Int): ProgressionEntry?

    @Query("SELECT * FROM progression WHERE unit = :unit ORDER BY practiceNum ASC")
    suspend fun getAllPracticesOfUnit(unit: Int): List<ProgressionEntry>

    @Query("SELECT * FROM progression")
    suspend fun getAll(): List<ProgressionEntry>
}
