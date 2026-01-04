package com.example.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isCustom: Boolean = true
)

enum class RecordType { EXPENSE, INCOME }

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: RecordType,
    val categoryId: Long,
    val note: String,
    val dateMillis: Long
)
