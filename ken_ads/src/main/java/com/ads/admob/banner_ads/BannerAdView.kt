package com.ads.admob.banner_ads

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.lib.mobileads.R

class BannerAdView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), LifecycleEventObserver {

    private val shimmer: View
    private val adContainer: FrameLayout

    private val bannerAdManager: BannerAdManager by lazy {
        BannerAdManagerImpl(context)
    }

    private var isLoading = false
    private var isDestroyed = false
    private var lifecycleOwner: LifecycleOwner? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_banner_ad, this, true)
        shimmer = findViewById(R.id.shimmer)
        adContainer = findViewById(R.id.adContainer)
    }

    fun setupLifecycle(lifecycleOwner: LifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                if (!isDestroyed) {
                    bannerAdManager.onResumeBannerAd()
                }
            }

            Lifecycle.Event.ON_PAUSE -> {
                if (!isDestroyed) {
                    bannerAdManager.onPauseBannerAd()
                }
            }

            Lifecycle.Event.ON_DESTROY -> {
                destroy()
            }

            else -> {}
        }
    }

    fun registerBanner(
        lifecycleOwner: LifecycleOwner,
        adId: String,
        adIdHigh: String? = null,
        collapsible: Boolean = false,
        shouldLoad: Boolean = true,
        adSize: AdSizeBanner = AdSizeBanner.ADAPTIVE
    ) {
        if (!shouldLoad) {
            visibility = GONE
            return
        }
        if (this.lifecycleOwner == null) {
            this.lifecycleOwner = lifecycleOwner
            lifecycleOwner.lifecycle.addObserver(this)
        }

        if (isDestroyed) {
            return
        }

        if (isLoading) {
            return
        }

        isLoading = true
        visibility = VISIBLE
        shimmer.visibility = VISIBLE
        adContainer.removeAllViews()

        bannerAdManager.loadBannerAd(
            adId = adId,
            adIdHigh = adIdHigh,
            adSize = adSize,
            collapsible = collapsible,
            onAdLoaded = { adView ->
                if (!isDestroyed) {
                    shimmer.visibility = GONE
                    adContainer.removeAllViews()
                    adContainer.addView(adView)
                }
                isLoading = false
            },
            onAdFailedToLoad = {
                if (!isDestroyed) {
                    shimmer.visibility = GONE
                    visibility = GONE
                }
                isLoading = false
            }
        )
    }

    fun goneBannerView() {
        visibility = GONE
    }

    fun showBannerView() {
        visibility = VISIBLE
    }

    private fun destroy() {
        if (!isDestroyed) {
            isDestroyed = true
            adContainer.removeAllViews()
            bannerAdManager.destroyBannerAd()
            lifecycleOwner?.lifecycle?.removeObserver(this)
            lifecycleOwner = null
        }
    }
}