package com.example.nammasantheledger.feature.transaction

import com.example.nammasantheledger.data.repository.FakeCustomerRepository
import com.example.nammasantheledger.data.repository.FakeTransactionRepository
import com.example.nammasantheledger.domain.model.Customer
import com.example.nammasantheledger.domain.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddTransactionViewModelTest {

    private lateinit var transactionRepository: FakeTransactionRepository
    private lateinit var customerRepository: FakeCustomerRepository
    private lateinit var viewModel: AddTransactionViewModel

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
    fun `keypad input builds amount correctly`() = runTest {
        viewModel = AddTransactionViewModel(transactionRepository, customerRepository)
        advanceUntilIdle()

        viewModel.onAmountKeyPress("5")
        viewModel.onAmountKeyPress("0")
        viewModel.onAmountKeyPress("0")

        assertEquals("500", viewModel.uiState.value.amountText)
    }

    @Test
    fun `backspace removes last digit`() = runTest {
        viewModel = AddTransactionViewModel(transactionRepository, customerRepository)
        advanceUntilIdle()

        viewModel.onAmountKeyPress("1")
        viewModel.onAmountKeyPress("2")
        viewModel.onAmountKeyPress("3")
        viewModel.onAmountKeyPress("⌫")

        assertEquals("12", viewModel.uiState.value.amountText)
    }

    @Test
    fun `clear resets amount`() = runTest {
        viewModel = AddTransactionViewModel(transactionRepository, customerRepository)
        advanceUntilIdle()

        viewModel.onAmountKeyPress("5")
        viewModel.onAmountKeyPress("0")
        viewModel.onAmountKeyPress("C")

        assertEquals("", viewModel.uiState.value.amountText)
    }

    @Test
    fun `quick amount sets value directly`() = runTest {
        viewModel = AddTransactionViewModel(transactionRepository, customerRepository)
        advanceUntilIdle()

        viewModel.setQuickAmount(500)
        assertEquals("500", viewModel.uiState.value.amountText)
    }

    @Test
    fun `confirm without customer shows error`() = runTest {
        viewModel = AddTransactionViewModel(transactionRepository, customerRepository)
        advanceUntilIdle()

        viewModel.onAmountKeyPress("1")
        viewModel.onAmountKeyPress("0")
        viewModel.onAmountKeyPress("0")
        viewModel.confirmTransaction()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)
    }

    @Test
    fun `confirm with valid data saves transaction`() = runTest {
        val customer = Customer(id = 1, name = "Ramesh")
        customerRepository.addCustomer(customer)

        viewModel = AddTransactionViewModel(transactionRepository, customerRepository)
        advanceUntilIdle()

        viewModel.selectCustomer(customer.copy(id = 1))
        viewModel.setQuickAmount(500)
        viewModel.confirmTransaction()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `toggle transaction type switches between credit and payment`() = runTest {
        viewModel = AddTransactionViewModel(transactionRepository, customerRepository)
        advanceUntilIdle()

        assertEquals(TransactionType.CREDIT, viewModel.uiState.value.transactionType)

        viewModel.toggleTransactionType()
        assertEquals(TransactionType.PAYMENT, viewModel.uiState.value.transactionType)

        viewModel.toggleTransactionType()
        assertEquals(TransactionType.CREDIT, viewModel.uiState.value.transactionType)
    }

    @Test
    fun `undo removes last transaction`() = runTest {
        val customer = Customer(id = 1, name = "Ramesh")
        customerRepository.addCustomer(customer)

        viewModel = AddTransactionViewModel(transactionRepository, customerRepository)
        advanceUntilIdle()

        viewModel.selectCustomer(customer.copy(id = 1))
        viewModel.setQuickAmount(500)
        viewModel.confirmTransaction()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSaved)

        viewModel.undoTransaction()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSaved)
    }
}
