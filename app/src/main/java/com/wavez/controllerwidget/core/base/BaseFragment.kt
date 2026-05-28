package com.wavez.controllerwidget.core.base

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewbinding.ViewBinding
import com.ads.admob.native_ads.adapter.NativeAdViewAd
import com.ads.admob.native_ads.adapter.NativeDisplayController
import com.wavez.controllerwidget.ads.native_ad.NativeAdPlacement
import com.wavez.controllerwidget.ads.native_ad.NativeAdViewModel
import com.wavez.controllerwidget.data.repository.SharedPref
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

abstract class BaseFragment<T : ViewBinding> : Fragment() {

    private var _binding: T? = null

    protected val binding get() = _binding!!

    open val registerEventBus = false

    open val screenId = ""

    @Inject
    lateinit var sharedPref: SharedPref

    protected val nativeAdViewModel by viewModels<NativeAdViewModel>()

    private val nativeDisplayControllers =
        mutableMapOf<NativeAdPlacement, NativeDisplayController>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = bindingView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view, savedInstanceState)
        if (registerEventBus) EventBus.getDefault().register(this)
        initConfig(view, savedInstanceState)
        initListener()
        initObserver()
    }

    fun loadNativeWithPosition(position: NativeAdPlacement, reloadToClick: Boolean = false) {
        nativeAdViewModel.loadAd(position, reloadToClick)
    }

    fun registerNativeAd(
        position: NativeAdPlacement,
        adViewHolder: NativeAdViewAd,
        shouldShow: Boolean,
    ) {
        nativeAdViewModel.getNativeFlowByPosition(position).observe(viewLifecycleOwner) { state ->
            Log.d("Log_native_ad", "registerNativeAd: $state")
            val nativeAdPlaceholder = nativeAdViewModel.getNativeAdPlaceholder(position)
            adViewHolder.shouldShowAd = shouldShow && !sharedPref.isVip
            val nativeDisplay = NativeDisplayController(nativeAdPlaceholder, adViewHolder)
            nativeDisplay.setupAdDisplay(state)
            nativeDisplayControllers.remove(position)?.cleanupAdDisplay()
            nativeDisplayControllers[position] = nativeDisplay
        }
    }

    /**
     * @param position
     * @param view
     * @param autoLoad
     * @param shouldShow
     * */
    fun setupNative(
        position: NativeAdPlacement,
        view: NativeAdViewAd,
        shouldShow: Boolean = true,
        autoLoad: Boolean = false,
    ) {
        if (autoLoad) loadNativeWithPosition(position)
        ensureObserved(position)
        registerNativeAd(position, view, shouldShow)
    }

    private fun ensureObserved(position: NativeAdPlacement) {
        nativeAdViewModel.ensureObserved(position)
    }

    private fun releaseNativeAds() {
        nativeDisplayControllers.forEach {
            it.value.cleanupAdDisplay()
        }
        nativeDisplayControllers.clear()
    }


    fun navigateTo(intent: Intent, forceShowAd: Boolean = false) {
        (requireActivity() as BaseActivity<*>).navigateTo(intent, forceShowAd)
    }

    fun executeWithAds(action: () -> Unit) {
        (requireActivity() as BaseActivity<*>).executeWithAds(action)
    }

    open fun onHandleBackPressed() {
        (requireActivity() as BaseActivity<*>).onHandleBackPressed()
    }

    override fun onDestroyView() {
        releaseNativeAds()
        release()
        if (registerEventBus) EventBus.getDefault().unregister(this)
        _binding = null
        super.onDestroyView()
    }

    open fun initView(view: View, savedInstanceState: Bundle?) {}

    open fun initObserver() {}

    open fun initConfig(view: View, savedInstanceState: Bundle?) {}

    open fun initListener() {}

    open fun release() {}

    abstract fun bindingView(): T

}
