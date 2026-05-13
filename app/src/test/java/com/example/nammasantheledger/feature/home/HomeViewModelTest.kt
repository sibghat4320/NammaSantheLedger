package com.example.nammasantheledger.feature.home

import com.example.nammasantheledger.data.repository.FakeCustomerRepository
import com.example.nammasantheledger.data.repository.FakeTransactionRepository
import com.example.nammasantheledger.domain.model.Customer
import com.example.nammasantheledger.domain.model.Transaction
import com.example.nammasantheledger.domain.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var transactionRepository: FakeTransactionRepository
    private lateinit var customerRepository: FakeCustomerRepository
    private lateinit var viewModel: HomeViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        transactionRepository = FakeTransactionRepository()
        customerRepository = FakeCustomerRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state shows loading then data`() = runTest {
        // Given some test data
        customerRepository.addCustomer(Customer(name = "Ramesh", phoneNumber = "9876543210"))
        transactionRepository.addTransaction(
            Transaction(customerId = 1, amount = 500.0, type = TransactionType.CREDIT)
        )

        // When ViewModel is created
        viewModel = HomeViewModel(transactionRepository, customerRepository)
        advanceUntilIdle()

        // Then state reflects the data
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(500.0, state.totalOutstanding, 0.01)
        assertEquals(1, state.recentTransactions.size)
    }

    @Test
    fun `total outstanding calculated correctly`() = runTest {
        // Given credit and payment transactions
        transactionRepository.addTransaction(
            Transaction(customerId = 1, amount = 1000.0, type = TransactionType.CREDIT)
        )
        transactionRepository.addTransaction(
            Transaction(customerId = 1, amount = 400.0, type = TransactionType.PAYMENT)
        )

        viewModel = HomeViewModel(transactionRepository, customerRepository)
        advanceUntilIdle()

        // Then outstanding is credit - payment
        assertEquals(600.0, viewModel.uiState.value.totalOutstanding, 0.01)
    }

    @Test
    fun `empty state when no transactions`() = runTest {
        viewModel = HomeViewModel(transactionRepository, customerRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(0.0, state.totalOutstanding, 0.01)
        assertEquals(0, state.recentTransactions.size)
    }
}
