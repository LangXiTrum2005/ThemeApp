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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {

    protected open fun onError(throwable: Throwable) = Unit

    protected fun launchSafely(
        onError: (Throwable) -> Unit = ::onError,
        block: suspend CoroutineScope.() -> Unit,
    ) {
        val handler = CoroutineExceptionHandler { _, throwable -> onError(throwable) }
        viewModelScope.launch(handler, block = block)
    }
}
