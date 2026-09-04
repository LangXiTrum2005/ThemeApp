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

import android.os.Bundle
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.onetabb.template.android.R
import com.onetabb.template.android.ads.AdsBootstrap
import com.onetabb.template.android.ads.AdsInventory
import com.onetabb.template.android.ads.AdsScreen
import com.onetabb.template.android.ads.AdsTrigger
import com.onetabb.template.android.core.base.BaseActivity
import com.onetabb.template.android.core.extensions.registerRequestNotiPermissionResult
import com.onetabb.template.android.core.permission.PermissionHelper
import com.onetabb.template.android.data.local.SharedPref
import com.onetabb.template.android.databinding.ActivityLaunchBinding
import com.onetabb.template.android.presentation.dialog.PermissionDialog
import com.onetabb.template.android.presentation.dialog.PermissionDialogArgs
import com.onetabb.template.android.presentation.language.LanguageActivity
import com.onetabb.template.android.presentation.main.MainActivity
import com.onetabb.template.android.util.remoteconfig.RatingRemoteConfig
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

/**
 * Splash — điểm vào duy nhất của app.
 *
 * Luồng: xin quyền thông báo -> cold start ads (UMP -> Remote Config -> init networks -> preload)
 * -> đợi tối thiểu [MIN_SPLASH_MS] -> Inter splash -> điều hướng Language (lần đầu) hoặc Main.
 */
@AndroidEntryPoint
class LaunchActivity : BaseActivity<ActivityLaunchBinding>() {

    private companion object {
        const val MIN_SPLASH_MS = 2_000L
        const val INTER_WAIT_MS = 12_000L
        const val NATIVE_LFO_WAIT_MS = 5_000L
    }

    private val viewModel: LaunchViewModel by viewModels()

    override fun inflateBinding() = ActivityLaunchBinding.inflate(layoutInflater)

    @Inject
    lateinit var sharedPref: SharedPref

    /**
     * Phiên "khách mới" — quyết định splash_view_1/2, inter_splash vs inter_splash_2nd.
     *
     * Mốc là ĐÃ QUA HẾT ONBOARDING (`isNeedGotoWalkthrough`, lật ở trang cuối OnboardingActivity),
     * KHÔNG phải `hasOpenedBefore` (lật ngay cuối Splash lần đầu). Dùng `hasOpenedBefore` thì user
     * thoát giữa chừng luồng LFO/onboarding, lần sau vào lại sẽ rơi vào splash_view_2 trong khi
     * vẫn đi tiếp màn Language — mà inv_native_lfo1 chỉ preload ở `screen_enter splash_view_1`,
     * nên native màn Language không có đạn.
     */
    private val isFirstSession by lazy { sharedPref.isNeedGotoWalkthrough }

    override val adsScreenId
        get() = if (isFirstSession) AdsScreen.SPLASH_VIEW_1 else AdsScreen.SPLASH_VIEW_2

    /** Config chưa nạp lúc onCreate — Splash tự bind ở bước 3 của luồng cold start. */
    override val bindAdsScreenOnCreate = false

    /** Splash tự drive cold start và ĐỢI kết quả, không để base class chạy ngầm. */
    override val autoStartAdsColdStart = false

    override val allowAdsConsentForm = true

    private var hasNavigated = false

    private val requestNotiPermissionLauncher = registerRequestNotiPermissionResult {
        // Ghi lại là ĐÃ bật hộp thoại hệ thống — mốc để lần sau biết user bị chặn vĩnh viễn hay
        // chỉ là chưa hỏi bao giờ. Xem PermissionHelper.
        PermissionHelper.markAsRequested(this, PermissionHelper.NOTIFICATION_PERMISSION)
        startSplashFlow()
    }

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
        PermissionDialog.setResultListener(supportFragmentManager, this) { isAccepted ->
            if (isAccepted) {
                PermissionHelper.openSettings(this, PermissionHelper.NOTIFICATION_PERMISSION)
            }
            // Dù user đi Settings hay bỏ qua, splash vẫn chạy tiếp — không ai bị kẹt ở đây.
            startSplashFlow()
        }
        requestNotificationPermission()
    }

    /**
     * Ba nhánh: đã có quyền thì đi tiếp; hệ thống còn cho hỏi thì bật hộp thoại của máy; đã bị
     * chặn vĩnh viễn thì gọi launcher cũng không hiện gì, nên giải thích rồi mở Settings.
     */
    private fun requestNotificationPermission() {
        val permission = PermissionHelper.NOTIFICATION_PERMISSION
        when {
            !PermissionHelper.isNotificationPermissionRequired() -> startSplashFlow()

            PermissionHelper.isGranted(this, permission) -> startSplashFlow()

            PermissionHelper.mustOpenSettings(this, permission) -> PermissionDialog.show(
                supportFragmentManager,
                PermissionDialogArgs(
                    icon = R.drawable.ic_permission_notification,
                    title = R.string.permission_notification_settings_title,
                    description = R.string.permission_notification_settings_description,
                    positiveText = R.string.permission_open_settings,
                ),
            )

            else -> runCatching { requestNotiPermissionLauncher.launch() }
                .onFailure { startSplashFlow() }
        }
    }

    private fun startSplashFlow() {
        lifecycleScope.launch {
            startLoading()
            val minSplash = async { delay(MIN_SPLASH_MS.milliseconds) } // chạy SONG SONG

            // UMP -> Remote Config -> initNetworks -> preload
            AdsBootstrap.awaitColdStart(
                activity = this@LaunchActivity,
                isOnboardingCompleted = !isFirstSession,
            )
            // Cold start vừa fetch + activate Remote Config — lấy luôn cấu hình bung rating.
            RatingRemoteConfig.apply()
            AdsBootstrap.awaitPreloadReady() // đợi PreloadEngine kích hoạt
            bindAdsScreen() // giờ mới bind -> bắn screen_enter
            minSplash.await() // đủ splash tối thiểu

            val trigger =
                if (isFirstSession) AdsTrigger.SPLASH_LEAVE_1 else AdsTrigger.SPLASH_LEAVE_2
            // Không chờ thì trigger() thấy cache rỗng và bỏ qua quảng cáo luôn.
            val hasSplashInter = withTimeoutOrNull(INTER_WAIT_MS.milliseconds) {
                AdsBootstrap.awaitAdReadyFor(trigger)
            } == true

            // Fallback: Inter không có đạn thì chủ động nạp Native của màn Language ngay, để slot
            // đầu tiên của luồng onboarding không rơi vào cache rỗng.
            if (!hasSplashInter && sharedPref.isNeedShowLanguage) {
                withTimeoutOrNull(NATIVE_LFO_WAIT_MS.milliseconds) {
                    AdsBootstrap.preloadAndAwait(AdsInventory.NATIVE_LFO1)
                }
            }

            triggerAds(trigger) { runWhenResumed { goToNextScreen() } }
        }
    }

    private fun goToNextScreen() {
        if (isFinishing || isDestroyed || hasNavigated) return
        hasNavigated = true
        viewModel.markOpened()

        val intent = if (sharedPref.isNeedShowLanguage) {
            LanguageActivity.newIntent(this)
        } else {
            MainActivity.newIntent(this)
        }
        startActivity(intent)
        // KHÔNG finish() ngay: SDK còn Dialog gắn vào cửa sổ này -> WindowLeaked.
        AdsBootstrap.runOnAdClosed("finish_splash") { finish() }
    }

    /** Thanh tiến trình giả — chạy tới 90% rồi dừng, màn đóng khi luồng ads xong. */
    private fun startLoading() {
        lifecycleScope.launch {
            var progress = 0
            while (progress < 90) {
                delay(50.milliseconds)
                progress = (progress + (1..3).random()).coerceAtMost(90)
                binding.progressSplash.setProgress(progress, true)
            }
        }
    }
}
