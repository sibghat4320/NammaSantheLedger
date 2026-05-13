package com.example.nammasantheledger.feature.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nammasantheledger.core.util.DateTimeUtil
import com.example.nammasantheledger.domain.model.Transaction
import com.example.nammasantheledger.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val totalOutstanding: Double = 0.0,
    val weeklyCredit: Double = 0.0,
    val weeklyPayment: Double = 0.0,
    val monthlyCredit: Double = 0.0,
    val monthlyPayment: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAnalytics()
    }

    private fun loadAnalytics() {
        viewModelScope.launch {
            transactionRepository.getTotalOutstanding().collect { total ->
                _uiState.value = _uiState.value.copy(
                    totalOutstanding = total,
                    isLoading = false
                )
            }
        }
        viewModelScope.launch {
            transactionRepository.getAllTransactions().collect { transactions ->
                val now = DateTimeUtil.now()
                val weekStart = DateTimeUtil.weekStart()
                val monthStart = DateTimeUtil.monthStart()

                val weeklyTxns = transactions.filter { it.timestamp >= weekStart }
                val monthlyTxns = transactions.filter { it.timestamp >= monthStart }

                _uiState.value = _uiState.value.copy(
                    weeklyCredit = weeklyTxns
                        .filter { it.type == com.example.nammasantheledger.domain.model.TransactionType.CREDIT }
                        .sumOf { it.amount },
                    weeklyPayment = weeklyTxns
                        .filter { it.type == com.example.nammasantheledger.domain.model.TransactionType.PAYMENT }
                        .sumOf { it.amount },
                    monthlyCredit = monthlyTxns
                        .filter { it.type == com.example.nammasantheledger.domain.model.TransactionType.CREDIT }
                        .sumOf { it.amount },
                    monthlyPayment = monthlyTxns
                        .filter { it.type == com.example.nammasantheledger.domain.model.TransactionType.PAYMENT }
                        .sumOf { it.amount },
                    recentTransactions = transactions.take(50)
                )
            }
        }
    }
}
