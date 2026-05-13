package com.example.nammasantheledger.data.repository

import com.example.nammasantheledger.domain.model.Customer
import com.example.nammasantheledger.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Fake CustomerRepository for unit testing.
 * Backed by in-memory MutableStateFlow for reactive behavior.
 */
class FakeCustomerRepository : CustomerRepository {

    private val customers = MutableStateFlow<List<Customer>>(emptyList())
    private var nextId = 1L

    override fun getAllActiveCustomers(): Flow<List<Customer>> =
        customers.map { list -> list.filter { it.isActive } }

    override fun searchCustomers(query: String): Flow<List<Customer>> =
        customers.map { list ->
            list.filter {
                it.isActive && (it.name.contains(query, ignoreCase = true) ||
                        it.phoneNumber.contains(query))
            }
        }

    override fun getCustomerById(customerId: Long): Flow<Customer?> =
        customers.map { list -> list.find { it.id == customerId } }

    override fun getActiveCustomerCount(): Flow<Int> =
        customers.map { list -> list.count { it.isActive } }

    override fun getCustomersWithOutstandingDues(): Flow<List<Customer>> =
        customers.map { list -> list.filter { it.outstandingBalance > 0 } }

    override fun getCustomersWithPhone(): Flow<List<Customer>> =
        customers.map { list -> list.filter { it.phoneNumber.isNotEmpty() && it.isActive } }

    override suspend fun addCustomer(customer: Customer): Long {
        val id = nextId++
        val newCustomer = customer.copy(id = id)
        customers.value = customers.value + newCustomer
        return id
    }

    override suspend fun updateCustomer(customer: Customer) {
        customers.value = customers.value.map {
            if (it.id == customer.id) customer else it
        }
    }

    override suspend fun deleteCustomer(customerId: Long) {
        customers.value = customers.value.map {
            if (it.id == customerId) it.copy(isActive = false) else it
        }
    }

    // Test helpers
    fun setCustomers(list: List<Customer>) {
        customers.value = list
    }
}
