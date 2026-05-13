package com.example.nammasantheledger.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nammasantheledger.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Transaction operations.
 * Provides both Flow-based reactive queries and suspend one-shot queries.
 */
@Dao
interface TransactionDao {

    // ── Read operations ──────────────────────────────────────────────────────

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int = 20): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE customer_id = :customerId ORDER BY timestamp DESC")
    fun getTransactionsForCustomer(customerId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    suspend fun getTransactionById(transactionId: Long): TransactionEntity?

    @Query("""
        SELECT * FROM transactions 
        WHERE timestamp BETWEEN :startTime AND :endTime 
        ORDER BY timestamp DESC
    """)
    fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<TransactionEntity>>

    // ── Aggregation queries ──────────────────────────────────────────────────

    @Query("""
        SELECT COALESCE(SUM(CASE WHEN type = 'CREDIT' THEN amount ELSE -amount END), 0) 
        FROM transactions 
        WHERE customer_id = :customerId
    """)
    fun getCustomerBalance(customerId: Long): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(CASE WHEN type = 'CREDIT' THEN amount ELSE -amount END), 0) 
        FROM transactions 
        WHERE customer_id = :customerId
    """)
    suspend fun getCustomerBalanceOnce(customerId: Long): Double

    @Query("""
        SELECT COALESCE(SUM(CASE WHEN type = 'CREDIT' THEN amount ELSE -amount END), 0) 
        FROM transactions
    """)
    fun getTotalOutstanding(): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) 
        FROM transactions 
        WHERE type = 'CREDIT' AND timestamp BETWEEN :startTime AND :endTime
    """)
    fun getTotalCreditBetween(startTime: Long, endTime: Long): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) 
        FROM transactions 
        WHERE type = 'PAYMENT' AND timestamp BETWEEN :startTime AND :endTime
    """)
    fun getTotalPaymentBetween(startTime: Long, endTime: Long): Flow<Double>

    @Query("SELECT COUNT(*) FROM transactions WHERE timestamp BETWEEN :startTime AND :endTime")
    fun getTransactionCountBetween(startTime: Long, endTime: Long): Flow<Int>

    @Query("""
        SELECT COUNT(DISTINCT customer_id) 
        FROM transactions 
        WHERE timestamp BETWEEN :startTime AND :endTime
    """)
    fun getUniqueCustomerCountBetween(startTime: Long, endTime: Long): Flow<Int>

    // ── Analytics queries ────────────────────────────────────────────────────

    @Query("""
        SELECT customer_id, 
               SUM(CASE WHEN type = 'CREDIT' THEN amount ELSE -amount END) as balance
        FROM transactions 
        GROUP BY customer_id 
        HAVING balance > 0
        ORDER BY balance DESC 
        LIMIT :limit
    """)
    suspend fun getTopDebtors(limit: Int = 10): List<CustomerBalanceResult>

    @Query("""
        SELECT COALESCE(SUM(CASE WHEN type = 'CREDIT' THEN amount ELSE 0 END), 0) as totalCredit,
               COALESCE(SUM(CASE WHEN type = 'PAYMENT' THEN amount ELSE 0 END), 0) as totalPayment,
               COUNT(*) as transactionCount
        FROM transactions
        WHERE timestamp BETWEEN :startTime AND :endTime
    """)
    suspend fun getDailySummaryData(startTime: Long, endTime: Long): DailySummaryResult

    // ── Write operations ─────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :transactionId")
    suspend fun deleteTransactionById(transactionId: Long)

    @Query("DELETE FROM transactions WHERE customer_id = :customerId")
    suspend fun deleteAllTransactionsForCustomer(customerId: Long)
}

/**
 * Query result model for customer balance aggregation.
 */
data class CustomerBalanceResult(
    val customer_id: Long,
    val balance: Double
)

/**
 * Query result model for daily summary aggregation.
 */
data class DailySummaryResult(
    val totalCredit: Double,
    val totalPayment: Double,
    val transactionCount: Int
)
