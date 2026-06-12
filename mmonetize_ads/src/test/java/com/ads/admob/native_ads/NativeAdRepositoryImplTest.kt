package com.ads.admob.native_ads

import app.cash.turbine.test
import com.ads.admob.native_ads.adapter.NativeAdLoadState
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [NativeAdRepositoryImpl] — canLoad() gate logic.
 *
 * These tests only cover cases that do NOT reach AdLoader.Builder so they run
 * as pure JVM unit tests without Robolectric.
 *
 * Cases that DO reach AdLoader (actual load / waterfall / callback) are
 * covered in androidTest (NativeAdRepositoryImplAndroidTest).
 */
class NativeAdRepositoryImplTest {

    // Context is needed for the constructor but never reached in these test paths
    private val context = mockk<android.content.Context>(relaxed = true)
    private lateinit var repo: NativeAdRepositoryImpl

    companion object {
        private const val PLACEMENT = "test_placement"
        private val FAKE_IDS = listOf("ca-app-pub-test/001", "ca-app-pub-test/002")
    }

    @Before
    fun setup() {
        repo = NativeAdRepositoryImpl(context)
    }

    // ─── Guard: no IDs registered ────────────────────────────────────────────

    @Test
    fun `loadAd without registering IDs - emits Failed`() = runTest {
        repo.getAdStateFlow(PLACEMENT).test {
            repo.loadAd(PLACEMENT)

            assertEquals(NativeAdLoadState.Idle, awaitItem())
            assertTrue(awaitItem() is NativeAdLoadState.Failed)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadAd without IDs - Failed message contains placement name`() = runTest {
        repo.getAdStateFlow(PLACEMENT).test {
            repo.loadAd(PLACEMENT)

            awaitItem() // Idle
            val failed = awaitItem() as NativeAdLoadState.Failed
            assertTrue(failed.errorMessage.contains(PLACEMENT, ignoreCase = true))
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── Guard: LoadCondition blocks ─────────────────────────────────────────

    @Test
    fun `loadAd with LoadCondition returning false - stays Idle`() = runTest {
        repo.setLoadCondition(NativeLoadCondition { false })
        repo.setNativeAdIds(PLACEMENT, FAKE_IDS)

        repo.getAdStateFlow(PLACEMENT).test {
            repo.loadAd(PLACEMENT)

            assertEquals(NativeAdLoadState.Idle, awaitItem())
            expectNoEvents()   // condition blocked — nothing further emitted
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadAd with null LoadCondition - proceeds (condition not set)`() = runTest {
        // No LoadCondition set → shouldLoad() check skipped → canLoad() passes
        repo.setNativeAdIds(PLACEMENT, FAKE_IDS)

        repo.getAdStateFlow(PLACEMENT).test {
            repo.loadAd(PLACEMENT)

            awaitItem() // Idle
            // LoadCondition is null → canLoad() passes → reaches AdLoader → emits Loading
            assertEquals(NativeAdLoadState.Loading, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── releaseAd ────────────────────────────────────────────────────────────

    @Test
    fun `releaseAd - emits Idle`() = runTest {
        repo.setNativeAdIds(PLACEMENT, FAKE_IDS)

        repo.getAdStateFlow(PLACEMENT).test {
            awaitItem() // initial Idle

            repo.releaseAd(PLACEMENT)

            assertEquals(NativeAdLoadState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── clearAds ─────────────────────────────────────────────────────────────

    @Test
    fun `clearAds - emits Idle`() = runTest {
        repo.setNativeAdIds(PLACEMENT, FAKE_IDS)

        repo.getAdStateFlow(PLACEMENT).test {
            awaitItem() // initial Idle

            repo.clearAds(PLACEMENT)

            assertEquals(NativeAdLoadState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── Independent placements ───────────────────────────────────────────────

    @Test
    fun `LoadCondition blocks all - multiple placements each stay Idle`() = runTest {
        val p2 = "placement_2"
        repo.setLoadCondition(NativeLoadCondition { false })
        repo.setNativeAdIds(PLACEMENT, FAKE_IDS)
        repo.setNativeAdIds(p2, FAKE_IDS)

        repo.loadAd(PLACEMENT)
        repo.loadAd(p2)

        repo.getAdStateFlow(PLACEMENT).test {
            assertEquals(NativeAdLoadState.Idle, awaitItem())
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
        repo.getAdStateFlow(p2).test {
            assertEquals(NativeAdLoadState.Idle, awaitItem())
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setNativeAdIds - resets index to zero`() = runTest {
        // Register once, register again — index should reset
        repo.setNativeAdIds(PLACEMENT, FAKE_IDS)
        repo.setNativeAdIds(PLACEMENT, listOf("ca-app-pub-new/999"))

        // State should be Idle; no crash from re-registration
        repo.getAdStateFlow(PLACEMENT).test {
            assertEquals(NativeAdLoadState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
