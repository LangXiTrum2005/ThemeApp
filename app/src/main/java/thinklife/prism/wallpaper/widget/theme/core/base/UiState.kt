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

package thinklife.prism.wallpaper.widget.theme.core.base

sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Data<T>(val value: T) : UiState<T> {
        val data: T get() = value
    }
    data class Error(
        val throwable: Throwable = Throwable(),
        val message: String = throwable.message ?: "",
    ) : UiState<Nothing> {
        constructor(message: String) : this(Throwable(message), message)
    }
}

inline fun <T> UiState<T>.onSuccess(action: (T) -> Unit): UiState<T> {
    if (this is UiState.Data) action(value)
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
