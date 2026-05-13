package com.example.nammasantheledger.feature.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nammasantheledger.domain.model.Customer
import com.example.nammasantheledger.domain.repository.CustomerRepository
import com.example.nammasantheledger.domain.repository.TransactionRepository
import com.example.nammasantheledger.domain.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Customers List ───────────────────────────────────────────────────────────

data class CustomersUiState(
    val isLoading: Boolean = true,
    val customers: List<Customer> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null
)

@HiltViewModel
class CustomersViewModel @Inject constructor(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomersUiState())
    val uiState: StateFlow<CustomersUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        observeCustomers()
    }

    private fun observeCustomers() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    if (query.isBlank()) {
                        customerRepository.getAllActiveCustomers()
                    } else {
                        customerRepository.searchCustomers(query)
                    }
                }
                .collect { customers ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        customers = customers
                    )
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        _searchQuery.value = query
    }

    fun deleteCustomer(customerId: Long) {
        viewModelScope.launch {
            customerRepository.deleteCustomer(customerId)
        }
    }
}

// ── Customer Detail ──────────────────────────────────────────────────────────

data class CustomerDetailUiState(
    val isLoading: Boolean = true,
    val customer: Customer? = null,
    val transactions: List<Transaction> = emptyList(),
    val balance: Double = 0.0,
    val error: String? = null
)

@HiltViewModel
class CustomerDetailViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerDetailUiState())
    val uiState: StateFlow<CustomerDetailUiState> = _uiState.asStateFlow()

    fun loadCustomer(customerId: Long) {
        viewModelScope.launch {
            customerRepository.getCustomerById(customerId).collect { customer ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    customer = customer,
                    balance = customer?.outstandingBalance ?: 0.0
                )
            }
        }
        viewModelScope.launch {
            transactionRepository.getTransactionsForCustomer(customerId).collect { transactions ->
                _uiState.value = _uiState.value.copy(transactions = transactions)
            }
        }
    }
}

// ── Add/Edit Customer ────────────────────────────────────────────────────────

data class CustomerFormUiState(
    val name: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val notes: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddCustomerViewModel @Inject constructor(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerFormUiState())
    val uiState: StateFlow<CustomerFormUiState> = _uiState.asStateFlow()

    fun onNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(name = name, error = null)
    }

    fun onPhoneChanged(phone: String) {
        _uiState.value = _uiState.value.copy(phoneNumber = phone)
    }

    fun onAddressChanged(address: String) {
        _uiState.value = _uiState.value.copy(address = address)
    }

    fun onNotesChanged(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun saveCustomer() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.value = state.copy(error = "Customer name is required")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                customerRepository.addCustomer(
                    Customer(
                        name = state.name.trim(),
                        phoneNumber = state.phoneNumber.trim(),
                        address = state.address.trim(),
                        notes = state.notes.trim()
                    )
                )
                _uiState.value = _uiState.value.copy(isLoading = false, isSaved = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to save customer"
                )
            }
        }
    }
}

@HiltViewModel
class EditCustomerViewModel @Inject constructor(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerFormUiState())
    val uiState: StateFlow<CustomerFormUiState> = _uiState.asStateFlow()
    private var customerId: Long = 0

    fun loadCustomer(id: Long) {
        customerId = id
        viewModelScope.launch {
            customerRepository.getCustomerById(id).collect { customer ->
                customer?.let {
                    _uiState.value = _uiState.value.copy(
                        name = it.name,
                        phoneNumber = it.phoneNumber,
                        address = it.address,
                        notes = it.notes,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(name = name, error = null)
    }

    fun onPhoneChanged(phone: String) {
        _uiState.value = _uiState.value.copy(phoneNumber = phone)
    }

    fun onAddressChanged(address: String) {
        _uiState.value = _uiState.value.copy(address = address)
    }

    fun onNotesChanged(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun updateCustomer() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.value = state.copy(error = "Customer name is required")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                customerRepository.updateCustomer(
                    Customer(
                        id = customerId,
                        name = state.name.trim(),
                        phoneNumber = state.phoneNumber.trim(),
                        address = state.address.trim(),
                        notes = state.notes.trim()
                    )
                )
                _uiState.value = _uiState.value.copy(isLoading = false, isSaved = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to update customer"
                )
            }
        }
    }
}
