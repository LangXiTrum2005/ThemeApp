package com.wavez.controllerwidget.core.base

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.ads.admob.MMonetize
import com.ads.admob.native_ads.NativeAdBinder
import com.ads.admob.native_ads.NativeAdRepository
import com.ads.admob.native_ads.adapter.NativeAdViewAd
import com.wavez.controllerwidget.ads.native_ad.NativeAdPlacement
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


    private val nativeAdBinder by lazy { NativeAdBinder() }

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

    fun setupNative(
        position: NativeAdPlacement,
        view: NativeAdViewAd,
        shouldShow: Boolean = true,
        autoLoad: Boolean = false,
        preloadOnClick: Boolean = false,
    ) {
        nativeAdBinder.attach(
            lifecycleOwner = viewLifecycleOwner,
            placement = position.name,
            view = view,
            shouldShow = shouldShow,
            autoLoad = autoLoad,
            preloadOnClick = preloadOnClick,
        )
    }

    fun loadNativeWithPosition(position: NativeAdPlacement) {
        MMonetize.nativeAdRepository.loadAd(position.name)
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
