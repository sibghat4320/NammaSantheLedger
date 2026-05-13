package com.example.nammasantheledger.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a financial transaction in the ledger.
 *
 * Design decisions:
 * - Foreign key to customers with CASCADE delete
 * - Type field distinguishes CREDIT (goods given) vs PAYMENT (money received)
 * - Amount is always positive; sign is determined by type
 * - Indexed on customerId + timestamp for fast ledger queries
 * - notes field for transaction context (item names, etc.)
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customer_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["customer_id"]),
        Index(value = ["timestamp"]),
        Index(value = ["customer_id", "timestamp"]),
        Index(value = ["type"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "customer_id")
    val customerId: Long,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "type")
    val type: String, // "CREDIT" or "PAYMENT"

    @ColumnInfo(name = "notes")
    val notes: String = "",

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
