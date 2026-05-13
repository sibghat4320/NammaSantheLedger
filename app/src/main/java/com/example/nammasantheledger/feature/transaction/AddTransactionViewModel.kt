package com.example.nammasantheledger.feature.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nammasantheledger.domain.model.Customer
import com.example.nammasantheledger.domain.model.Transaction
import com.example.nammasantheledger.domain.model.TransactionType
import com.example.nammasantheledger.domain.repository.CustomerRepository
import com.example.nammasantheledger.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddTransactionUiState(
    val customers: List<Customer> = emptyList(),
    val selectedCustomer: Customer? = null,
    val amountText: String = "",
    val transactionType: TransactionType = TransactionType.CREDIT,
    val notes: String = "",
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val showUndo: Boolean = false,
    val lastTransactionId: Long? = null,
    val error: String? = null
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    init {
        loadCustomers()
    }

    private fun loadCustomers() {
        viewModelScope.launch {
            customerRepository.getAllActiveCustomers().collect { customers ->
                _uiState.value = _uiState.value.copy(
                    customers = customers,
                    isLoading = false
                )
            }
        }
    }

    fun setPreSelectedCustomer(customerId: Long?) {
        if (customerId != null && customerId > 0) {
            viewModelScope.launch {
                customerRepository.getCustomerById(customerId).collect { customer ->
                    if (customer != null) {
                        _uiState.value = _uiState.value.copy(selectedCustomer = customer)
                    }
                }
            }
        }
    }

    fun selectCustomer(customer: Customer) {
        _uiState.value = _uiState.value.copy(selectedCustomer = customer, error = null)
    }

    fun onAmountKeyPress(key: String) {
        val current = _uiState.value.amountText
        val newText = when (key) {
            "⌫" -> if (current.isNotEmpty()) current.dropLast(1) else ""
            "." -> if (!current.contains(".")) "$current." else current
            "C" -> ""
            else -> {
                // Limit to 2 decimal places
                val parts = current.split(".")
                if (parts.size == 2 && parts[1].length >= 2) current
                else if (current.length >= 10) current // Max 10 digits
                else "$current$key"
            }
        }
        _uiState.value = _uiState.value.copy(amountText = newText, error = null)
    }

    fun setQuickAmount(amount: Int) {
        _uiState.value = _uiState.value.copy(amountText = amount.toString(), error = null)
    }

    fun toggleTransactionType() {
        val newType = if (_uiState.value.transactionType == TransactionType.CREDIT)
            TransactionType.PAYMENT else TransactionType.CREDIT
        _uiState.value = _uiState.value.copy(transactionType = newType)
    }

    fun setTransactionType(type: TransactionType) {
        _uiState.value = _uiState.value.copy(transactionType = type)
    }

    fun onNotesChanged(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun confirmTransaction() {
        val state = _uiState.value

        if (state.selectedCustomer == null) {
            _uiState.value = state.copy(error = "Please select a customer")
            return
        }

        val amount = state.amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _uiState.value = state.copy(error = "Enter a valid amount")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val id = transactionRepository.addTransaction(
                    Transaction(
                        customerId = state.selectedCustomer.id,
                        customerName = state.selectedCustomer.name,
                        amount = amount,
                        type = state.transactionType,
                        notes = state.notes.trim()
                    )
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSaved = true,
                    showUndo = true,
                    lastTransactionId = id
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to save transaction"
                )
            }
        }
    }

    fun undoTransaction() {
        viewModelScope.launch {
            transactionRepository.undoLastTransaction()
            _uiState.value = _uiState.value.copy(
                showUndo = false,
                isSaved = false,
                amountText = "",
                notes = ""
            )
        }
    }
}
