package com.ads.admob

import android.app.Application
import com.ads.admob.interstitial_ads.AdInterstitialManager
import com.ads.admob.native_ads.NativeAdRepository
import com.ads.admob.open_ads.OpenAdManager
import com.ads.admob.reward.RewardAdManager
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

/**
 * Verifies [MMonetize] accessor safety:
 * - Accessing before init throws a clear error (not silent NPE).
 * - After init all expected beans are resolvable.
 * - Double init is caught (KoinAppAlreadyStartedException).
 */
class MMonetizeTest {

    private val mockApp = mockk<Application>(relaxed = true)

    @Before
    fun cleanup() {
        runCatching { stopKoin() }  // ensure clean state between tests
    }

    @After
    fun teardown() {
        runCatching { stopKoin() }
    }

    // ─── Before init ─────────────────────────────────────────────────────────

    @Test
    fun `nativeAdRepository before initializeAds - throws`() {
        assertThrows(Exception::class.java) {
            MMonetize.nativeAdRepository
        }
    }

    @Test
    fun `interstitialManager before initializeAds - throws`() {
        assertThrows(Exception::class.java) {
            MMonetize.interstitialManager
        }
    }

    @Test
    fun `openAdManager before initializeAds - throws`() {
        assertThrows(Exception::class.java) {
            MMonetize.openAdManager
        }
    }

    @Test
    fun `rewardAdManager before initializeAds - throws`() {
        assertThrows(Exception::class.java) {
            MMonetize.rewardAdManager
        }
    }

    // ─── After init ───────────────────────────────────────────────────────────

    @Test
    fun `all beans resolvable after Koin started`() {
        startKoin {
            androidContext(mockApp)
            modules(kenAdsModule())
        }

        assertNotNull(MMonetize.nativeAdRepository)
        assertNotNull(MMonetize.interstitialManager)
        assertNotNull(MMonetize.openAdManager)
        assertNotNull(MMonetize.rewardAdManager)
    }

    @Test
    fun `all beans are correct types`() {
        startKoin {
            androidContext(mockApp)
            modules(kenAdsModule())
        }

        assertTrue(MMonetize.nativeAdRepository   is NativeAdRepository)
        assertTrue(MMonetize.interstitialManager  is AdInterstitialManager)
        assertTrue(MMonetize.openAdManager        is OpenAdManager)
        assertTrue(MMonetize.rewardAdManager      is RewardAdManager)
    }

    @Test
    fun `beans are singletons - same instance on multiple accesses`() {
        startKoin {
            androidContext(mockApp)
            modules(kenAdsModule())
        }

        assertSame(MMonetize.nativeAdRepository,  MMonetize.nativeAdRepository)
        assertSame(MMonetize.interstitialManager,  MMonetize.interstitialManager)
        assertSame(MMonetize.openAdManager,        MMonetize.openAdManager)
        assertSame(MMonetize.rewardAdManager,      MMonetize.rewardAdManager)
    }

    // ─── Double init ─────────────────────────────────────────────────────────

    @Test
    fun `calling initializeAds twice throws KoinAppAlreadyStartedException`() {
        startKoin {
            androidContext(mockApp)
            modules(kenAdsModule())
        }

        assertThrows(Exception::class.java) {
            startKoin {
                androidContext(mockApp)
                modules(kenAdsModule())
            }
        }
    }
}
