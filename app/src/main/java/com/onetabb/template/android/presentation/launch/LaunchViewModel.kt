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

package com.onetabb.template.android.presentation.launch

import com.onetabb.template.android.core.base.BaseViewModel
import com.onetabb.template.android.data.local.SharedPref
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * TODO(project): thêm warm-up dữ liệu đầu app tại đây (gọi API cấu hình, nạp cache...) và bọc
 * bằng `launchSafely` + `withTimeoutOrNull` để không kéo dài splash quá lâu.
 */
@HiltViewModel
class LaunchViewModel @Inject constructor(
    private val sharedPref: SharedPref,
) : BaseViewModel() {

    fun markOpened() {
        sharedPref.hasOpenedBefore = true
    }
}
