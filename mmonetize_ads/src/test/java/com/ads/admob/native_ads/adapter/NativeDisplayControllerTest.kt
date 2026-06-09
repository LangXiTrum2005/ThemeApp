package com.ads.admob.native_ads.adapter

import com.google.android.gms.ads.nativead.NativeAd
import io.mockk.*
import org.junit.Before
import org.junit.Test

/**
 * Validates the view state machine driven by [NativeDisplayController].
 *
 * Key invariants under test:
 * - shouldShowAd = false → always hidePlaceholder(), regardless of state
 * - Loaded(ad) → bind() called; same ad instance a second time → bind() NOT called again
 * - Loading / AdClicked → showPlaceholder(), boundAd cleared
 * - Failed / Idle → hidePlaceholder()
 * - cleanup() resets boundAd so next Loaded re-binds even with the same ad object
 */
class NativeDisplayControllerTest {

    private lateinit var view: NativeAdViewAd
    private lateinit var controller: NativeDisplayController

    private val ad1 = mockk<NativeAd>(relaxed = true)
    private val ad2 = mockk<NativeAd>(relaxed = true)

    @Before
    fun setup() {
        view = mockk(relaxed = true)
        controller = NativeDisplayController(view)
    }

    // ─── shouldShowAd = false gate ────────────────────────────────────────────

    @Test
    fun `shouldShowAd false - Loaded state - hides placeholder`() {
        every { view.shouldShowAd } returns false

        controller.onStateChanged(NativeAdLoadState.Loaded(ad1))

        verify(exactly = 1) { view.hidePlaceholder() }
        verify(exactly = 0) { view.bind(any()) }
    }

    @Test
    fun `shouldShowAd false - Loading state - hides placeholder`() {
        every { view.shouldShowAd } returns false

        controller.onStateChanged(NativeAdLoadState.Loading)

        verify(exactly = 1) { view.hidePlaceholder() }
        verify(exactly = 0) { view.showPlaceholder() }
    }

    @Test
    fun `shouldShowAd false - Failed state - hides placeholder`() {
        every { view.shouldShowAd } returns false

        controller.onStateChanged(NativeAdLoadState.Failed("error"))

        verify(exactly = 1) { view.hidePlaceholder() }
    }

    // ─── Loaded ───────────────────────────────────────────────────────────────

    @Test
    fun `Loaded - binds ad to view`() {
        every { view.shouldShowAd } returns true

        controller.onStateChanged(NativeAdLoadState.Loaded(ad1))

        verify(exactly = 1) { view.bind(ad1) }
    }

    @Test
    fun `Loaded - same ad instance twice - bind called only once`() {
        every { view.shouldShowAd } returns true

        controller.onStateChanged(NativeAdLoadState.Loaded(ad1))
        controller.onStateChanged(NativeAdLoadState.Loaded(ad1))

        // Dedup: second emission with same ad object must not re-bind
        verify(exactly = 1) { view.bind(ad1) }
    }

    @Test
    fun `Loaded - different ad after first - bind called for each`() {
        every { view.shouldShowAd } returns true

        controller.onStateChanged(NativeAdLoadState.Loaded(ad1))
        controller.onStateChanged(NativeAdLoadState.Loaded(ad2))

        verify(exactly = 1) { view.bind(ad1) }
        verify(exactly = 1) { view.bind(ad2) }
    }

    // ─── Loading / AdClicked ──────────────────────────────────────────────────

    @Test
    fun `Loading - shows placeholder and clears bound ad`() {
        every { view.shouldShowAd } returns true

        // First bind an ad, then go to loading
        controller.onStateChanged(NativeAdLoadState.Loaded(ad1))
        controller.onStateChanged(NativeAdLoadState.Loading)

        verify { view.showPlaceholder() }
    }

    @Test
    fun `Loading then Loaded with same ad - bind called again after Loading cleared boundAd`() {
        every { view.shouldShowAd } returns true

        controller.onStateChanged(NativeAdLoadState.Loaded(ad1))  // bind
        controller.onStateChanged(NativeAdLoadState.Loading)       // clears boundAd
        controller.onStateChanged(NativeAdLoadState.Loaded(ad1))  // bind again — boundAd was null

        verify(exactly = 2) { view.bind(ad1) }
    }

    @Test
    fun `AdClicked - shows placeholder`() {
        every { view.shouldShowAd } returns true

        controller.onStateChanged(NativeAdLoadState.AdClicked)

        verify { view.showPlaceholder() }
    }

    // ─── Failed / Idle ────────────────────────────────────────────────────────

    @Test
    fun `Failed - hides placeholder`() {
        every { view.shouldShowAd } returns true

        controller.onStateChanged(NativeAdLoadState.Failed("timeout"))

        verify { view.hidePlaceholder() }
    }

    @Test
    fun `Idle - hides placeholder`() {
        every { view.shouldShowAd } returns true

        controller.onStateChanged(NativeAdLoadState.Idle)

        verify { view.hidePlaceholder() }
    }

    // ─── cleanup ──────────────────────────────────────────────────────────────

    @Test
    fun `cleanup - resets bound ad - next same-ad Loaded re-binds`() {
        every { view.shouldShowAd } returns true

        controller.onStateChanged(NativeAdLoadState.Loaded(ad1))  // binds
        controller.cleanup()
        controller.onStateChanged(NativeAdLoadState.Loaded(ad1))  // boundAd was cleared → binds again

        verify(exactly = 2) { view.bind(ad1) }
    }
}
