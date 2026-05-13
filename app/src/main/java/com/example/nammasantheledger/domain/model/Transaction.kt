package com.example.nammasantheledger.domain.model

/**
 * Domain model representing a financial transaction.
 */
data class Transaction(
    val id: Long = 0,
    val customerId: Long,
    val customerName: String = "", // Populated via join or manual lookup
    val amount: Double,
    val type: TransactionType,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Type of financial transaction in the ledger.
 */
enum class TransactionType {
    /** Goods given on credit - increases outstanding balance */
    CREDIT,
    /** Payment received - decreases outstanding balance */
    PAYMENT
}
