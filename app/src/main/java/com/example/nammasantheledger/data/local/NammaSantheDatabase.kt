package com.example.nammasantheledger.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.nammasantheledger.data.local.dao.CustomerDao
import com.example.nammasantheledger.data.local.dao.DailySummaryDao
import com.example.nammasantheledger.data.local.dao.TransactionDao
import com.example.nammasantheledger.data.local.entity.CustomerEntity
import com.example.nammasantheledger.data.local.entity.DailySummaryEntity
import com.example.nammasantheledger.data.local.entity.TransactionEntity

/**
 * Room database for Namma Santhe Ledger.
 *
 * Version history:
 * - v1: Original single Transaction table
 * - v2: Added phoneNumber to transactions
 * - v3: Complete rewrite - separate Customer, Transaction (with FK), DailySummary tables
 *
 * Since v3 is a breaking schema change, we use destructive migration.
 * In production, you'd write proper migrations to preserve data.
 */
@Database(
    entities = [
        CustomerEntity::class,
        TransactionEntity::class,
        DailySummaryEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class NammaSantheDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun transactionDao(): TransactionDao
    abstract fun dailySummaryDao(): DailySummaryDao
}
