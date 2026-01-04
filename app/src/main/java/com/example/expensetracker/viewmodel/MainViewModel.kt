package com.example.expensetracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.ExpenseTrackerApp
import com.example.expensetracker.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val repo: Repository = (app as ExpenseTrackerApp).repository

    val transactions: LiveData<List<TransactionWithCategory>> = repo.observeTransactions()
    val categories: LiveData<List<Category>> = repo.observeCategories()

    fun addCategory(name: String, onDone: (Long) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = repo.addCategory(name)
            onDone(id)
        }
    }

    fun upsertTransaction(
        id: Long?,
        amount: Double,
        type: RecordType,
        categoryId: Long,
        note: String,
        dateMillis: Long
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = TransactionEntity(
                id = id ?: 0,
                amount = amount,
                type = type,
                categoryId = categoryId,
                note = note,
                dateMillis = dateMillis
            )
            if (id == null || id == 0L) repo.insertTransaction(entity) else repo.updateTransaction(entity)
        }
    }

    fun deleteTransaction(entity: TransactionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.deleteTransaction(entity)
        }
    }
}
