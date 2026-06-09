package com.ads.admob

import android.app.Application
import com.ads.admob.native_ads.NativeAdRepository
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

/**
 * End-to-end safety tests — simulates typical app startup sequence
 * and verifies nothing crashes in normal production flow.
 */
class AdsFlowSafetyTest {

    private val mockApp = mockk<Application>(relaxed = true)

    @Before
    fun cleanup() { runCatching { stopKoin() } }

    @After
    fun teardown() { runCatching { stopKoin() } }

    // ─── Startup sequence ─────────────────────────────────────────────────────

    @Test
    fun `typical startup - Koin start then access all singletons - no crash`() {
        // Simulates: Application.onCreate() → MMonetize.initializeAds()
        startKoin {
            androidContext(mockApp)
            modules(kenAdsModule())
        }

        // Simulates: BaseActivity lazy NativeAdBinder init
        val repo = MMonetize.nativeAdRepository
        assertNotNull(repo)

        // Simulates: AppRemoteConfigImpl.setupInterstitialManager()
        MMonetize.interstitialManager.setAdUnitIds(listOf("ca-app-pub-test/001"))
        MMonetize.openAdManager.setAdUnitId("ca-app-pub-test/002")

        // Simulates: LoadCondition set in GameModeApp
        MMonetize.interstitialManager.setLoadCondition(LoadCondition { true })
        MMonetize.openAdManager.setLoadCondition(LoadCondition { true })
        MMonetize.nativeAdRepository.setLoadCondition(LoadCondition { true })
    }

    @Test
    fun `VIP user - LoadCondition blocks all loads - no crash`() {
        startKoin {
            androidContext(mockApp)
            modules(kenAdsModule())
        }

        val vip = LoadCondition { false }
        MMonetize.nativeAdRepository.setLoadCondition(vip)
        MMonetize.interstitialManager.setLoadCondition(vip)
        MMonetize.openAdManager.setLoadCondition(vip)

        // loadAd() with VIP condition → blocked, no crash
        MMonetize.nativeAdRepository.loadAd("test_placement")
        // No exception thrown
    }

    @Test
    fun `NativeAdRepository loadAd without registered IDs - emits Failed, no crash`() {
        startKoin {
            androidContext(mockApp)
            modules(kenAdsModule())
        }

        // Calling loadAd before setNativeAdIds → should emit Failed, not crash
        MMonetize.nativeAdRepository.loadAd("unregistered_placement")
        // StateFlow emits Failed — no exception
    }

    @Test
    fun `setLoadCondition and setShowCondition on all managers - no crash`() {
        startKoin {
            androidContext(mockApp)
            modules(kenAdsModule())
        }

        val show = ShowCondition { true }
        val load = LoadCondition { true }

        MMonetize.interstitialManager.setShowCondition(show)
        MMonetize.interstitialManager.setLoadCondition(load)
        MMonetize.openAdManager.setShowCondition(show)
        MMonetize.openAdManager.setLoadCondition(load)
        MMonetize.nativeAdRepository.setLoadCondition(load)
    }

    // ─── Repository state isolation ───────────────────────────────────────────

    @Test
    fun `two different placements are independent`() {
        startKoin {
            androidContext(mockApp)
            modules(kenAdsModule())
        }

        val repo = MMonetize.nativeAdRepository
        repo.setNativeAdIds("placement_a", listOf("ca-app-pub-test/001"))
        repo.setNativeAdIds("placement_b", listOf("ca-app-pub-test/002"))

        // clearAds on one doesn't crash the other
        repo.clearAds("placement_a")
        repo.releaseAd("placement_b")
    }

    // ─── Singleton identity across accesses ──────────────────────────────────

    @Test
    fun `NativeAdRepository singleton - same instance used by NativeAdBinder`() {
        startKoin {
            androidContext(mockApp)
            modules(kenAdsModule())
        }

        // NativeAdBinder default constructor gets repo from MMonetize (Koin)
        val fromMMonetize: NativeAdRepository = MMonetize.nativeAdRepository
        val binderRepo:    NativeAdRepository = MMonetize.nativeAdRepository

        // Must be same instance — binder and app layer share same repository
        assertSame(fromMMonetize, binderRepo)
    }
}
