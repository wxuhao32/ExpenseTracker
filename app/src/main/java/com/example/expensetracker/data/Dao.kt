package com.example.expensetracker.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): LiveData<List<Category>>

    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE ASC")
    suspend fun getAll(): List<Category>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(category: Category): Long
}

@Dao
interface TransactionDao {
    @Transaction
    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC, id DESC")
    fun observeAll(): LiveData<List<TransactionWithCategory>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TransactionWithCategory?

    @Insert
    suspend fun insert(entity: TransactionEntity): Long

    @Update
    suspend fun update(entity: TransactionEntity)

    @Delete
    suspend fun delete(entity: TransactionEntity)
}
