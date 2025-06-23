package com.rakra.wordsprint.data.progressionDatabase

import androidx.room.TypeConverter

class ProgressConverters {

    @TypeConverter
    fun fromProgressStatus(status: ProgressStatus): String {
        return status.name
    }

    @TypeConverter
    fun toProgressStatus(value: String): ProgressStatus {
        return ProgressStatus.valueOf(value)
    }

    @TypeConverter
    fun fromIntList(list: List<Int>): String = list.joinToString(",")

    @TypeConverter
    fun toIntList(data: String): List<Int> =
        if (data.isEmpty()) emptyList() else data.split(",").map { it.toInt() }
}
