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

package com.onetabb.template.android

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp
import com.onetabb.template.android.ads.AdsBootstrap
import com.onetabb.template.android.data.local.SharedPref
import com.onetabb.template.android.util.Constants
import com.onetabb.template.android.util.remoteconfig.RatingRemoteConfig
import com.ontabb.rate.OneTabbRate
import com.ontabb.rate.RateConfig
import com.ontabb.rate.RateStyle
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    companion object {

        @SuppressLint("StaticFieldLeak")
        private lateinit var app: App

        fun instance(): App = app

        fun appContext(): Context = instance().applicationContext
    }

    @Inject
    lateinit var sharedPref: SharedPref

    override fun onCreate() {
        super.onCreate()
        app = this
        FirebaseApp.initializeApp(this)
        // `isNeedGotoWalkthrough` = true nghĩa là user CHƯA xong onboarding (lật ở trang cuối
        // OnboardingActivity, cùng chỗ gọi markOnboardingCompleted).
        AdsBootstrap.onApplicationCreate(
            application = this,
            isOnboardingCompleted = !sharedPref.isNeedGotoWalkthrough,
        )
        // Module đánh giá: nạp cấu hình rồi đếm lần mở app (bung ở lần mở thứ 2, và ở lần quay
        // lại Home thứ 2/4/6 — xem OneTabbRate).
        OneTabbRate.init(
            RateConfig(
                contactEmail = Constants.EMAIL_SUPPORT,
                storeId = BuildConfig.STORE_ID,
                // Đổi sang RateStyle.BOTTOM_SHEET nếu muốn khối đánh giá trượt lên từ mép dưới.
                style = RateStyle.DIALOG,
                isEnabled = true,
                fontRegular = R.font.nunito_medium,
                fontBold = R.font.nunito_semibold,
            ),
        )
        OneTabbRate.recordAppOpen(this)
        // Áp giá trị Remote Config đã cache từ phiên trước. Bản mới fetch ở Splash sẽ ghi đè
        // (xem LaunchActivity), nhưng nhờ dòng này phiên hiện tại không phải chạy bằng mặc định
        // trong code cho tới lúc fetch xong.
        RatingRemoteConfig.apply()

        // TODO(project): khởi tạo các SDK riêng khác của app tại đây (analytics phụ, preload
        // Lottie...). Giữ AdsBootstrap.onApplicationCreate là lệnh CHẠY ĐẦU TIÊN.
    }
}
