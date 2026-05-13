package com.example.nammasantheledger.core.common

/**
 * A generic sealed class that represents the result of an operation.
 * Used throughout the app for clean error handling without exceptions.
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : Result<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw throwable ?: RuntimeException(message)
    }

    fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> Error(message, throwable)
    }

    suspend fun <R> suspendMap(transform: suspend (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> Error(message, throwable)
    }

    companion object {
        inline fun <T> runCatching(block: () -> T): Result<T> {
            return try {
                Success(block())
            } catch (e: Exception) {
                Error(e.message ?: "Unknown error", e)
            }
        }
    }
}
