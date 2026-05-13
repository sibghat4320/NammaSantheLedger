package com.example.nammasantheledger.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nammasantheledger.data.firebase.FirebaseAuthRepository
import com.example.nammasantheledger.data.firebase.FirestoreSyncService
import com.example.nammasantheledger.domain.repository.CustomerRepository
import com.example.nammasantheledger.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isSignUpMode: Boolean = false,
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val isSyncingData: Boolean = false,
    val error: String? = null,
    val resetEmailSent: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: FirebaseAuthRepository,
    private val firestoreSyncService: FirestoreSyncService,
    private val customerRepository: CustomerRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState(
        isAuthenticated = authRepository.isLoggedIn
    ))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(email = email, error = null)
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun onConfirmPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = password, error = null)
    }

    fun toggleSignUpMode() {
        _uiState.value = _uiState.value.copy(
            isSignUpMode = !_uiState.value.isSignUpMode,
            error = null,
            confirmPassword = ""
        )
    }

    fun signIn() {
        val state = _uiState.value
        if (!validateEmail(state.email)) return
        if (state.password.length < 6) {
            _uiState.value = state.copy(error = "Password must be at least 6 characters")
            return
        }

        _uiState.value = state.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val result = authRepository.signIn(state.email.trim(), state.password)
            result.fold(
                onSuccess = { handleSuccessfulLogin() },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = mapFirebaseError(e.message)
                    )
                }
            )
        }
    }

    fun signUp() {
        val state = _uiState.value
        if (!validateEmail(state.email)) return
        if (state.password.length < 6) {
            _uiState.value = state.copy(error = "Password must be at least 6 characters")
            return
        }
        if (state.password != state.confirmPassword) {
            _uiState.value = state.copy(error = "Passwords don't match")
            return
        }

        _uiState.value = state.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val result = authRepository.signUp(state.email.trim(), state.password)
            result.fold(
                onSuccess = { handleSuccessfulLogin() },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = mapFirebaseError(e.message)
                    )
                }
            )
        }
    }

    fun forgotPassword() {
        val email = _uiState.value.email.trim()
        if (!validateEmail(email)) return

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val result = authRepository.sendPasswordReset(email)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        resetEmailSent = true,
                        error = null
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = mapFirebaseError(e.message)
                    )
                }
            )
        }
    }

    fun dismissResetMessage() {
        _uiState.value = _uiState.value.copy(resetEmailSent = false)
    }

    private fun validateEmail(email: String): Boolean {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _uiState.value = _uiState.value.copy(error = "Enter a valid email address")
            return false
        }
        return true
    }

    /**
     * After successful login, sync local data to Firestore.
     */
    private suspend fun handleSuccessfulLogin() {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isSyncingData = true
        )

        try {
            // Upload existing local data to cloud
            val customers = customerRepository.getAllActiveCustomers().first()
            val transactions = transactionRepository.getAllTransactions().first()

            if (customers.isNotEmpty()) {
                firestoreSyncService.syncAllCustomers(customers)
            }
            if (transactions.isNotEmpty()) {
                firestoreSyncService.syncAllTransactions(transactions)
            }
        } catch (_: Exception) {
            // Sync failure is non-fatal — data is safe locally
        }

        _uiState.value = _uiState.value.copy(
            isSyncingData = false,
            isAuthenticated = true
        )
    }

    fun skipLogin() {
        _uiState.value = _uiState.value.copy(isAuthenticated = true)
    }

    fun signOut() {
        authRepository.signOut()
        _uiState.value = AuthUiState()
    }

    /**
     * Maps Firebase error messages to user-friendly strings.
     */
    private fun mapFirebaseError(message: String?): String {
        return when {
            message == null -> "Something went wrong"
            "email address is already in use" in message ->
                "This email is already registered. Try signing in."
            "no user record" in message || "user may have been deleted" in message ->
                "No account found with this email. Try creating one."
            "password is invalid" in message || "wrong password" in message ->
                "Incorrect password. Try again."
            "badly formatted" in message ->
                "Please enter a valid email address."
            "network error" in message ->
                "Network error. Check your internet connection."
            "too many requests" in message ->
                "Too many attempts. Please try again later."
            else -> message
        }
    }
}
