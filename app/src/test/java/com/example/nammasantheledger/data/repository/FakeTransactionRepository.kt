package com.example.nammasantheledger.data.repository

import com.example.nammasantheledger.domain.model.DashboardSummary
import com.example.nammasantheledger.domain.model.Transaction
import com.example.nammasantheledger.domain.model.TransactionType
import com.example.nammasantheledger.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Fake TransactionRepository for unit testing.
 */
class FakeTransactionRepository : TransactionRepository {

    private val transactions = MutableStateFlow<List<Transaction>>(emptyList())
    private var nextId = 1L
    private var lastInsertedId: Long? = null

    override fun getAllTransactions(): Flow<List<Transaction>> =
        transactions.map { it.sortedByDescending { t -> t.timestamp } }

    override fun getRecentTransactions(limit: Int): Flow<List<Transaction>> =
        transactions.map { it.sortedByDescending { t -> t.timestamp }.take(limit) }

    override fun getTransactionsForCustomer(customerId: Long): Flow<List<Transaction>> =
        transactions.map { list ->
            list.filter { it.customerId == customerId }
                .sortedByDescending { it.timestamp }
        }

    override fun getCustomerBalance(customerId: Long): Flow<Double> =
        transactions.map { list ->
            list.filter { it.customerId == customerId }
                .sumOf { if (it.type == TransactionType.CREDIT) it.amount else -it.amount }
        }

    override fun getTotalOutstanding(): Flow<Double> =
        transactions.map { list ->
            list.sumOf { if (it.type == TransactionType.CREDIT) it.amount else -it.amount }
        }

    override fun getTodayCredit(): Flow<Double> =
        transactions.map { list ->
            list.filter { it.type == TransactionType.CREDIT }
                .sumOf { it.amount }
        }

    override fun getTodayPayment(): Flow<Double> =
        transactions.map { list ->
            list.filter { it.type == TransactionType.PAYMENT }
                .sumOf { it.amount }
        }

    override suspend fun addTransaction(transaction: Transaction): Long {
        val id = nextId++
        val newTransaction = transaction.copy(id = id)
        transactions.value = transactions.value + newTransaction
        lastInsertedId = id
        return id
    }

    override suspend fun deleteTransaction(transactionId: Long) {
        transactions.value = transactions.value.filter { it.id != transactionId }
    }

    override suspend fun getDashboardSummary(): DashboardSummary {
        val txns = transactions.value
        return DashboardSummary(
            totalOutstanding = txns.sumOf {
                if (it.type == TransactionType.CREDIT) it.amount else -it.amount
            },
            todayCredit = txns.filter { it.type == TransactionType.CREDIT }.sumOf { it.amount },
            todayPayment = txns.filter { it.type == TransactionType.PAYMENT }.sumOf { it.amount },
            todayTransactionCount = txns.size,
            activeCustomerCount = txns.map { it.customerId }.distinct().size
        )
    }

    override suspend fun undoLastTransaction(): Transaction? {
        val lastId = lastInsertedId ?: return null
        val transaction = transactions.value.find { it.id == lastId } ?: return null
        transactions.value = transactions.value.filter { it.id != lastId }
        lastInsertedId = null
        return transaction
    }

    // Test helpers
    fun setTransactions(list: List<Transaction>) {
        transactions.value = list
    }
}
