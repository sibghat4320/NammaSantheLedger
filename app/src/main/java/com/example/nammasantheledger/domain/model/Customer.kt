package com.example.nammasantheledger.domain.model

/**
 * Domain model representing a customer.
 * Decoupled from the Room entity for clean architecture.
 */
data class Customer(
    val id: Long = 0,
    val name: String,
    val phoneNumber: String = "",
    val address: String = "",
    val notes: String = "",
    val isActive: Boolean = true,
    val outstandingBalance: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
