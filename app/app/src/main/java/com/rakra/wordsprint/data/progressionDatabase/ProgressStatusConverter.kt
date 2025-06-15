package com.rakra.wordsprint.data.progressionDatabase

import androidx.room.TypeConverter

class ProgressStatusConverter {

    @TypeConverter
    fun fromProgressStatus(status: ProgressStatus): String {
        return status.name
    }

    @TypeConverter
    fun toProgressStatus(value: String): ProgressStatus {
        return ProgressStatus.valueOf(value)
    }
}
