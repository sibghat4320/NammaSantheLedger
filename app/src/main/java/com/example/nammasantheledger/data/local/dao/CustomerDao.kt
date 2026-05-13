package com.example.nammasantheledger.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.nammasantheledger.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Customer operations.
 * All queries return Flow for reactive UI updates.
 */
@Dao
interface CustomerDao {

    @Query("SELECT * FROM customers WHERE is_active = 1 ORDER BY name ASC")
    fun getAllActiveCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :customerId")
    fun getCustomerById(customerId: Long): Flow<CustomerEntity?>

    @Query("SELECT * FROM customers WHERE id = :customerId")
    suspend fun getCustomerByIdOnce(customerId: Long): CustomerEntity?

    @Query("""
        SELECT * FROM customers 
        WHERE is_active = 1 AND (
            name LIKE '%' || :query || '%' 
            OR phone_number LIKE '%' || :query || '%'
        ) 
        ORDER BY name ASC
    """)
    fun searchCustomers(query: String): Flow<List<CustomerEntity>>

    @Query("SELECT COUNT(*) FROM customers WHERE is_active = 1")
    fun getActiveCustomerCount(): Flow<Int>

    @Query("""
        SELECT c.* FROM customers c
        INNER JOIN transactions t ON c.id = t.customer_id
        WHERE c.is_active = 1
        GROUP BY c.id
        HAVING SUM(CASE WHEN t.type = 'CREDIT' THEN t.amount ELSE -t.amount END) > 0
        ORDER BY SUM(CASE WHEN t.type = 'CREDIT' THEN t.amount ELSE -t.amount END) DESC
    """)
    fun getCustomersWithOutstandingDues(): Flow<List<CustomerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Query("UPDATE customers SET is_active = 0, updated_at = :timestamp WHERE id = :customerId")
    suspend fun softDeleteCustomer(customerId: Long, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    @Query("SELECT * FROM customers WHERE phone_number != '' AND is_active = 1")
    fun getCustomersWithPhone(): Flow<List<CustomerEntity>>
}
