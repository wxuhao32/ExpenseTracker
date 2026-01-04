package com.example.expensetracker.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromRecordType(value: RecordType): String = value.name

    @TypeConverter
    fun toRecordType(value: String): RecordType = RecordType.valueOf(value)
}
