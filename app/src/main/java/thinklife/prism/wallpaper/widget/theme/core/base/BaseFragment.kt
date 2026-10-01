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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStateAtLeast
import androidx.viewbinding.ViewBinding
import com.adshub.monetization.AdsController
import com.adshub.monetization.config.ConfigManager
import com.adshub.monetization.model.RewardConfig
import thinklife.prism.wallpaper.widget.theme.core.extensions.collectWithLifecycle
import thinklife.prism.wallpaper.widget.theme.core.utils.NetworkTracker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    @javax.inject.Inject
    lateinit var sharedPref: thinklife.prism.wallpaper.widget.theme.data.local.SharedPref

    private var nullableBinding: VB? = null
    protected val binding: VB
        get() = checkNotNull(nullableBinding) { "ViewBinding is only valid between onCreateView and onDestroyView." }


    open val registerEventBus: Boolean = false

    open fun bindingView(): VB? = null

    open fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): VB =
        bindingView() ?: throw UnsupportedOperationException("Fragment must implement inflateBinding or bindingView")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        nullableBinding = inflateBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerBackPressedCallback()
        initView(view, savedInstanceState)
        if (registerEventBus) {
            org.greenrobot.eventbus.EventBus.getDefault().register(this)
        }
        initConfig(view, savedInstanceState)
        initListener()
        initObserver()
    }

    open fun initView(view: View, savedInstanceState: Bundle?) = Unit

    open fun initConfig(view: View, savedInstanceState: Bundle?) = initConfig(savedInstanceState)

    open fun initConfig(savedInstanceState: Bundle?) = Unit

    open fun initListener() = Unit

    open fun initObserver() = Unit

    open fun release() = Unit

    fun navigateTo(intent: android.content.Intent) {
        (activity as? BaseActivity<*>)?.navigateTo(intent) ?: startActivity(intent)
    }

    /** Gọi mỗi khi trạng thái mạng thay đổi */
    protected open fun onNetworkChanged(isConnected: Boolean) = Unit

    open fun onHandleBackPressed(): Boolean = false

    // region AdsHub =========================================================

    /**
     * MỌI fragment phải override bằng hằng số trong AdsScreen — kể cả fragment KHÔNG có ads
     * (giữ currentScreenId đúng cho safety rules / resume ad / preload màn kế).
     */
    protected open val adsScreenId: String? = null

    /** Override để gán tag slot động trước khi bind. */
    protected open fun onPrepareAdsSlots() = Unit

    private var hasBoundAds = false

    /**
     * Bind ở onResume (KHÔNG phải onViewCreated): ViewPager2 tạo view trước khi user thật sự nhìn
     * thấy — bind sớm là đốt fill + impression cho màn chưa hiện.
     *
     * Bind ĐÚNG MỘT LẦN cho mỗi view: `scanAndBind` của SDK gắn sẵn một LifecycleObserver tự
     * refresh slot ở các lần resume sau theo `refresh_policy` trong config. Bind lại mỗi lần
     * resume sẽ cộng dồn observer (SDK không gỡ cái cũ) → quay lại tab lần thứ N là N lượt load
     * cùng lúc.
     */
    override fun onResume() {
        super.onResume()
        if (hasBoundAds) return
        hasBoundAds = true
        bindAdsScreen()
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

    /** onFinished LUÔN chạy kể cả khi ads bị chặn → user không bao giờ kẹt. */
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

    /** Xem chú thích ở BaseActivity.runWhenResumed — fragment dùng viewLifecycleOwner. */
    protected fun runWhenResumed(action: () -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.withStateAtLeast(Lifecycle.State.RESUMED) { action() }
        }
    }

    // endregion AdsHub ======================================================

    private fun registerBackPressedCallback() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (!onHandleBackPressed()) {
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            },
        )
    }

    override fun onDestroyView() {
        release()
        if (registerEventBus) {
            org.greenrobot.eventbus.EventBus.getDefault().unregister(this)
        }
        hasBoundAds = false
        nullableBinding = null
        super.onDestroyView()
    }
}
