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

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Giống BaseViewModel nhưng extend AndroidViewModel để có Application context.
 * Dùng cho ViewModel cần truy cập Application (vd: khởi tạo singleton cần context).
 */
abstract class BaseAndroidViewModel(app: Application) : AndroidViewModel(app) {

    protected open fun onError(throwable: Throwable) = Unit

    protected fun launchSafely(
        onError: (Throwable) -> Unit = ::onError,
        block: suspend CoroutineScope.() -> Unit,
    ) {
        val handler = CoroutineExceptionHandler { _, throwable -> onError(throwable) }
        viewModelScope.launch(handler, block = block)
    }
}
