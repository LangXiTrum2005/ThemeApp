package com.wavez.themewallpaper.core.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentManager
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wavez.themewallpaper.R
import org.greenrobot.eventbus.EventBus

abstract class BaseBottomSheetDialog<T : ViewBinding> : BottomSheetDialogFragment() {

    open val binding by lazy { bindingView() }

    open var registerEventBus = false

    open val useDim: Boolean = false

    open val hideNavigationBar: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (registerEventBus) EventBus.getDefault().register(this)
        initObserver()
        initConfig(view, savedInstanceState)
        initListener()
        initTask()
    }

    override fun onStart() {
        super.onStart()
        if (!useDim) {
            dialog?.window?.setBackgroundDrawableResource(R.color.transparent)
        }
        if (hideNavigationBar) {
            dialog?.window?.let { window ->
                WindowCompat.setDecorFitsSystemWindows(window, false)
                val decorView = window.decorView
                ViewCompat.setOnApplyWindowInsetsListener(decorView) { view, insets ->
                    if (insets.isVisible(WindowInsetsCompat.Type.navigationBars())) {
                        WindowInsetsControllerCompat(window, view).apply {
                            hide(WindowInsetsCompat.Type.navigationBars())
                            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                        }
                    }
                    ViewCompat.onApplyWindowInsets(view, insets)
                }
                decorView.post {
                    WindowInsetsControllerCompat(window, decorView).apply {
                        hide(WindowInsetsCompat.Type.navigationBars())
                        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        if (registerEventBus) EventBus.getDefault().unregister(this)
        release()
        super.onDestroyView()
    }

    override fun show(manager: FragmentManager, tag: String?) {
        manager.findFragmentByTag(tag).let { fragment ->
            fragment ?: let {
                manager.beginTransaction().let { transition ->
                    this.show(transition, tag)
                }
            }
        }
    }

    open fun initObserver() {}

    open fun initConfig(view: View, savedInstanceState: Bundle?) {}

    open fun initListener() {}

    open fun initTask() {}

    open fun release() {}

    abstract fun bindingView(): T

    override fun getTheme(): Int = R.style.TransparentBottomSheetDialog
}
