package com.wavez.themewallpaper.core.base

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.LocaleList
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewbinding.ViewBinding
import com.wavez.themewallpaper.R
import com.wavez.themewallpaper.core.extensions.collectWithLifecycle
import com.wavez.themewallpaper.core.utils.NetworkTracker
import com.wavez.themewallpaper.data.repository.LanguageRepository
import com.wavez.themewallpaper.data.repository.SharedPref
import com.wavez.themewallpaper.di.AppModuleEntry
import com.wavez.themewallpaper.ui.dialog.NetworkDialog
import dagger.hilt.android.EntryPointAccessors
import org.greenrobot.eventbus.EventBus
import java.util.Locale
import javax.inject.Inject

abstract class BaseActivity<T : ViewBinding> : AppCompatActivity() {

    companion object {
    }

    open val binding by lazy { bindingView() }

    open val registerEventBus = false

    @Inject
    lateinit var sharedPref: SharedPref

    abstract fun bindingView(): T

    private var networkTracker: NetworkTracker? = null
    private var networkDialog: NetworkDialog? = null

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun attachBaseContext(newBase: Context) {
        val languageProvider: LanguageRepository by lazy {
            EntryPointAccessors.fromApplication(
                newBase.applicationContext, AppModuleEntry::class.java
            ).getLanguageRepository()
        }
        val wrappedContext = updateLocaleFormat(newBase, languageProvider.getConfigLanguageCode())
        super.attachBaseContext(wrappedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onHandleBackPressed()
            }
        })
        networkTracker = NetworkTracker(this, lifecycle)
        if (registerEventBus) EventBus.getDefault().register(this)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        hideBottomNavigation()
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
            true
        initView(savedInstanceState)
        initConfig(savedInstanceState)
        initListener()
        initObserver()
    }

    open fun initView(savedInstanceState: Bundle?) {}

    open fun initListener() {}

    open fun initConfig(savedInstanceState: Bundle?) {}

    open fun initObserver() {
        networkTracker?.isConnectedFlow?.collectWithLifecycle(this) { isConnected ->
            if (isConnected) dismissNetworkDialog() else showNetworkDialog()
        }
    }

    open fun release() {}

    open fun onHandleBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 0) finish()
        else supportFragmentManager.popBackStack()
    }

    private fun updateLocaleFormat(c: Context, languageTag: String): ContextWrapper {
        var context = c
        val locale = Locale.forLanguageTag(languageTag)
        Locale.setDefault(locale)
        val resources = context.resources
        val configuration = Configuration(resources.configuration)
        val localeList = LocaleList(locale)
        LocaleList.setDefault(localeList)
        configuration.setLocales(localeList)
        configuration.setLayoutDirection(locale)
        context = context.createConfigurationContext(configuration)
        return ContextWrapper(context)
    }

    protected fun hideBottomNavigation() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    fun navigateTo(
        intent: Intent,
        shouldFinishCurrentActivity: Boolean = false,
    ) {
        startActivity(intent)
        if (shouldFinishCurrentActivity) finish()
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

    override fun onDestroy() {
        if (registerEventBus) EventBus.getDefault().unregister(this)
        release()
        super.onDestroy()
    }
}
