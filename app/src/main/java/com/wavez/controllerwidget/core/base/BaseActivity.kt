package com.wavez.controllerwidget.core.base

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.LocaleList
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.ads.admob.AdsManagerState
import com.ads.admob.ShowCondition
import com.ads.admob.banner_ads.BannerAdView
import com.ads.admob.interstitial_ads.AdInterstitialManager
import com.ads.admob.native_ads.NativeAdManager
import com.ads.admob.native_ads.NativeAdRepository
import com.ads.admob.native_ads.adapter.CollapsibleAd
import com.ads.admob.native_ads.adapter.NativeAdViewAd
import com.wavez.controllerwidget.R
import com.wavez.controllerwidget.ads.native_ad.NativeAdPlacement
import com.wavez.controllerwidget.core.extensions.collectWithLifecycle
import com.wavez.controllerwidget.core.extensions.findFirstViewOfType
import com.wavez.controllerwidget.core.extensions.gone
import com.wavez.controllerwidget.core.utils.NetworkTracker
import com.wavez.controllerwidget.data.repository.LanguageRepository
import com.wavez.controllerwidget.data.repository.SharedPref
import com.wavez.controllerwidget.di.AppModuleEntry
import com.wavez.controllerwidget.ui.dialog.NetworkDialog
import com.wavez.controllerwidget.ui.premium.PremiumViewModel
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
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

    @Inject
    lateinit var interstitialManager: AdInterstitialManager

    @Inject
    lateinit var showCondition: ShowCondition

    @Inject
    lateinit var nativeAdRepository: NativeAdRepository

    abstract fun bindingView(): T

    protected val nativeAds by lazy {
        NativeAdManager(
            repository = nativeAdRepository,
            lifecycleOwner = this,
            scope = lifecycleScope,
            isVip = { sharedPref.shouldShowInterSplash && !sharedPref.isVip  },
        )
    }

    private var networkTracker: NetworkTracker? = null
    private var networkDialog: NetworkDialog? = null

    private val premiumViewModel by viewModels<PremiumViewModel>()

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

    fun loadInterAds() {
        interstitialManager.loadAd()
    }

    fun loadNativeWithPosition(position: NativeAdPlacement, reloadToClick: Boolean = false) {
        nativeAds.load(position.name, reloadToClick)
    }

    fun setupNative(
        position: NativeAdPlacement,
        view: NativeAdViewAd,
        shouldShow: Boolean = true,
        autoLoad: Boolean = false,
        autoReloadOnStart: Boolean = false,
        shouldUseCollapseRate: Boolean = true,
    ) {
        if (view is CollapsibleAd) view.shouldCollapseRate = shouldUseCollapseRate
        nativeAds.setup(position.name, view, shouldShow, autoLoad, autoReloadOnStart)
    }

    protected fun hideBannerAdsIfNeed(bannerContainer: ViewGroup? = null): Boolean {
        val adView = bannerContainer ?: binding.root.findFirstViewOfType<BannerAdView>() ?: return false
        if (!showCondition.shouldShow()) {
            adView.gone()
            return true
        }
        return false
    }

    open fun initView(savedInstanceState: Bundle?) {}

    open fun initListener() {}

    open fun initConfig(savedInstanceState: Bundle?) {}

    open fun initObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                premiumViewModel.isPremium.collect {
                    onVipChangedState(it)
                }
            }
        }

        networkTracker?.isConnectedFlow?.collectWithLifecycle(this) { isConnected ->
            if (isConnected) dismissNetworkDialog() else showNetworkDialog()
        }
    }

    protected open fun onVipChangedState(isPremium: Boolean) {
        hideBannerAdsIfNeed()
    }

    open fun release() {}

    open fun onHandleBackPressed() {
        AdsManagerState.onBackScreen()
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
        forceShowAd: Boolean = false,
        shouldFinishCurrentActivity: Boolean = false,
    ) {
        val isSkipAd = !showCondition.shouldShow()
        if (isSkipAd) {
            startAndFinishCurrentIfNeed(intent, shouldFinishCurrentActivity)
            return
        }
        val showAdAction: (() -> Unit) -> Unit = if (forceShowAd) {
            { onContinue ->
                interstitialManager.forceShowAdFullScreen(
                    this,
                    onAdComplete = onContinue
                )
            }
        } else {
            { onContinue -> interstitialManager.showAdFullScreen(this, onAdComplete = onContinue) }
        }
        showAdAction { startAndFinishCurrentIfNeed(intent, shouldFinishCurrentActivity) }
    }

    private fun startAndFinishCurrentIfNeed(intent: Intent, shouldFinishCurrentActivity: Boolean) {
        startActivity(intent)
        if (shouldFinishCurrentActivity) finish()
    }

    fun executeWithAds(onBack: () -> Unit) {
        if (sharedPref.isVip) {
            onBack(); return
        }
        interstitialManager.showAdFullScreen(this) { onBack() }
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
        nativeAds.release()
        super.onDestroy()
    }
}
