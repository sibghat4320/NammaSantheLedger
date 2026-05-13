package com.example.nammasantheledger.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository handling Firebase Email/Password Authentication.
 *
 * Flow:
 * 1. New user → signUp(email, password) creates account
 * 2. Existing user → signIn(email, password) logs in
 * 3. On success, FirebaseUser is available for Firestore operations
 *
 * Design decisions:
 * - Email/Password is 100% free on Firebase (no SMS costs)
 * - Uses Kotlin coroutines via await() extension
 * - Exposes currentUser for checking auth state across the app
 */
@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    val isLoggedIn: Boolean
        get() = firebaseAuth.currentUser != null

    /**
     * Creates a new account with email and password.
     */
    suspend fun signUp(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user
                ?: return Result.failure(Exception("Account created but user is null"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Signs in with an existing email and password.
     */
    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user
                ?: return Result.failure(Exception("Sign-in succeeded but user is null"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sends a password reset email.
     */
    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}
