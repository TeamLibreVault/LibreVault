package org.librevault.common.state

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error<T>(val throwable: Throwable, val data: T? = null) : UiState<T>()

    val dataOrNull: T?
        get() = when (this) {
            is Success -> data
            is Error -> data
            else -> null
        }

    val throwableOrNull: Throwable?
        get() = when (this) {
            is Error -> throwable
            else -> null
        }
}