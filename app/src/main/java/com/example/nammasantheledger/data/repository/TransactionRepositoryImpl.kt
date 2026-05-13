package com.example.nammasantheledger.data.repository

import com.example.nammasantheledger.core.util.DateTimeUtil
import com.example.nammasantheledger.data.firebase.FirestoreSyncService
import com.example.nammasantheledger.data.local.dao.CustomerDao
import com.example.nammasantheledger.data.local.dao.TransactionDao
import com.example.nammasantheledger.data.mapper.toDomain
import com.example.nammasantheledger.data.mapper.toEntity
import com.example.nammasantheledger.domain.model.DashboardSummary
import com.example.nammasantheledger.domain.model.Transaction
import com.example.nammasantheledger.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of TransactionRepository using Room database.
 *
 * Design decisions:
 * - Customer names are resolved via a lookup to avoid denormalization
 * - Undo is implemented by tracking the last inserted transaction ID
 * - Dashboard summary is computed from multiple DAO queries
 */
@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val customerDao: CustomerDao,
    private val firestoreSyncService: FirestoreSyncService
) : TransactionRepository {

    // Track last transaction for undo functionality
    private var lastInsertedTransactionId: Long? = null

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { entities ->
            entities.map { entity ->
                val customerName = customerDao.getCustomerByIdOnce(entity.customerId)?.name ?: "Unknown"
                entity.toDomain(customerName)
            }
        }
    }

    override fun getRecentTransactions(limit: Int): Flow<List<Transaction>> {
        return transactionDao.getRecentTransactions(limit).map { entities ->
            entities.map { entity ->
                val customerName = customerDao.getCustomerByIdOnce(entity.customerId)?.name ?: "Unknown"
                entity.toDomain(customerName)
            }
        }
    }

    override fun getTransactionsForCustomer(customerId: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsForCustomer(customerId).map { entities ->
            val customerName = customerDao.getCustomerByIdOnce(customerId)?.name ?: "Unknown"
            entities.map { entity -> entity.toDomain(customerName) }
        }
    }

    override fun getCustomerBalance(customerId: Long): Flow<Double> {
        return transactionDao.getCustomerBalance(customerId)
    }

    override fun getTotalOutstanding(): Flow<Double> {
        return transactionDao.getTotalOutstanding()
    }

    override fun getTodayCredit(): Flow<Double> {
        return transactionDao.getTotalCreditBetween(
            DateTimeUtil.todayStart(),
            DateTimeUtil.todayEnd()
        )
    }

    override fun getTodayPayment(): Flow<Double> {
        return transactionDao.getTotalPaymentBetween(
            DateTimeUtil.todayStart(),
            DateTimeUtil.todayEnd()
        )
    }

    override suspend fun addTransaction(transaction: Transaction): Long {
        val id = transactionDao.insertTransaction(transaction.toEntity())
        lastInsertedTransactionId = id
        // Sync to Firestore (fire-and-forget, non-blocking)
        try {
            val customerName = customerDao.getCustomerByIdOnce(transaction.customerId)?.name ?: ""
            firestoreSyncService.syncTransaction(transaction.copy(id = id, customerName = customerName))
        } catch (_: Exception) { }
        return id
    }

    override suspend fun deleteTransaction(transactionId: Long) {
        transactionDao.deleteTransactionById(transactionId)
        try { firestoreSyncService.deleteTransaction(transactionId) } catch (_: Exception) { }
    }

    override suspend fun getDashboardSummary(): DashboardSummary {
        val todayStart = DateTimeUtil.todayStart()
        val todayEnd = DateTimeUtil.todayEnd()
        val weekStart = DateTimeUtil.weekStart()

        val totalOutstanding = transactionDao.getTotalOutstanding().first()
        val todayCredit = transactionDao.getTotalCreditBetween(todayStart, todayEnd).first()
        val todayPayment = transactionDao.getTotalPaymentBetween(todayStart, todayEnd).first()
        val todayCount = transactionDao.getTransactionCountBetween(todayStart, todayEnd).first()
        val activeCustomers = customerDao.getActiveCustomerCount().first()

        // Calculate weekly trend
        val lastWeekCredit = transactionDao.getTotalCreditBetween(
            DateTimeUtil.daysAgo(14), weekStart
        ).first()
        val thisWeekCredit = transactionDao.getTotalCreditBetween(weekStart, todayEnd).first()
        val trend = if (lastWeekCredit > 0) {
            ((thisWeekCredit - lastWeekCredit) / lastWeekCredit) * 100
        } else 0.0

        return DashboardSummary(
            totalOutstanding = totalOutstanding,
            todayCredit = todayCredit,
            todayPayment = todayPayment,
            todayTransactionCount = todayCount,
            activeCustomerCount = activeCustomers,
            weeklyTrend = trend
        )
    }

    override suspend fun undoLastTransaction(): Transaction? {
        val lastId = lastInsertedTransactionId ?: return null
        val entity = transactionDao.getTransactionById(lastId) ?: return null
        val customerName = customerDao.getCustomerByIdOnce(entity.customerId)?.name ?: "Unknown"
        transactionDao.deleteTransactionById(lastId)
        lastInsertedTransactionId = null
        return entity.toDomain(customerName)
    }
}
