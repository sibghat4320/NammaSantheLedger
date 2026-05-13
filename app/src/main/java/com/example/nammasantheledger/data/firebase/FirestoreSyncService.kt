package com.example.nammasantheledger.data.firebase

import com.example.nammasantheledger.domain.model.Customer
import com.example.nammasantheledger.domain.model.Transaction
import com.example.nammasantheledger.domain.model.TransactionType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for syncing local Room data to Cloud Firestore.
 *
 * Firestore structure:
 *   users/{userId}/
 *     ├── customers/{customerId}    — mirrors CustomerEntity
 *     └── transactions/{txnId}      — mirrors TransactionEntity
 *
 * Design decisions:
 * - Write-through: every local write also pushes to Firestore
 * - Uses merge (SetOptions.merge()) to avoid overwriting fields
 * - userId from FirebaseAuth ensures data isolation per user
 * - Graceful no-op when user is not logged in (offline-only mode)
 */
@Singleton
class FirestoreSyncService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    private val userId: String?
        get() = firebaseAuth.currentUser?.uid

    // ── Customer Sync ────────────────────────────────────────────────────────

    /**
     * Syncs a customer to Firestore. Uses the local Room ID as the document ID.
     * No-op if user is not logged in.
     */
    suspend fun syncCustomer(customer: Customer) {
        val uid = userId ?: return
        val docRef = firestore.collection("users").document(uid)
            .collection("customers").document(customer.id.toString())

        val data = mapOf(
            "id" to customer.id,
            "name" to customer.name,
            "phoneNumber" to customer.phoneNumber,
            "address" to customer.address,
            "notes" to customer.notes,
            "isActive" to customer.isActive,
            "outstandingBalance" to customer.outstandingBalance,
            "createdAt" to customer.createdAt,
            "updatedAt" to customer.updatedAt
        )

        try {
            docRef.set(data, SetOptions.merge()).await()
        } catch (_: Exception) {
            // Silently fail — data is safe in local Room DB
            // Will sync on next attempt
        }
    }

    /**
     * Deletes a customer from Firestore (soft or hard).
     */
    suspend fun deleteCustomer(customerId: Long) {
        val uid = userId ?: return
        try {
            firestore.collection("users").document(uid)
                .collection("customers").document(customerId.toString())
                .delete().await()
        } catch (_: Exception) { }
    }

    // ── Transaction Sync ─────────────────────────────────────────────────────

    /**
     * Syncs a transaction to Firestore.
     */
    suspend fun syncTransaction(transaction: Transaction) {
        val uid = userId ?: return
        val docRef = firestore.collection("users").document(uid)
            .collection("transactions").document(transaction.id.toString())

        val data = mapOf(
            "id" to transaction.id,
            "customerId" to transaction.customerId,
            "customerName" to transaction.customerName,
            "amount" to transaction.amount,
            "type" to transaction.type.name,
            "notes" to transaction.notes,
            "timestamp" to transaction.timestamp,
            "createdAt" to transaction.createdAt
        )

        try {
            docRef.set(data, SetOptions.merge()).await()
        } catch (_: Exception) { }
    }

    /**
     * Deletes a transaction from Firestore.
     */
    suspend fun deleteTransaction(transactionId: Long) {
        val uid = userId ?: return
        try {
            firestore.collection("users").document(uid)
                .collection("transactions").document(transactionId.toString())
                .delete().await()
        } catch (_: Exception) { }
    }

    // ── Bulk Sync (Initial upload of local data) ─────────────────────────────

    /**
     * Uploads all local customers to Firestore.
     * Called after first login to backup existing offline data.
     */
    suspend fun syncAllCustomers(customers: List<Customer>) {
        val uid = userId ?: return
        val batch = firestore.batch()
        val customersRef = firestore.collection("users").document(uid)
            .collection("customers")

        customers.forEach { customer ->
            val docRef = customersRef.document(customer.id.toString())
            val data = mapOf(
                "id" to customer.id,
                "name" to customer.name,
                "phoneNumber" to customer.phoneNumber,
                "address" to customer.address,
                "notes" to customer.notes,
                "isActive" to customer.isActive,
                "outstandingBalance" to customer.outstandingBalance,
                "createdAt" to customer.createdAt,
                "updatedAt" to customer.updatedAt
            )
            batch.set(docRef, data, SetOptions.merge())
        }

        try {
            batch.commit().await()
        } catch (_: Exception) { }
    }

    /**
     * Uploads all local transactions to Firestore.
     * Called after first login to backup existing offline data.
     */
    suspend fun syncAllTransactions(transactions: List<Transaction>) {
        val uid = userId ?: return
        // Firestore batch limit is 500, chunk if needed
        transactions.chunked(450).forEach { chunk ->
            val batch = firestore.batch()
            val txnRef = firestore.collection("users").document(uid)
                .collection("transactions")

            chunk.forEach { txn ->
                val docRef = txnRef.document(txn.id.toString())
                val data = mapOf(
                    "id" to txn.id,
                    "customerId" to txn.customerId,
                    "customerName" to txn.customerName,
                    "amount" to txn.amount,
                    "type" to txn.type.name,
                    "notes" to txn.notes,
                    "timestamp" to txn.timestamp,
                    "createdAt" to txn.createdAt
                )
                batch.set(docRef, data, SetOptions.merge())
            }

            try {
                batch.commit().await()
            } catch (_: Exception) { }
        }
    }

    // ── Restore from Cloud ───────────────────────────────────────────────────

    /**
     * Fetches all customers from Firestore for the current user.
     * Used for restoring data on a new device.
     */
    suspend fun fetchCloudCustomers(): List<Customer> {
        val uid = userId ?: return emptyList()
        return try {
            val snapshot = firestore.collection("users").document(uid)
                .collection("customers").get().await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    Customer(
                        id = doc.getLong("id") ?: 0L,
                        name = doc.getString("name") ?: return@mapNotNull null,
                        phoneNumber = doc.getString("phoneNumber") ?: "",
                        address = doc.getString("address") ?: "",
                        notes = doc.getString("notes") ?: "",
                        isActive = doc.getBoolean("isActive") ?: true,
                        outstandingBalance = doc.getDouble("outstandingBalance") ?: 0.0,
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                        updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                    )
                } catch (_: Exception) { null }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Fetches all transactions from Firestore for the current user.
     */
    suspend fun fetchCloudTransactions(): List<Transaction> {
        val uid = userId ?: return emptyList()
        return try {
            val snapshot = firestore.collection("users").document(uid)
                .collection("transactions").get().await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    Transaction(
                        id = doc.getLong("id") ?: 0L,
                        customerId = doc.getLong("customerId") ?: return@mapNotNull null,
                        customerName = doc.getString("customerName") ?: "",
                        amount = doc.getDouble("amount") ?: return@mapNotNull null,
                        type = TransactionType.valueOf(doc.getString("type") ?: "CREDIT"),
                        notes = doc.getString("notes") ?: "",
                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    )
                } catch (_: Exception) { null }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
