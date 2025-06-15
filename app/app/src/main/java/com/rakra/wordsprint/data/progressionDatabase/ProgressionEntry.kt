package com.rakra.wordsprint.data.progressionDatabase

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
enum class ProgressStatus {
    NOT_STARTED,
    COMPLETED,
}

@Serializable
@Entity(tableName = "progression")
data class ProgressionEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var practiceNum: Int,
    var unit: Int,
    var completion: ProgressStatus = ProgressStatus.NOT_STARTED,
)
