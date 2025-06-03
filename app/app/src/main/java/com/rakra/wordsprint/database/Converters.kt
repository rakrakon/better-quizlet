package com.rakra.wordsprint.database

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromStatus(value: Status): String {
        return value.name
    }

    @TypeConverter
    fun toStatus(value: String): Status {
        return Status.valueOf(value)
    }
}