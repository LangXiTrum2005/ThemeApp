package com.wavez.controllerwidget.core.base

sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Data<T>(val value: T) : UiState<T>
    data class Error(val throwable: Throwable, val message: String) : UiState<Nothing>
}
