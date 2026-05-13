package com.example.nammasantheledger.data.repository

import com.example.nammasantheledger.data.local.dao.CustomerDao
import com.example.nammasantheledger.data.local.dao.TransactionDao
import com.example.nammasantheledger.data.firebase.FirestoreSyncService
import com.example.nammasantheledger.data.mapper.toDomain
import com.example.nammasantheledger.data.mapper.toEntity
import com.example.nammasantheledger.domain.model.Customer
import com.example.nammasantheledger.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of CustomerRepository using Room database.
 *
 * Design decision: We combine customer entities with their balances
 * to provide the outstandingBalance field in the domain model.
 * This avoids N+1 queries by leveraging Flow's combine operator.
 */
@Singleton
class CustomerRepositoryImpl @Inject constructor(
    private val customerDao: CustomerDao,
    private val transactionDao: TransactionDao,
    private val firestoreSyncService: FirestoreSyncService
) : CustomerRepository {

    override fun getAllActiveCustomers(): Flow<List<Customer>> {
        return customerDao.getAllActiveCustomers().map { entities ->
            entities.map { entity -> entity.toDomain() }
        }
    }

    override fun searchCustomers(query: String): Flow<List<Customer>> {
        return customerDao.searchCustomers(query).map { entities ->
            entities.map { entity -> entity.toDomain() }
        }
    }

    override fun getCustomerById(customerId: Long): Flow<Customer?> {
        return combine(
            customerDao.getCustomerById(customerId),
            transactionDao.getCustomerBalance(customerId)
        ) { entity, balance ->
            entity?.toDomain(outstandingBalance = balance)
        }
    }

    override fun getActiveCustomerCount(): Flow<Int> {
        return customerDao.getActiveCustomerCount()
    }

    override fun getCustomersWithOutstandingDues(): Flow<List<Customer>> {
        return customerDao.getCustomersWithOutstandingDues().map { entities ->
            entities.map { entity ->
                val balance = try {
                    transactionDao.getCustomerBalanceOnce(entity.id)
                } catch (_: Exception) { 0.0 }
                entity.toDomain(outstandingBalance = balance)
            }
        }
    }

    override fun getCustomersWithPhone(): Flow<List<Customer>> {
        return customerDao.getCustomersWithPhone().map { entities ->
            entities.map { entity -> entity.toDomain() }
        }
    }

    override suspend fun addCustomer(customer: Customer): Long {
        val id = customerDao.insertCustomer(customer.toEntity())
        try { firestoreSyncService.syncCustomer(customer.copy(id = id)) } catch (_: Exception) { }
        return id
    }

    override suspend fun updateCustomer(customer: Customer) {
        customerDao.updateCustomer(customer.toEntity())
        try { firestoreSyncService.syncCustomer(customer) } catch (_: Exception) { }
    }

    override suspend fun deleteCustomer(customerId: Long) {
        customerDao.softDeleteCustomer(customerId)
        try { firestoreSyncService.deleteCustomer(customerId) } catch (_: Exception) { }
    }
}
