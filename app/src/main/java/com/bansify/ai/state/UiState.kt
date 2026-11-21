package com.bansify.ai.state

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data object Empty : UiState<Nothing>
    data class Error(val message: String, val canRetry: Boolean = true) : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
}
