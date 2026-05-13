package com.example.nammasantheledger.domain.repository

import com.example.nammasantheledger.domain.model.DashboardSummary
import com.example.nammasantheledger.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Transaction operations.
 * Defined in the domain layer - implemented in data layer.
 */
interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getRecentTransactions(limit: Int = 20): Flow<List<Transaction>>
    fun getTransactionsForCustomer(customerId: Long): Flow<List<Transaction>>
    fun getCustomerBalance(customerId: Long): Flow<Double>
    fun getTotalOutstanding(): Flow<Double>
    fun getTodayCredit(): Flow<Double>
    fun getTodayPayment(): Flow<Double>
    suspend fun addTransaction(transaction: Transaction): Long
    suspend fun deleteTransaction(transactionId: Long)
    suspend fun getDashboardSummary(): DashboardSummary
    suspend fun undoLastTransaction(): Transaction?
}
