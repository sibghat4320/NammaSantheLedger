package com.example.nammasantheledger.data.mapper

import com.example.nammasantheledger.data.local.entity.CustomerEntity
import com.example.nammasantheledger.data.local.entity.DailySummaryEntity
import com.example.nammasantheledger.data.local.entity.TransactionEntity
import com.example.nammasantheledger.domain.model.Customer
import com.example.nammasantheledger.domain.model.DailySummary
import com.example.nammasantheledger.domain.model.Transaction
import com.example.nammasantheledger.domain.model.TransactionType

/**
 * Mapper functions between data layer entities and domain models.
 * Keeps the domain layer clean from Room annotations.
 */

// ── Customer Mappers ─────────────────────────────────────────────────────────

fun CustomerEntity.toDomain(outstandingBalance: Double = 0.0): Customer = Customer(
    id = id,
    name = name,
    phoneNumber = phoneNumber,
    address = address,
    notes = notes,
    isActive = isActive,
    outstandingBalance = outstandingBalance,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Customer.toEntity(): CustomerEntity = CustomerEntity(
    id = id,
    name = name,
    phoneNumber = phoneNumber,
    address = address,
    notes = notes,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = System.currentTimeMillis()
)

// ── Transaction Mappers ──────────────────────────────────────────────────────

fun TransactionEntity.toDomain(customerName: String = ""): Transaction = Transaction(
    id = id,
    customerId = customerId,
    customerName = customerName,
    amount = amount,
    type = TransactionType.valueOf(type),
    notes = notes,
    timestamp = timestamp,
    createdAt = createdAt
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    customerId = customerId,
    amount = amount,
    type = type.name,
    notes = notes,
    timestamp = timestamp,
    createdAt = createdAt
)

// ── DailySummary Mappers ─────────────────────────────────────────────────────

fun DailySummaryEntity.toDomain(): DailySummary = DailySummary(
    date = date,
    totalCredit = totalCredit,
    totalPayment = totalPayment,
    transactionCount = transactionCount,
    uniqueCustomers = uniqueCustomers
)

fun DailySummary.toEntity(): DailySummaryEntity = DailySummaryEntity(
    date = date,
    totalCredit = totalCredit,
    totalPayment = totalPayment,
    transactionCount = transactionCount,
    uniqueCustomers = uniqueCustomers
)
