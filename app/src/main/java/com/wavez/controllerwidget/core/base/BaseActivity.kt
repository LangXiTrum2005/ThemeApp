package com.wavez.controllerwidget.core.base

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.LocaleList
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
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
import com.wavez.controllerwidget.R
import com.wavez.controllerwidget.data.repository.SharedPref
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.util.Locale
import javax.inject.Inject

abstract class BaseActivity<T : ViewBinding> :
    AppCompatActivity() {

    companion object {
    }

    open val binding by lazy { bindingView() }

    open val registerEventBus = false

    @Inject
    lateinit var sharedPref: SharedPref

    abstract fun bindingView(): T

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    /* override fun attachBaseContext(newBase: Context) {
 //        val languageProvider: LanguageRepository by lazy {
 //            EntryPointAccessors.fromApplication(
 //                newBase.applicationContext, AppModuleEntry::class.java
 //            ).getLanguageRepository()
 //        }
 //        val wrappedContext = updateLocaleFormat(newBase, languageProvider.getConfigLanguageCode())
         super.attachBaseContext(wrappedContext)
     }
 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onHandleBackPressed()
                }
            },
        )
        if (registerEventBus) EventBus.getDefault().register(this)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        hideBottomNavigation()
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
            false
        initView(savedInstanceState)
        initConfig(savedInstanceState)
        initListener()
        initObserver()
    }


    protected fun hideBannerAdsIfNeed(bannerContainer: ViewGroup? = null): Boolean {
        /* val adView: View =
             bannerContainer ?: findViewById<BannerAdView>(R.id.banner_ads)
             ?: return false
         if (!showCondition.shouldShow()) {
             Log.d("banner", "hideBannerAdsIfNeed: ")
             adView.gone()
             return true
         }*/
        return false
    }

    open fun initView(savedInstanceState: Bundle?) {}

    open fun initListener() {}

    open fun initConfig(savedInstanceState: Bundle?) {}

    open fun initObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                premiumViewModel.isPremium.collect {
//                    onVipChangedState(it)
//                }
            }
        }
    }

    protected open fun onVipChangedState(isPremium: Boolean) {
        hideBannerAdsIfNeed()

    }

    open fun release() {}

    open fun onHandleBackPressed() {
//        AdsManagerState.onBackScreen()
        if (supportFragmentManager.backStackEntryCount == 0) {
            finish()
            return
        } else {
            supportFragmentManager.popBackStack()
        }
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
        /*val isSkipAd = !showCondition.shouldShow()
        if (isSkipAd) {
            startAndFinishCurrentIfNeed(intent, shouldFinishCurrentActivity)
            return
        }

        val showAdAction: (() -> Unit) -> Unit = if (forceShowAd) {
            { onContinue ->
                interstitialManager.forceShowAdFullScreen(this, onAdComplete = onContinue)
            }
        } else {
            { onContinue -> interstitialManager.showAdFullScreen(this, onAdComplete = onContinue) }
        }
        showAdAction { startAndFinishCurrentIfNeed(intent, shouldFinishCurrentActivity) }*/
    }

    private fun startAndFinishCurrentIfNeed(intent: Intent, shouldFinishCurrentActivity: Boolean) {
        startActivity(intent)
        if (shouldFinishCurrentActivity) {
            finish()
        }
    }

    fun executeWithAds(onBack: () -> Unit) {
        /*val isSkipAd = sharedPref.isVip
        if (isSkipAd) {
            onBack()
            return
        }
        interstitialManager.showAdFullScreen(this) {
            onBack()
        }*/
    }

    override fun onDestroy() {
        if (registerEventBus) EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

}
