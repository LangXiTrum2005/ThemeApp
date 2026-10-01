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

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStateAtLeast
import androidx.viewbinding.ViewBinding
import com.adshub.monetization.AdsController
import com.adshub.monetization.config.ConfigManager
import com.adshub.monetization.model.RewardConfig
import thinklife.prism.wallpaper.widget.theme.ads.AdsBootstrap
import thinklife.prism.wallpaper.widget.theme.core.extensions.collectWithLifecycle
import thinklife.prism.wallpaper.widget.theme.core.utils.NetworkTracker
import thinklife.prism.wallpaper.widget.theme.presentation.dialog.NetworkDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    @javax.inject.Inject
    lateinit var sharedPref: thinklife.prism.wallpaper.widget.theme.data.local.SharedPref

    private var nullableBinding: VB? = null
    protected val binding: VB
        get() = checkNotNull(nullableBinding) { "ViewBinding is only valid after onCreate." }


    open val registerEventBus: Boolean = false

    open fun bindingView(): VB? = null

    open fun inflateBinding(): VB =
        bindingView() ?: throw UnsupportedOperationException("Activity must implement inflateBinding or bindingView")

    open fun navigateTo(intent: android.content.Intent) {
        startActivity(intent)
    }

    private var networkTracker: NetworkTracker? = null
    private var networkDialog: NetworkDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // light(): icon thanh hệ thống màu TỐI, hợp nền sáng. Đổi app sang chủ đề tối thì đổi
        // hai dòng này sang SystemBarStyle.dark(...) cùng lúc với bảng màu trong colors.xml.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        nullableBinding = inflateBinding()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Ads full-screen của SDK bật immersive (setSystemUiVisibility 5894) nên status bar bị
            // ẩn -> inset top của màn phía sau về 0 -> cả màn nhảy lên rồi nhảy xuống khi ad đóng.
            // Lấy chiều cao status bar KỂ CẢ lúc nó đang ẩn để padding đứng yên.
            val stableTop = insets
                .getInsetsIgnoringVisibility(WindowInsetsCompat.Type.systemBars()).top
            v.setPadding(systemBars.left, stableTop, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }
        hideBottomNavigation()
        networkTracker = NetworkTracker(this, lifecycle)
        if (registerEventBus) {
            org.greenrobot.eventbus.EventBus.getDefault().register(this)
        }
        registerBackPressedCallback()
        initView(savedInstanceState)
        initConfig(savedInstanceState)
        initListener()
        initObserver()

        networkTracker?.isConnectedFlow?.collectWithLifecycle(this) { isConnected ->
            if (isConnected) dismissNetworkDialog() else showNetworkDialog()
            onNetworkChanged(isConnected)
        }
    }

    open fun initView(savedInstanceState: Bundle?) = Unit

    open fun initConfig(savedInstanceState: Bundle?) = Unit

    open fun initListener() = Unit

    open fun initObserver() = Unit

    /** Gọi mỗi khi trạng thái mạng thay đổi */
    protected open fun onNetworkChanged(isConnected: Boolean) = Unit

    open fun onHandleBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 0) {
            finish()
        } else {
            supportFragmentManager.popBackStack()
        }
    }

    override fun onDestroy() {
        if (registerEventBus) {
            org.greenrobot.eventbus.EventBus.getDefault().unregister(this)
        }
        nullableBinding = null
        super.onDestroy()
    }

    private fun registerBackPressedCallback() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onHandleBackPressed()
                }
            },
        )
    }

    private fun showNetworkDialog() {
        if (isFinishing || supportFragmentManager.isStateSaved) return
        (supportFragmentManager.findFragmentByTag(NetworkDialog.TAG) as? NetworkDialog)?.let {
            networkDialog = it
            return
        }
        NetworkDialog.newInstance().also { dialog ->
            networkDialog = dialog
            dialog.show(supportFragmentManager, NetworkDialog.TAG)
        }
    }

    private fun dismissNetworkDialog() {
        val dialog = networkDialog
            ?: supportFragmentManager.findFragmentByTag(NetworkDialog.TAG) as? NetworkDialog
        dialog?.dismissAllowingStateLoss()
        networkDialog = null
    }

    // region AdsHub =========================================================

    /**
     * MỌI màn phải override bằng hằng số trong AdsScreen — kể cả màn KHÔNG có ads: bindScreen set
     * currentScreenId (safety rules, not_on_screens, fromScreen đúng), kích preload màn kế và tự
     * logScreenView. `null` CHỈ dành cho Splash (bind muộn theo luồng cold start riêng).
     */
    protected open val adsScreenId: String? = null

    /** Splash đặt false để tự bind theo luồng cold start. */
    protected open val bindAdsScreenOnCreate = true

    /** Fallback cold start khi process mở thẳng vào màn khác Splash (widget/notification). */
    protected open val autoStartAdsColdStart = true

    /** Chỉ Splash cho hiện form UMP. */
    protected open val allowAdsConsentForm = false

    /** Root để SDK quét slot — cả cây view của Activity, không phụ thuộc kiểu ViewBinding. */
    protected open val adsRootView: View
        get() = findViewById(android.R.id.content)

    /** Override để gán tag slot động trước khi bind (1 layout dùng cho nhiều màn). */
    protected open fun onPrepareAdsSlots() = Unit

    /** Chạy SAU onCreate của lớp con → view đã sẵn sàng để quét slot. */
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (bindAdsScreenOnCreate) bindAdsScreen()
        if (autoStartAdsColdStart) startAdsColdStartIfNeeded()
    }

    /**
     * Báo SDK user vào màn này. Đợi config nạp xong mới bind — bind sớm khi config chưa có là
     * slot chết im lặng.
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
            AdsController.bindScreen(screenId, adsRootView, this@BaseActivity)
        }
    }

    private fun startAdsColdStartIfNeeded() {
        if (AdsBootstrap.isColdStartDone) return
        lifecycleScope.launch {
            AdsBootstrap.awaitColdStart(this@BaseActivity, requestConsentForm = allowAdsConsentForm)
        }
    }

    /**
     * Kéo cò quảng cáo. onFinished LUÔN được gọi (kể cả khi ads bị chặn bởi capping/VIP/hết đạn)
     * → đặt toàn bộ logic điều hướng vào đó, user không bao giờ kẹt.
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
     * BẮT BUỘC bọc mọi điều hướng trong onFinished bằng hàm này: SDK gọi onFinished ngay lúc ad
     * đóng, khi FragmentManager có thể còn "state saved" — commit() lúc đó ném IllegalStateException
     * và SDK NUỐT exception → app đứng im không lỗi gì.
     */
    protected fun runWhenResumed(action: () -> Unit) {
        lifecycleScope.launch {
            lifecycle.withStateAtLeast(Lifecycle.State.RESUMED) { action() }
        }
    }

    // endregion AdsHub ======================================================

    protected fun hideBottomNavigation() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
