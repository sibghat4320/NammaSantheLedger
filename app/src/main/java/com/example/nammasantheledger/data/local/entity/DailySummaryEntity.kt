package com.example.nammasantheledger.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for pre-computed daily summaries.
 * Avoids expensive aggregate queries on the transaction table.
 *
 * Design decisions:
 * - date stored as "yyyy-MM-dd" string for easy grouping
 * - Indexed on date for fast lookups
 * - Computed and cached at end of day or on demand
 */
@Entity(
    tableName = "daily_summaries",
    indices = [Index(value = ["date"], unique = true)]
)
data class DailySummaryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "date")
    val date: String, // "yyyy-MM-dd"

    @ColumnInfo(name = "total_credit")
    val totalCredit: Double = 0.0,

    @ColumnInfo(name = "total_payment")
    val totalPayment: Double = 0.0,

    @ColumnInfo(name = "transaction_count")
    val transactionCount: Int = 0,

    @ColumnInfo(name = "unique_customers")
    val uniqueCustomers: Int = 0,

    @ColumnInfo(name = "computed_at")
    val computedAt: Long = System.currentTimeMillis()
)
