package com.ads.admob.interstitial_ads

import android.app.Activity
import io.mockk.mockk
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Crash-safety tests for [InterSplashManager].
 *
 * Production crash risks:
 * 1. [onComplete] never called → user stuck on splash forever.
 * 2. [onComplete] called more than once → double navigation crash.
 * 3. Scope cancelled mid-wait → silent failure, user stuck on splash.
 */
class InterSplashManagerTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val mockActivity   = mockk<Activity>(relaxed = true)
    private val mockContext    = mockk<android.content.Context>(relaxed = true)

    @Before
    fun setup() { Dispatchers.setMain(testDispatcher) }

    @After
    fun teardown() { Dispatchers.resetMain() }

    // ─── shouldShow = false ───────────────────────────────────────────────────

    @Test
    fun `shouldShow=false - onComplete called exactly once after minDelay`() = runTest {
        val manager = manager(shouldShow = { false })
        var count = 0

        manager.showOrTimeout(mockActivity) { count++ }

        assertEquals(1, count)
    }

    @Test
    fun `shouldShow=false - onComplete NOT called before minDelay elapses`() = runTest {
        val manager = manager(shouldShow = { false }, minDelayMs = 500)
        var count = 0

        launch {
            manager.showOrTimeout(mockActivity) { count++ }
        }

        // Before minDelay completes, onComplete not yet called
        assertEquals(0, count)

        // After advancing virtual time past minDelay
        advanceTimeBy(501)
        assertEquals(1, count)
    }

    // ─── load() not called ───────────────────────────────────────────────────

    @Test
    fun `load not called - showOrTimeout still calls onComplete`() = runTest {
        // loadStartedAt = 0 → elapsed = currentTime - 0 = huge → immediate timeout
        val manager = manager(shouldShow = { true })
        var count = 0

        // Without calling load(), elapsed will exceed maxWaitMs immediately
        manager.showOrTimeout(mockActivity) { count++ }

        // onComplete must be called (ad not ready + timeout)
        assertEquals(1, count)
    }

    // ─── Scope cancellation ───────────────────────────────────────────────────

    @Test
    fun `scope cancelled during minDelay - onComplete NOT called - no crash`() = runTest {
        val manager = manager(shouldShow = { true }, minDelayMs = 1000)
        var count = 0
        var threw = false

        val job = launch {
            try {
                manager.showOrTimeout(mockActivity) { count++ }
            } catch (e: Exception) {
                threw = true
            }
        }

        // Cancel before minDelay completes
        job.cancel()
        advanceUntilIdle()

        // No crash, onComplete not called (activity is gone, correct behavior)
        assertFalse("Exception thrown on scope cancel", threw)
        assertEquals("onComplete called after scope cancel", 0, count)
    }

    @Test
    fun `scope cancelled after minDelay during poll - onComplete NOT called - no crash`() = runTest {
        val manager = manager(
            shouldShow = { true },
            minDelayMs = 100,
            maxWaitMs  = 10_000,
            pollMs     = 500,
        )
        var count = 0
        var threw = false

        val job = launch {
            try {
                manager.showOrTimeout(mockActivity) { count++ }
            } catch (e: Exception) {
                threw = true
            }
        }

        advanceTimeBy(150)   // past minDelay, inside first poll wait
        job.cancel()
        advanceUntilIdle()

        assertFalse("Exception thrown on scope cancel", threw)
        assertEquals("onComplete called after scope cancel", 0, count)
    }

    // ─── Double call ─────────────────────────────────────────────────────────

    @Test
    fun `showOrTimeout called twice - onComplete called twice but independently`() = runTest {
        val manager = manager(shouldShow = { false })
        var count = 0

        manager.showOrTimeout(mockActivity) { count++ }
        manager.showOrTimeout(mockActivity) { count++ }

        // Each call is independent — each fires once
        assertEquals(2, count)
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private fun manager(
        shouldShow: () -> Boolean,
        minDelayMs: Long = 100,
        maxWaitMs: Long  = 500,
        pollMs: Long     = 50,
    ) = InterSplashManager(
        context    = mockContext,
        adUnitIds  = listOf("ca-app-pub-test/001"),
        shouldShow = shouldShow,
        minDelayMs = minDelayMs,
        maxWaitMs  = maxWaitMs,
        pollMs     = pollMs,
    )
}
