package com.example.expensetracker.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.data.RecordType
import com.example.expensetracker.data.TransactionWithCategory
import com.example.expensetracker.databinding.ItemTransactionBinding
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class TransactionAdapter(
    private val onClick: (TransactionWithCategory) -> Unit,
    private val onDelete: (TransactionWithCategory) -> Unit
) : ListAdapter<TransactionWithCategory, TransactionAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<TransactionWithCategory>() {
            override fun areItemsTheSame(oldItem: TransactionWithCategory, newItem: TransactionWithCategory): Boolean =
                oldItem.transaction.id == newItem.transaction.id

            override fun areContentsTheSame(oldItem: TransactionWithCategory, newItem: TransactionWithCategory): Boolean =
                oldItem == newItem
        }

        private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        private val moneyFmt = NumberFormat.getCurrencyInstance(Locale.CHINA)
    }

    inner class VH(val b: ItemTransactionBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        val t = item.transaction
        val categoryName = item.category?.name ?: "未分类"
        holder.b.tvCategory.text = categoryName

        val note = t.note.trim()
        holder.b.tvNote.text = if (note.isEmpty()) "（无备注）" else note

        val date = LocalDate.ofInstant(Instant.ofEpochMilli(t.dateMillis), ZoneId.systemDefault())
        holder.b.tvDate.text = dateFmt.format(date)

        val amountText = moneyFmt.format(t.amount)
        holder.b.tvAmount.text = if (t.type == RecordType.EXPENSE) "- $amountText" else "+ $amountText"

        holder.b.root.setOnClickListener { onClick(item) }
        holder.b.btnDelete.setOnClickListener { onDelete(item) }
    }
}
