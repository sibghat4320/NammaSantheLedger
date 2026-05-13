package com.example.nammasantheledger.core.common

/**
 * Represents the state of UI content that loads asynchronously.
 * Enforces handling of all states in the UI layer.
 */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
    data object Empty : UiState<Nothing>
}
