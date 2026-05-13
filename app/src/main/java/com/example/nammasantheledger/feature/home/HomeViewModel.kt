package com.example.nammasantheledger.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nammasantheledger.domain.model.DashboardSummary
import com.example.nammasantheledger.domain.model.Transaction
import com.example.nammasantheledger.domain.repository.CustomerRepository
import com.example.nammasantheledger.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val dashboardSummary: DashboardSummary = DashboardSummary(),
    val recentTransactions: List<Transaction> = emptyList(),
    val totalOutstanding: Double = 0.0,
    val todayCredit: Double = 0.0,
    val todayPayment: Double = 0.0,
    val activeCustomers: Int = 0,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
        observeRealtimeData()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            try {
                val summary = transactionRepository.getDashboardSummary()
                _uiState.value = _uiState.value.copy(
                    dashboardSummary = summary,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private fun observeRealtimeData() {
        viewModelScope.launch {
            transactionRepository.getRecentTransactions(15).collect { transactions ->
                _uiState.value = _uiState.value.copy(recentTransactions = transactions)
            }
        }
        viewModelScope.launch {
            transactionRepository.getTotalOutstanding().collect { total ->
                _uiState.value = _uiState.value.copy(totalOutstanding = total)
            }
        }
        viewModelScope.launch {
            transactionRepository.getTodayCredit().collect { credit ->
                _uiState.value = _uiState.value.copy(todayCredit = credit)
            }
        }
        viewModelScope.launch {
            transactionRepository.getTodayPayment().collect { payment ->
                _uiState.value = _uiState.value.copy(todayPayment = payment)
            }
        }
        viewModelScope.launch {
            customerRepository.getActiveCustomerCount().collect { count ->
                _uiState.value = _uiState.value.copy(activeCustomers = count)
            }
        }
    }

    fun refreshDashboard() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadDashboard()
    }
}
