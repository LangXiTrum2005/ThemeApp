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

package thinklife.prism.wallpaper.widget.theme.data.provider

import thinklife.prism.wallpaper.widget.theme.R
import thinklife.prism.wallpaper.widget.theme.presentation.onboarding.model.OnboardingModel

/**
 * Nội dung 2 trang onboarding onb2 / onb3.
 *
 * TODO(project): thay title + ảnh thật. Thêm trang mới phải khai thêm màn `onbN_view` + slot
 * trong `assets/ads_config.json` và hằng số trong AdsScreen/AdsSlot/AdsTrigger.
 */
object OnboardingProvider {

    val onboardingList = listOf(
        OnboardingModel(
            index = 0,
            title = R.string.onboarding_title_1,
            image = R.drawable.obd_img_onboarding_1,
            description = R.string.onboarding_desc_1,
        ),
        OnboardingModel(
            index = 1,
            title = R.string.onboarding_title_2,
            image = R.drawable.obd_img_onboarding_2,
            description = R.string.onboarding_desc_2,
        ),
    )
}
