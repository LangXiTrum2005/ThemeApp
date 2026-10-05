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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStateAtLeast
import androidx.viewbinding.ViewBinding
import com.adshub.monetization.AdsController
import com.adshub.monetization.config.ConfigManager
import com.adshub.monetization.model.RewardConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class BaseDialogFragment<T : ViewBinding> : DialogFragment() {

    private var nullableBinding: T? = null
    protected val binding: T
        get() = checkNotNull(nullableBinding) { "ViewBinding is only valid between onCreateView and onDestroyView." }

    open val fullScreen = false
    open val isTransparentBackground: Boolean = true
    open val hideNavigationBar: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        nullableBinding = bindingView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initConfig(view, savedInstanceState)
        initListener()
        initObserver()
        initTask()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setDimAmount(0.7f)
        if (isTransparentBackground) {
            dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        }
        dialog?.window?.apply {
            if (!fullScreen) {
                setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            } else {
                setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            }
            WindowInsetsControllerCompat(this, decorView).hide(WindowInsetsCompat.Type.statusBars())
        }
        if (hideNavigationBar) {
            dialog?.window?.let { window ->
                WindowCompat.setDecorFitsSystemWindows(window, false)
                WindowInsetsControllerCompat(window, window.decorView).apply {
                    hide(WindowInsetsCompat.Type.navigationBars())
                    systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            }
        }
    }

    override fun onDestroyView() {
        hasBoundAds = false
        release()
        nullableBinding = null
        super.onDestroyView()
    }

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            if (manager.findFragmentByTag(tag) == null) super.show(manager, tag)
        } catch (_: Exception) {}
    }

    // region AdsHub =========================================================

    /** Dialog cũng là một màn — khai screenId để log đủ full và giữ currentScreenId đúng. */
    protected open val adsScreenId: String? = null

    private var hasBoundAds = false

    override fun onResume() {
        super.onResume()
        if (!hasBoundAds) {
            hasBoundAds = true
            bindAdsScreen()
        }
    }

    protected fun bindAdsScreen() {
        val screenId = adsScreenId ?: return
        val root = view ?: return
        if (ConfigManager.isInitialConfigLoaded) {
            AdsController.bindScreen(screenId, root, viewLifecycleOwner)
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            while (!ConfigManager.isInitialConfigLoaded) delay(50L)
            val aliveRoot = view ?: return@launch
            AdsController.bindScreen(screenId, aliveRoot, viewLifecycleOwner)
        }
    }

    private var isRewardPending = false

    protected fun triggerRewardAds(triggerId: String, action: () -> Unit) {
        if (isRewardPending) return
        isRewardPending = true
        thinklife.prism.wallpaper.widget.theme.ads.AdsBootstrap.triggerReward(
            activity = requireActivity(),
            triggerId = triggerId,
            screenId = adsScreenId,
            onAllowed = { runWhenResumed(action) },
            onFinished = { isRewardPending = false },
        )
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

    protected fun runWhenResumed(action: () -> Unit) {
        val owner = viewLifecycleOwnerLiveData.value ?: return
        owner.lifecycleScope.launch {
            owner.lifecycle.withStateAtLeast(Lifecycle.State.RESUMED) { action() }
        }
    }

    // endregion AdsHub ======================================================

    open fun initObserver() = Unit
    open fun initConfig(view: View, savedInstanceState: Bundle?) = Unit
    open fun initListener() = Unit
    open fun initTask() = Unit
    open fun release() = Unit

    protected abstract fun bindingView(): T
}
