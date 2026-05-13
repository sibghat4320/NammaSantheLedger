package com.example.nammasantheledger.domain.model

/**
 * Domain model for daily business summary.
 */
data class DailySummary(
    val date: String, // "yyyy-MM-dd"
    val totalCredit: Double = 0.0,
    val totalPayment: Double = 0.0,
    val transactionCount: Int = 0,
    val uniqueCustomers: Int = 0
) {
    val netBalance: Double get() = totalCredit - totalPayment
    val repaymentRate: Double get() = if (totalCredit > 0) (totalPayment / totalCredit) * 100 else 0.0
}

/**
 * Dashboard summary model combining multiple data points.
 */
data class DashboardSummary(
    val totalOutstanding: Double = 0.0,
    val todayCredit: Double = 0.0,
    val todayPayment: Double = 0.0,
    val todayTransactionCount: Int = 0,
    val activeCustomerCount: Int = 0,
    val weeklyTrend: Double = 0.0 // Percentage change from last week
)
