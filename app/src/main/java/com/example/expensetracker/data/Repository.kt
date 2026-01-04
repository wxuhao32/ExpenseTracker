package com.example.expensetracker.data

import androidx.lifecycle.LiveData

class Repository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao
) {
    fun observeTransactions(): LiveData<List<TransactionWithCategory>> = transactionDao.observeAll()
    fun observeCategories(): LiveData<List<Category>> = categoryDao.observeAll()

    suspend fun addCategory(name: String): Long {
        val clean = name.trim()
        require(clean.isNotEmpty()) { "Category name is empty" }
        return categoryDao.insert(Category(name = clean, isCustom = true))
    }

    suspend fun insertTransaction(entity: TransactionEntity): Long = transactionDao.insert(entity)
    suspend fun updateTransaction(entity: TransactionEntity) = transactionDao.update(entity)
    suspend fun deleteTransaction(entity: TransactionEntity) = transactionDao.delete(entity)
    suspend fun getTransaction(id: Long): TransactionWithCategory? = transactionDao.getById(id)
    suspend fun getAllCategories(): List<Category> = categoryDao.getAll()
}
