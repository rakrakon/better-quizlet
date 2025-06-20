package com.rakra.wordsprint.data.wordsDatabase

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
enum class Status {
    NOT_SELECTED,
    RECENT,
    KNOWN,
    UNKNOWN,
}

@Serializable
@Entity(tableName = "words")
data class WordEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var word: String,
    var meaning: String,
    var unit: Int,
    var status: Status = Status.NOT_SELECTED,
)
