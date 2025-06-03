package com.rakra.wordsprint.database

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Status {
    NOT_SELECTED,
    KNOWN,
    UNKNOWN
}

@Entity(tableName = "words")
data class WordEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val word: String,
    val meaning: String,
    val unit: Int,
    val status: Status = Status.NOT_SELECTED
)
