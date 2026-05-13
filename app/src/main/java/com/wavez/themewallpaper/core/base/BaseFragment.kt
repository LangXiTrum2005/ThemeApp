package com.example.m65_gamemodefps.core.base

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.wavez.themewallpaper.core.base.BaseActivity
import com.wavez.themewallpaper.data.repository.SharedPref
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

abstract class BaseFragment<T : ViewBinding> : Fragment() {

    private var _binding: T? = null

    protected val binding get() = _binding!!

    open val registerEventBus = false

    open val screenId = ""

    @Inject
    lateinit var sharedPref: SharedPref

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
