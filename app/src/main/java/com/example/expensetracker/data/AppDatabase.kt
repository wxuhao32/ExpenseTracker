package com.example.expensetracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Category::class, TransactionEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: build(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun build(context: Context): AppDatabase {
            val defaults = listOf(
                "餐饮", "交通", "购物", "房租", "水电燃气", "娱乐", "医疗", "学习", "工资", "奖金", "其他"
            )
            return Room.databaseBuilder(context, AppDatabase::class.java, "expense_tracker.db")
                .addCallback(object : Callback() {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Prepopulate common categories (isCustom = 0)
                        defaults.forEach { name ->
                            db.execSQL("INSERT INTO categories (name, isCustom) VALUES (?, 0)", arrayOf(name))
                        }
                    }
                })
                .build()
        }
    }
}
