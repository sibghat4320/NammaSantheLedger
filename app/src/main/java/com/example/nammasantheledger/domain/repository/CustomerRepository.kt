package com.example.nammasantheledger.domain.repository

import com.example.nammasantheledger.domain.model.Customer
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Customer operations.
 * Defined in the domain layer - implemented in data layer.
 */
interface CustomerRepository {
    fun getAllActiveCustomers(): Flow<List<Customer>>
    fun searchCustomers(query: String): Flow<List<Customer>>
    fun getCustomerById(customerId: Long): Flow<Customer?>
    fun getActiveCustomerCount(): Flow<Int>
    fun getCustomersWithOutstandingDues(): Flow<List<Customer>>
    fun getCustomersWithPhone(): Flow<List<Customer>>
    suspend fun addCustomer(customer: Customer): Long
    suspend fun updateCustomer(customer: Customer)
    suspend fun deleteCustomer(customerId: Long)
}
