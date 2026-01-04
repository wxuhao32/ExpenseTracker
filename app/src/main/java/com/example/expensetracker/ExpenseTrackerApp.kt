package com.example.expensetracker

import android.app.Application
import com.example.expensetracker.data.AppDatabase
import com.example.expensetracker.data.Repository

class ExpenseTrackerApp : Application() {

    // Simple service locator
    lateinit var repository: Repository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getInstance(this)
        repository = Repository(db.transactionDao(), db.categoryDao())
    }
}
