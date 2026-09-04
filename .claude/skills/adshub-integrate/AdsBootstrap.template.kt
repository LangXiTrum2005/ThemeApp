package __APP_PACKAGE__.ads

// ============================================================================
// TEMPLATE — copy vào app/src/main/java/<package>/ads/AdsBootstrap.kt
// Thay __APP_PACKAGE__ bằng package thật.
// Toàn bộ tương tác với AdsHub SDK đi qua file này — màn hình KHÔNG import SDK trực tiếp
// (trừ base class). Nguồn: AdsHub Integration Kit (thư mục chứa AI_INTEGRATION_PLAYBOOK.md).
// ============================================================================

import android.app.Activity
import android.app.Application
import android.content.pm.ApplicationInfo
import android.view.ViewGroup
import com.adshub.monetization.AdLogger
import com.adshub.monetization.AdsController
import com.adshub.monetization.config.ConfigManager
import com.adshub.monetization.engine.cache.PreloadEngine
import com.adshub.monetization.engine.inline.AdsSlotScanner
import com.adshub.monetization.engine.tracking.TrackingManager
import com.adshub.monetization.init.AdNetworkInitializer
import com.adshub.monetization.init.CmpManager
import com.adshub.monetization.model.NativeAdStyleConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings // BOM cũ hơn 32.7 thì đổi sang gói .ktx
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds
import __APP_PACKAGE__.BuildConfig
import __APP_PACKAGE__.R

/**
 * Cầu nối duy nhất giữa app và AdsHub SDK.
 *
 * Luồng chuẩn:
 *  1. Application.onCreate()  -> [onApplicationCreate]   (đồng bộ, trước mọi coroutine)
 *  2. Splash                  -> [awaitColdStart] -> [awaitPreloadReady] -> bindAdsScreen()
 *                                -> [awaitAdReadyFor] -> triggerAds { điều hướng }
 *  3. Màn thường              -> BaseActivity/BaseFragment tự bindAdsScreen()
 */
object AdsBootstrap {

    /** Key Remote Config duy nhất — giá trị là NGUYÊN chuỗi JSON cùng schema với assets/ads_config.json. */
    const val REMOTE_CONFIG_KEY_ADS = "ads_config_v1"
    private const val REMOTE_CONFIG_TIMEOUT_MS = 5_000L

    private val coldStartMutex = Mutex()
    private var isConsentRequested = false
    private var isRemoteConfigApplied = false

    @Volatile
    var isColdStartDone = false
        private set

    // =========================================================================
    // CHẶNG 1 — Application.onCreate() (ĐỒNG BỘ, đúng thứ tự 5 bước)
    // =========================================================================
    fun onApplicationCreate(application: Application, isOnboardingCompleted: Boolean) {
        // 1. Analytics bridge — set TRƯỚC init để không rơi event đầu tiên.
        TrackingManager.isDebugMode = BuildConfig.DEBUG
        TrackingManager.setProvider(AdsAnalyticsProvider(application))

        // 2. Nạp assets/ads_config.json + bật AppLifecycleTracker / AppOpenManager /
        //    GlobalScreenTracker. appFlyerSdkKey = null nếu app không dùng AppsFlyer.
        AdsController.init(application, BuildConfig.APPSFLYER_DEV_KEY)

        // 3. PurchasedUserGate — truyền cờ build-time (flavor paid) hoặc trạng thái IAP đã verify.
        //    KHÔNG truyền cờ VIP tự chế trong SharedPreferences.
        AdsController.setPurchasedUserStatus(BuildConfig.IS_PAID_TIER)

        // 4. App multi-Activity BẮT BUỘC tắt Zero-Flash: khi bật, SDK gọi onFinished ngay lúc
        //    ad hiện để đổi màn ngầm phía sau — app multi-Activity finish() Activity đang show ad
        //    → callback đóng ad không bao giờ về, isFullScreenAdShowing kẹt true, mọi trigger/slot
        //    sau đó bị chặn vĩnh viễn. Chỉ bật (true) với app single-Activity (Compose/Fragment).
        AdsController.useZeroFlashNavigation = false

        // 5. User property cho Rule Engine (routing phiên đầu / phiên sau).
        updateOnboardingState(isOnboardingCompleted)

        // 6. Style Native toàn cục — đổi màu theo Design System của app.
        AdsController.globalNativeAdStyle = NativeAdStyleConfig(
            backgroundColorRes = R.color.ad_native_bg,
            cornerRadiusDp = 16f,
            elevationDp = 0f,
            borderSizeDp = 0f,
            borderColorRes = R.color.ad_native_bg,
            paddingLeftDp = 16f,
            paddingTopDp = 12f,
            paddingRightDp = 16f,
            paddingBottomDp = 16f,
            textColorPrimaryRes = R.color.ad_native_text_primary,
            textColorSecondaryRes = R.color.ad_native_text_secondary,
            buttonBackgroundColorRes = R.color.ad_native_button_bg,
            buttonTextColorRes = R.color.ad_native_button_text,
            buttonCornerRadiusDp = 16f,
            buttonHeightDp = 48f,
        )
    }

    // =========================================================================
    // CHẶNG 2 — Cold start ở Splash. Thứ tự BẮT BUỘC:
    // UMP -> Remote Config -> initNetworks -> PreloadEngine.onAppStartOrConsentReady()
    // (đổi thứ tự sẽ dính race condition: preload chạy với config cũ)
    // =========================================================================
    suspend fun awaitColdStart(activity: Activity, requestConsentForm: Boolean = true) =
        coldStartMutex.withLock {
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

                remoteConfigJob?.await()?.let { json ->
                    isRemoteConfigApplied = true
                    AdsController.updateRemoteConfig(json)
                }

                withContext(Dispatchers.IO) { AdNetworkInitializer.initNetworks(activity.applicationContext) }
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

    // =========================================================================
    // User property (Rule Engine)
    // =========================================================================

    /** Gọi mỗi khi biết trạng thái onboarding (Application, Splash). Chỉ hợp lệ SAU AdsController.init(). */
    fun updateOnboardingState(isCompleted: Boolean) {
        AdsController.setUserProperty("is_onboarding_completed", isCompleted)
        // Chốt 1 lần cho cả phiên — phân biệt "phiên đầu" / "phiên sau" ổn định trong suốt session.
        if (!isSessionPropertyLocked) {
            isSessionPropertyLocked = true
            AdsController.setUserProperty("is_onboarding_completed_session", isCompleted)
        }
    }

    /** Gọi đúng lúc user hoàn thành onboarding. KHÔNG đụng biến _session (giữ nguyên hết phiên). */
    fun markOnboardingCompleted() {
        AdsController.setUserProperty("is_onboarding_completed", true)
    }

    private var isSessionPropertyLocked = false

    // =========================================================================
    // Slot render tay — cho màn nhiều trạng thái (Language bước 2, RecyclerView...)
    // Container KHÔNG gắn tag ads_slot: trong XML (tránh bindScreen render slot đang ẩn,
    // đốt fill + impression cho quảng cáo user chưa thấy).
    // =========================================================================

    /** Báo SDK user đã vào màn/trạng thái [screenId] — kích các preload_triggers screen_enter
     *  và log screen view (giữ nguyên quy tắc "log đủ full tất cả các màn" cho cả trạng thái
     *  render tay). BẮT BUỘC gọi trước [renderSlotNow] với trạng thái render tay.
     *  Log hoãn qua runOnAdClosed giống hệt bindScreen — chỉ đếm khi user thật sự thấy màn. */
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

    /** Gọi ở onDestroyView / onRecycle để SDK không render vào view đã chết.
     *  CHỈ xoá khỏi slotRegistry — KHÔNG gỡ slot khỏi danh sách quét của màn (foundSlots trong
     *  AdsSlotScanner). Muốn slot ngừng HẲN (màn nhiều trạng thái, ẩn slot cũ đi) phải gỡ luôn
     *  container khỏi cây view: (container.parent as? ViewGroup)?.removeView(container) — view chỉ
     *  gone() vẫn isAttachedToWindow nên refresh_policy còn nạp ad vào đó mỗi lần resume. */
    fun releaseSlot(slotId: String) {
        AdsSlotScanner.removeSlotFromRegistry(slotId)
    }

    // =========================================================================
    // Remote Config: 1 key duy nhất ads_config_v1, giá trị = nguyên chuỗi JSON
    // (SDK không tự đọc Firebase — app fetch rồi bơm vào updateRemoteConfig)
    // =========================================================================
    private suspend fun fetchAdsConfigJson(): String? =
        withTimeoutOrNull(REMOTE_CONFIG_TIMEOUT_MS.milliseconds) {
            runCatching {
                val remoteConfig = FirebaseRemoteConfig.getInstance()
                remoteConfig.setConfigSettingsAsync(
                    remoteConfigSettings {
                        minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600
                        fetchTimeoutInSeconds = REMOTE_CONFIG_TIMEOUT_MS / 1000
                    }
                ).await()
                remoteConfig.fetchAndActivate().await()
                remoteConfig.getString(REMOTE_CONFIG_KEY_ADS)
                    .takeIf { it.isNotBlank() && it != "{}" }
                    ?.also { AdLogger.i("AdsBootstrap", "Đã nhận ads_config từ Remote Config") }
            }.getOrNull()
        }
}
