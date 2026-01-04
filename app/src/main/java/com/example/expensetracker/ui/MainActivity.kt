package com.example.expensetracker.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.expensetracker.R
import com.example.expensetracker.data.RecordType
import com.example.expensetracker.data.TransactionWithCategory
import com.example.expensetracker.databinding.ActivityMainBinding
import com.example.expensetracker.databinding.DialogCategoryBinding
import com.example.expensetracker.databinding.DialogTransactionBinding
import com.example.expensetracker.viewmodel.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val vm: MainViewModel by viewModels()

    private val moneyFmt = NumberFormat.getCurrencyInstance(Locale.CHINA)
    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val adapter = TransactionAdapter(
            onClick = { showTransactionDialog(it) },
            onDelete = { item ->
                vm.deleteTransaction(item.transaction)
                Snackbar.make(binding.root, "已删除", Snackbar.LENGTH_SHORT).show()
            }
        )
        binding.recyclerView.adapter = adapter

        binding.fabAdd.setOnClickListener { showTransactionDialog(null) }

        vm.transactions.observe(this) { list ->
            adapter.submitList(list)
            binding.tvEmpty.visibility = if (list.isNullOrEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            updateSummary(list ?: emptyList())
        }
    }

    private fun updateSummary(list: List<TransactionWithCategory>) {
        var income = 0.0
        var expense = 0.0
        list.forEach {
            when (it.transaction.type) {
                RecordType.INCOME -> income += it.transaction.amount
                RecordType.EXPENSE -> expense += it.transaction.amount
            }
        }
        val balance = income - expense
        binding.tvBalance.text = "${getString(R.string.summary_balance)}：${moneyFmt.format(balance)}"
        binding.tvIncome.text = "${getString(R.string.summary_income)}：${moneyFmt.format(income)}"
        binding.tvExpense.text = "${getString(R.string.summary_expense)}：${moneyFmt.format(expense)}"
    }

    private fun showTransactionDialog(editing: TransactionWithCategory?) {
        val title = if (editing == null) getString(R.string.add_record) else getString(R.string.edit_record)

        val b = DialogTransactionBinding.inflate(LayoutInflater.from(this))

        // default type & date
        b.toggleType.check(b.btnExpense.id)
        var selectedDateMillis = startOfTodayMillis()
        b.etDate.setText(dateFmt.format(LocalDate.now()))

        // categories snapshot (will still work; if you add new category, next time it will appear)
        val categories = vm.categories.value.orEmpty()
        val names = categories.map { it.name }.distinct()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, names)
        b.actCategory.setAdapter(adapter)

        fun findCategoryIdByName(name: String): Long? =
            vm.categories.value.orEmpty().firstOrNull { it.name.equals(name.trim(), ignoreCase = true) }?.id

        // Fill for edit
        val editId = editing?.transaction?.id
        if (editing != null) {
            val t = editing.transaction
            if (t.type == RecordType.EXPENSE) b.toggleType.check(b.btnExpense.id)
            else b.toggleType.check(b.btnIncome.id)

            b.etAmount.setText(t.amount.toString())
            b.actCategory.setText(editing.category?.name ?: "", false)
            b.etNote.setText(t.note)
            val date = LocalDate.ofInstant(Instant.ofEpochMilli(t.dateMillis), ZoneId.systemDefault())
            b.etDate.setText(dateFmt.format(date))
            selectedDateMillis = t.dateMillis
        }

        // Add category: end icon click (TextInputLayout is the grandparent of EditText)
        ((b.actCategory.parent.parent) as? TextInputLayout)?.setEndIconOnClickListener {
            showAddCategoryDialog { newName ->
                b.actCategory.setText(newName, false)
            }
        }

        // Date picker
        val openDatePicker = {
            val current = LocalDate.ofInstant(Instant.ofEpochMilli(selectedDateMillis), ZoneId.systemDefault())
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val d = LocalDate.of(year, month + 1, dayOfMonth)
                    selectedDateMillis = d.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    b.etDate.setText(dateFmt.format(d))
                },
                current.year,
                current.monthValue - 1,
                current.dayOfMonth
            ).show()
        }
        ((b.etDate.parent.parent) as? TextInputLayout)?.setEndIconOnClickListener { openDatePicker() }
        b.etDate.setOnClickListener { openDatePicker() }

        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setView(b.root)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save) { _, _ ->
                val amountStr = b.etAmount.text?.toString()?.trim().orEmpty()
                val categoryName = b.actCategory.text?.toString()?.trim().orEmpty()
                val note = b.etNote.text?.toString().orEmpty()

                val amount = amountStr.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    Snackbar.make(binding.root, "请输入有效金额（>0）", Snackbar.LENGTH_LONG).show()
                    return@setPositiveButton
                }
                if (categoryName.isEmpty()) {
                    Snackbar.make(binding.root, "请选择或输入分类", Snackbar.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                val type = if (b.toggleType.checkedButtonId == b.btnIncome.id) RecordType.INCOME else RecordType.EXPENSE

                val existingCategoryId = findCategoryIdByName(categoryName)
                if (existingCategoryId != null) {
                    vm.upsertTransaction(
                        id = editId,
                        amount = amount,
                        type = type,
                        categoryId = existingCategoryId,
                        note = note,
                        dateMillis = selectedDateMillis
                    )
                } else {
                    vm.addCategory(categoryName) { newId ->
                        vm.upsertTransaction(
                            id = editId,
                            amount = amount,
                            type = type,
                            categoryId = newId,
                            note = note,
                            dateMillis = selectedDateMillis
                        )
                    }
                }
            }
            .show()
    }

    private fun showAddCategoryDialog(onAdded: (String) -> Unit) {
        val b = DialogCategoryBinding.inflate(LayoutInflater.from(this))
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.add_category))
            .setView(b.root)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save) { _, _ ->
                val name = b.etCategoryName.text?.toString()?.trim().orEmpty()
                if (name.isEmpty()) {
                    Snackbar.make(binding.root, "分类名称不能为空", Snackbar.LENGTH_LONG).show()
                    return@setPositiveButton
                }
                vm.addCategory(name) {
                    runOnUiThread { onAdded(name) }
                }
            }
            .show()
    }

    private fun startOfTodayMillis(): Long =
        LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}
