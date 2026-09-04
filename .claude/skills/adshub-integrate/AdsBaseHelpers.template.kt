package __APP_PACKAGE__.core.base

// ============================================================================
// TEMPLATE — base class helpers cho app XML Views (Compose xem DEV_GUIDE mục 3/4.3).
// Thay __APP_PACKAGE__ bằng package thật.
//
// 2 cách dùng:
//  A. Project CHƯA có base class → copy nguyên file, cho Activity/Fragment kế thừa.
//  B. Project ĐÃ có BaseActivity/BaseFragment riêng → copy nguyên "region AdsHub"
//     bên dưới vào base có sẵn (không đổi hành vi cũ; chỉ thêm member).
//
// Yêu cầu dependency (hầu hết project đã có): androidx.lifecycle:lifecycle-runtime-ktx >= 2.6
// ============================================================================

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStateAtLeast
import com.adshub.monetization.AdsController
import com.adshub.monetization.config.ConfigManager
import com.adshub.monetization.model.RewardConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import __APP_PACKAGE__.ads.AdsBootstrap

/**
 * QUY TẮC PHỦ SÓNG (bắt buộc): MỌI màn — kể cả màn KHÔNG có ads — đều override
 * [adsScreenId] bằng hằng số trong AdsScreen, để log đủ full tất cả các màn.
 * `null` CHỈ dành cho Splash (bind muộn theo luồng riêng, xem playbook Phase 4.4).
 */
abstract class BaseAdsActivity : AppCompatActivity() {

    // region AdsHub =========================================================

    /** MỌI màn phải override (đủ full tất cả các màn). null chỉ dành cho Splash. */
    protected open val adsScreenId: String? = null

    /** Splash đặt false để tự bind theo luồng cold start. */
    protected open val bindAdsScreenOnCreate = true

    /** Fallback cold start khi process mở thẳng vào màn khác Splash (widget/notification). */
    protected open val autoStartAdsColdStart = true

    /** Chỉ Splash cho hiện form UMP. */
    protected open val allowAdsConsentForm = false

    /** Root để SDK quét slot — mặc định cả cây view của Activity, không phụ thuộc ViewBinding. */
    protected open val adsRootView: View
        get() = findViewById(android.R.id.content)

    /** Override để gán tag slot động trước khi bind (1 layout dùng cho nhiều màn). */
    protected open fun onPrepareAdsSlots() {}

    /** Chạy SAU khi onCreate của lớp con xong (setContentView đã gọi) → view đã sẵn sàng để quét. */
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (bindAdsScreenOnCreate) bindAdsScreen()
        if (autoStartAdsColdStart) startAdsColdStartIfNeeded()
    }

    /**
     * Báo SDK user vào màn này: quét slot inline + set currentScreenId (safety rules)
     * + kích preload screen_enter + tự logScreenView. Đợi config nạp xong mới bind —
     * bind sớm khi config chưa có là slot chết im lặng.
     */
    protected fun bindAdsScreen() {
        val screenId = adsScreenId ?: return
        if (ConfigManager.isInitialConfigLoaded) {
            onPrepareAdsSlots()
            AdsController.bindScreen(screenId, adsRootView, this)
            return
        }
        lifecycleScope.launch {
            while (!ConfigManager.isInitialConfigLoaded) delay(50L)
            if (isFinishing || isDestroyed) return@launch
            onPrepareAdsSlots()
            AdsController.bindScreen(screenId, adsRootView, this@BaseAdsActivity)
        }
    }

    private fun startAdsColdStartIfNeeded() {
        if (AdsBootstrap.isColdStartDone) return
        lifecycleScope.launch {
            AdsBootstrap.awaitColdStart(this@BaseAdsActivity, requestConsentForm = allowAdsConsentForm)
        }
    }

    /**
     * Kéo cò quảng cáo. onFinished LUÔN được gọi (kể cả khi ads bị chặn bởi
     * capping/VIP/hết đạn) → đặt toàn bộ logic điều hướng vào đó, user không bao giờ kẹt.
     */
    protected fun triggerAds(
        triggerId: String,
        fromScreen: String? = adsScreenId,
        onRewardEarned: ((List<RewardConfig>) -> Unit)? = null,
        onFinished: () -> Unit,
    ) {
        AdsController.trigger(
            activity = this,
            triggerId = triggerId,
            fromScreen = fromScreen,
            onRewardEarned = onRewardEarned,
            onFinished = onFinished,
        )
    }

    /**
     * BẮT BUỘC bọc mọi điều hướng trong onFinished bằng hàm này: SDK gọi onFinished
     * ngay lúc ad đóng, khi FragmentManager có thể còn "state saved" — commit() lúc đó
     * ném IllegalStateException và SDK NUỐT exception → app đứng im không lỗi gì.
     */
    protected fun runWhenResumed(action: () -> Unit) {
        lifecycleScope.launch {
            lifecycle.withStateAtLeast(Lifecycle.State.RESUMED) { action() }
        }
    }

    // endregion AdsHub ======================================================
}

/**
 * Bind ở onResume (KHÔNG phải onViewCreated) vì ViewPager2/tab tạo view trước khi
 * user thật sự nhìn thấy — bind sớm là đốt fill + impression cho màn chưa hiện.
 * Cờ [hasBoundAds] chống bind trùng mỗi lần resume; reset khi view huỷ để
 * back-stack quay lại vẫn bind lại được.
 */
abstract class BaseAdsFragment : Fragment() {

    // region AdsHub =========================================================

    /** MỌI màn phải override (đủ full tất cả các màn) — kể cả fragment không có ads. */
    protected open val adsScreenId: String? = null

    /** Override để gán tag slot động trước khi bind. */
    protected open fun onPrepareAdsSlots() {}

    private var hasBoundAds = false

    override fun onResume() {
        super.onResume()
        if (!hasBoundAds) {
            hasBoundAds = true
            bindAdsScreen()
        }
    }

    override fun onDestroyView() {
        hasBoundAds = false
        super.onDestroyView()
    }

    protected fun bindAdsScreen() {
        val screenId = adsScreenId ?: return
        val root = view ?: return
        if (ConfigManager.isInitialConfigLoaded) {
            onPrepareAdsSlots()
            AdsController.bindScreen(screenId, root, viewLifecycleOwner)
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            while (!ConfigManager.isInitialConfigLoaded) delay(50L)
            val aliveRoot = view ?: return@launch
            onPrepareAdsSlots()
            AdsController.bindScreen(screenId, aliveRoot, viewLifecycleOwner)
        }
    }

    protected fun triggerAds(
        triggerId: String,
        fromScreen: String? = adsScreenId,
        onRewardEarned: ((List<RewardConfig>) -> Unit)? = null,
        onFinished: () -> Unit,
    ) {
        AdsController.trigger(
            activity = requireActivity(),
            triggerId = triggerId,
            fromScreen = fromScreen,
            onRewardEarned = onRewardEarned,
            onFinished = onFinished,
        )
    }

    /** Xem chú thích ở BaseAdsActivity.runWhenResumed — fragment dùng viewLifecycleOwner. */
    protected fun runWhenResumed(action: () -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.withStateAtLeast(Lifecycle.State.RESUMED) { action() }
        }
    }

    // endregion AdsHub ======================================================
}
