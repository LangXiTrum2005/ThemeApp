/*
 * Copyright (c) 2026 1Tabb. All rights reserved.
 * https://1tabb.com
 *
 * SPDX-License-Identifier: LicenseRef-1Tabb-Proprietary
 *
 * Proprietary and confidential. Contains trade secrets of 1Tabb.
 * Unauthorized copying, modification, distribution, reverse engineering,
 * decompilation or disassembly of this file, via any medium, is strictly
 * prohibited except with prior written permission of 1Tabb.
 */

package com.onetabb.template.android.core.base

sealed class UiState<out T> {
    data object Idle : UiState<Nothing>()
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

inline fun <T> UiState<T>.onSuccess(action: (T) -> Unit): UiState<T> {
    if (this is UiState.Success) action(data)
    return this
}

inline fun <T> UiState<T>.onError(action: (String) -> Unit): UiState<T> {
    if (this is UiState.Error) action(message)
    return this
}

inline fun <T> UiState<T>.onLoading(action: () -> Unit): UiState<T> {
    if (this is UiState.Loading) action()
    return this
}
