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

package com.onetabb.template.android.data.provider

import com.onetabb.template.android.R
import com.onetabb.template.android.presentation.onboarding.model.OnboardingModel

/**
 * Nội dung 2 trang onboarding onb2 / onb3.
 *
 * TODO(project): thay title + ảnh thật. Thêm trang mới phải khai thêm màn `onbN_view` + slot
 * trong `assets/ads_config.json` và hằng số trong AdsScreen/AdsSlot/AdsTrigger.
 */
object OnboardingProvider {

    val onboardingList = listOf(
        OnboardingModel(
            title = R.string.onboarding_title_1,
            image = R.drawable.ic_placeholder_illustration,
        ),
        OnboardingModel(
            title = R.string.onboarding_title_2,
            image = R.drawable.ic_placeholder_illustration,
        ),
    )
}
