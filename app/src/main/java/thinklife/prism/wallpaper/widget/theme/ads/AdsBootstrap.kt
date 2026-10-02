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

package thinklife.prism.wallpaper.widget.theme.ads

import thinklife.prism.wallpaper.widget.theme.R
import android.app.Activity
import android.app.Application
import android.content.pm.ApplicationInfo
import android.view.ViewGroup
import com.adshub.monetization.AdLogger
import com.adshub.monetization.AdsController
import com.adshub.monetization.config.ConfigManager
import com.adshub.monetization.engine.cache.PreloadEngine
import com.adshub.monetization.engine.inline.AdsSlotScanner
import com.adshub.monetization.engine.inline.NativeAdRenderer
import com.adshub.monetization.engine.inline.NativeSkeletonLayout
import com.adshub.monetization.engine.tracking.TrackingManager
import com.adshub.monetization.init.AdNetworkInitializer
import com.adshub.monetization.init.CmpManager
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import thinklife.prism.wallpaper.widget.theme.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.apply
import kotlin.time.Duration.Companion.milliseconds

/**
 * Cầu nối duy nhất giữa app và AdsHub SDK — màn hình KHÔNG import SDK trực tiếp (trừ base class).
 *
 * Luồng chuẩn:
 *  1. Application.onCreate() -> [onApplicationCreate]  (đồng bộ, trước mọi coroutine)
 *  2. Splash -> [awaitColdStart] -> [awaitPreloadReady] -> bindAdsScreen()
 *              -> [awaitAdReadyFor] -> triggerAds { điều hướng }
 *  3. Màn thường -> BaseActivity/BaseFragment tự bindAdsScreen()
 */
object AdsBootstrap {

    /**
     * Key Remote Config duy nhất — giá trị là NGUYÊN chuỗi JSON cùng schema với
     * assets/ads_config.json.
     *
     * TODO(project): đổi thành key của app (mỗi app một param riêng trên Firebase Console).
     */
    const val REMOTE_CONFIG_KEY_ADS = "ads_config_v100"
    private const val REMOTE_CONFIG_TIMEOUT_MS = 6_000L

    private val coldStartMutex = Mutex()
    private var isConsentRequested = false
    private var isRemoteConfigApplied = false

    /** Trạng thái FTUE chốt ở Application, dùng lại khi cold start chạy từ màn không phải Splash. */
    private var isOnboardingCompletedAtStart = false

    @Volatile
    var isColdStartDone = false
        private set

    // =========================================================================
    // CHẶNG 1 — Application.onCreate() (ĐỒNG BỘ, đúng thứ tự)
    // =========================================================================
    fun onApplicationCreate(application: Application, isOnboardingCompleted: Boolean) {
        // 1. Analytics bridge — set TRƯỚC init để không rơi event đầu tiên.
        TrackingManager.isDebugMode = BuildConfig.DEBUG
        TrackingManager.setProvider(AdsAnalyticsProvider(application))

        // 2. Nạp assets/ads_config.json + bật AppLifecycleTracker / AppOpenManager / GlobalScreenTracker.
        //    Tham số thứ 2 là AppsFlyer dev key — app không dùng AppsFlyer thì truyền null.
        //    TODO(project): truyền dev key thật (nên qua buildConfigField, không hardcode).
        AdsController.init(application, BuildConfig.APPSFLYER_DEV_KEY)

        // 3. PurchasedUserGate — template chỉ có bản free nên luôn false.
        //    TODO(project): app có IAP thì truyền trạng thái mua ĐÃ VERIFY, không phải cờ tự chế.
        AdsController.setPurchasedUserStatus(false)

        // 4. App multi-Activity BẮT BUỘC tắt Zero-Flash: bật sẽ làm callback đóng ad không bao giờ
        //    quay về khi Activity show ad bị finish() → mọi trigger/slot sau đó bị chặn vĩnh viễn.
        AdsController.useZeroFlashNavigation = false

        // 5. Trạng thái FTUE cho Rule Engine. Set ngay ở đây vì app có thể vào thẳng màn khác
        //    Splash (notification/shortcut) mà không đi qua luồng cold start.
        setOnboardingUserProperties(isOnboardingCompleted)
    }

    // =========================================================================
    // CHẶNG 2 — Cold start ở Splash. Thứ tự BẮT BUỘC:
    // UMP -> Remote Config -> initNetworks -> PreloadEngine.onAppStartOrConsentReady()
    // =========================================================================
    suspend fun awaitColdStart(
        activity: Activity,
        isOnboardingCompleted: Boolean = isOnboardingCompletedAtStart,
        requestConsentForm: Boolean = true,
    ) = coldStartMutex.withLock {
        if (isColdStartDone) return@withLock
        coroutineScope {
            // Remote Config tải SONG SONG với UMP (không phụ thuộc nhau).
            val remoteConfigJob =
                if (!isRemoteConfigApplied) async(Dispatchers.IO) { fetchAdsConfigJson() } else null

            if (requestConsentForm && !isConsentRequested) {
                isConsentRequested = true
                val isDebuggable =
                    (activity.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
                CmpManager.requestConsent(activity, isDebuggable)
            }

            // Khai báo lại trạng thái user cho Rule Engine trước khi bơm config mới.
            setOnboardingUserProperties(isOnboardingCompleted)

            remoteConfigJob?.await()?.let { json ->
                isRemoteConfigApplied = true
                AdsController.updateRemoteConfig(completeFeedSlotConfig(json))
            }

            withContext(Dispatchers.IO) {
                AdsPrivacyUs.apply(activity.applicationContext)
                AdNetworkInitializer.initNetworks(activity.applicationContext) }
            delay(300L.milliseconds) // nhường một nhịp cho I/O ghi config an toàn
            PreloadEngine.onAppStartOrConsentReady()
            isColdStartDone = true
        }
    }

    // =========================================================================
    // Helper cho Splash
    // =========================================================================

    /** Đợi PreloadEngine kích hoạt. CỐ Ý không timeout: user EU có thể mất 10-20s đọc form UMP. */
    suspend fun awaitPreloadReady() {
        while (!PreloadEngine.hasTriggeredAppStart) delay(100L.milliseconds)
    }

    /** Đợi đạn của trigger [triggerId]. Inventory tra từ config lúc runtime — KHÔNG hardcode. */
    suspend fun awaitAdReadyFor(triggerId: String): Boolean {
        val inventoryId = ConfigManager.currentConfig.triggers[triggerId]
            ?.takeIf { it.enabled }?.inventoryId?.takeIf { it.isNotBlank() } ?: return false
        return AdsController.waitForInventory(inventoryId)
    }

    /** Chủ động preload rồi đợi — dùng cho inventory có preload_triggers rỗng (fallback sau Inter). */
    suspend fun preloadAndAwait(inventoryId: String): Boolean {
        AdsController.preload(inventoryId)
        return AdsController.waitForInventory(inventoryId)
    }

    /**
     * Hoãn [action] tới khi không còn ads toàn màn hình nào đang hiện. Splash dùng để finish()
     * MUỘN — finish() ngay sau startActivity khi SDK còn Dialog gắn vào cửa sổ này là WindowLeaked.
     */
    fun runOnAdClosed(key: String, action: () -> Unit) = AdsController.runOnAdClosed(key, action)

    /** Process bị OS kill rồi khôi phục — không phải cold start thật, đừng chặn nhầm resume ad. */
    fun notifyAppRestoredFromBackground() = AdsController.notifyAppRestoredFromBackground()

    /**
     * Đang có quảng cáo toàn màn hình hiện/chờ hiện. App dùng để không đè màn hình riêng của mình
     * lên trên ads khi user quay lại từ Inter / Reward / Native full-screen.
     */
    fun isFullScreenAdActive(): Boolean = AdsController.isFullScreenAdShowing || AdsController.isFullScreenAdPending

    // =========================================================================
    // User property (Rule Engine)
    // =========================================================================

    /**
     * Chốt trạng thái FTUE đầu mỗi phiên. Hai biến cùng giá trị tại thời điểm này; chúng chỉ tách
     * nhau khi user hoàn thành onboarding NGAY TRONG phiên hiện tại: lúc đó chỉ
     * [markOnboardingCompleted] chạy nên `is_onboarding_completed` thành true còn
     * `is_onboarding_completed_session` giữ false tới phiên sau. Nhờ đó phân biệt được
     * "mở app lần 1" và "mở app lần 2 trở đi" — xem load_conditions của
     * inv_inter_splash / inv_inter_splash_2nd và inv_inter_home / inv_inter_home_2nd.
     */
    private fun setOnboardingUserProperties(isOnboardingCompleted: Boolean) {
        isOnboardingCompletedAtStart = isOnboardingCompleted
        AdsController.setUserProperty("is_onboarding_completed", isOnboardingCompleted)
        AdsController.setUserProperty("is_onboarding_completed_session", isOnboardingCompleted)
    }

    /**
     * Gọi đúng lúc user bấm nút kết thúc onboarding.
     * CỐ Ý chỉ đổi `is_onboarding_completed`, KHÔNG đụng `is_onboarding_completed_session`.
     */
    fun markOnboardingCompleted() {
        AdsController.setUserProperty("is_onboarding_completed", true)
    }

    // =========================================================================
    // Slot render tay — cho màn nhiều trạng thái (Language bước 2, Survey sau khi chọn option)
    // Container KHÔNG gắn tag ads_slot: trong XML (tránh bindScreen render slot đang ẩn).
    // =========================================================================

    /**
     * Báo SDK user đã vào màn/trạng thái [screenId] — kích preload_triggers screen_enter và log
     * screen view. BẮT BUỘC gọi trước [renderSlotNow] với trạng thái render tay.
     */
    fun notifyScreenEnter(screenId: String) {
        PreloadEngine.onScreenEnter(screenId)
        AdsController.runOnAdClosed("log_screen_$screenId") {
            AdsController.logScreenView(screenId)
        }
    }

    /** Render slot vào [container] ngay bây giờ. Trả false nếu slot tắt/không có config. */
    fun renderSlotNow(screenId: String, slotId: String, container: ViewGroup): Boolean {
        val slotConfig = ConfigManager.currentConfig.slots[slotId]
        if (slotConfig == null || !slotConfig.enabled) {
            AdLogger.i("AdsBootstrap", "renderSlotNow bỏ qua: slot '$slotId' tắt hoặc không có config")
            return false
        }
        AdsSlotScanner.processSingleSlot(screenId, slotId, container)
        return true
    }

    /** Gọi ở onDestroyView / onRecycle để SDK không render vào view đã chết. */
    fun releaseSlot(slotId: String) {
        AdsSlotScanner.removeSlotFromRegistry(slotId)
    }

    fun isSlotOwnedBy(slotId: String, container: ViewGroup): Boolean =
        AdsSlotScanner.getSlotContainer(slotId) === container

    fun availableFeedSlot(baseSlotId: String): String? {
        val config = ConfigManager.currentConfig
        if (!config.global.adsEnabled || config.slots[baseSlotId]?.enabled != true) return null
        return AdsSlot.feedSlots(baseSlotId).firstOrNull { id ->
            val slot = config.slots[id]
            slot?.enabled == true && config.inventories[slot.inventoryId]?.enabled == true &&
                AdsSlotScanner.getSlotContainer(id) == null &&
                AdsSlot.feedSlotGroups.flatten().none { other ->
                    config.slots[other]?.inventoryId == slot.inventoryId &&
                        AdsSlotScanner.getSlotContainer(other) != null
                }
        }
    }

    fun nativeSlotTimeoutMs(slotId: String): Long {
        val config = ConfigManager.currentConfig
        val inventoryId = config.slots[slotId]?.inventoryId
        return config.inventories[inventoryId]?.timeoutMs ?: DEFAULT_NATIVE_TIMEOUT_MS
    }

    // Skeleton cũng chứa NativeAdView, nên phải loại wrapper loading trước khi nhận là đã render.
    fun isNativeSlotRendered(container: ViewGroup): Boolean =
        container.childCount > 0 && container.getChildAt(0) !is NativeSkeletonLayout &&
            NativeAdRenderer.findNativeAdView(container) != null

    private const val DEFAULT_NATIVE_TIMEOUT_MS = 16_000L

    // =========================================================================
    // Remote Config: 1 key duy nhất, giá trị = nguyên chuỗi JSON
    // (SDK không tự đọc Firebase — app fetch rồi bơm vào updateRemoteConfig)
    // =========================================================================

    /** Callback thưởng có thể tới trước lúc đóng video; đợi onFinished rồi mới đổi UI. */
    fun triggerReward(
        activity: android.app.Activity,
        triggerId: String,
        screenId: String?,
        onAllowed: () -> Unit,
        onFinished: () -> Unit,
    ) {
        val config = ConfigManager.currentConfig
        val enabled = config.global.adsEnabled && config.triggers[triggerId]?.enabled == true
        var earned = false
        AdsController.trigger(
            activity = activity,
            triggerId = triggerId,
            fromScreen = screenId,
            onRewardEarned = { earned = true },
            onFinished = {
                onFinished()
                if (earned || !enabled) onAllowed()
                else android.widget.Toast.makeText(activity, R.string.ads_reward_unavailable, android.widget.Toast.LENGTH_SHORT).show()
            },
        )
    }

    /** Fetch chuỗi JSON cấu hình ads từ Firebase Remote Config. Trả về null nếu hỏng/rỗng. */
    private suspend fun fetchAdsConfigJson(): String? = withTimeoutOrNull(REMOTE_CONFIG_TIMEOUT_MS.milliseconds) {
        runCatching {
            val remoteConfig = FirebaseRemoteConfig.getInstance()
            remoteConfig.setConfigSettingsAsync(
                remoteConfigSettings {
                    minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600
                    fetchTimeoutInSeconds = REMOTE_CONFIG_TIMEOUT_MS / 1000
                },
            ).await()
            remoteConfig.fetchAndActivate().await()
            remoteConfig.getString(REMOTE_CONFIG_KEY_ADS)
                .takeIf { it.isNotBlank() && it != "{}" }
                ?.also { AdLogger.i("AdsBootstrap", "Đã nhận ads_config từ Remote Config") }
        }.getOrNull()
    }
}
