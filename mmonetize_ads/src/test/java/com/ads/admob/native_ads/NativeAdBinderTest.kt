package com.ads.admob.native_ads

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.ads.admob.native_ads.adapter.NativeAdLoadState
import com.ads.admob.native_ads.adapter.NativeAdViewAd
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Validates [NativeAdBinder.attach] — autoLoad×shouldShow matrix,
 * lifecycle cleanup, and back-navigation detection via ProcessLifecycle.
 *
 * Back-navigation state machine:
 *   onStop()                    → STOPPED_FOREGROUND
 *   processLifecycle.onStop()   → STOPPED_BACKGROUND  (home button / recents)
 *   onStart() from FOREGROUND   → forceReload
 *   onStart() from BACKGROUND   → no reload
 *   onStart() from INIT         → no reload (fresh create)
 */
class NativeAdBinderTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var activityOwner: FakeLifecycleOwner
    private lateinit var processOwner: FakeLifecycleOwner
    private lateinit var repo: NativeAdRepository
    private lateinit var view: NativeAdViewAd
    private lateinit var binder: NativeAdBinder

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        activityOwner = FakeLifecycleOwner()
        processOwner  = FakeLifecycleOwner()
        repo = mockk(relaxed = true)
        view = mockk(relaxed = true)
        every { repo.getAdStateFlow(any()) } returns MutableStateFlow(NativeAdLoadState.Idle)
        binder = NativeAdBinder(repo, processLifecycle = processOwner.lifecycle)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    // ─── autoLoad × shouldShow ────────────────────────────────────────────────

    @Test
    fun `autoLoad=true shouldShow=true - loadAd called`() {
        binder.attach(activityOwner, "p", view, shouldShow = true, autoLoad = true)
        verify(exactly = 1) { repo.loadAd("p", false) }
    }

    @Test
    fun `autoLoad=true shouldShow=false - loadAd NOT called`() {
        binder.attach(activityOwner, "p", view, shouldShow = false, autoLoad = true)
        verify(exactly = 0) { repo.loadAd(any(), any()) }
    }

    @Test
    fun `autoLoad=false shouldShow=true - loadAd NOT called`() {
        binder.attach(activityOwner, "p", view, shouldShow = true, autoLoad = false)
        verify(exactly = 0) { repo.loadAd(any(), any()) }
    }

    @Test
    fun `autoLoad=false shouldShow=false - loadAd NOT called`() {
        binder.attach(activityOwner, "p", view, shouldShow = false, autoLoad = false)
        verify(exactly = 0) { repo.loadAd(any(), any()) }
    }

    // ─── shouldShowAd propagation ─────────────────────────────────────────────

    @Test
    fun `shouldShow=true propagated to view`() {
        binder.attach(activityOwner, "p", view, shouldShow = true)
        verify { view.shouldShowAd = true }
    }

    @Test
    fun `shouldShow=false propagated to view`() {
        binder.attach(activityOwner, "p", view, shouldShow = false)
        verify { view.shouldShowAd = false }
    }

    // ─── onDestroy cleanup ────────────────────────────────────────────────────

    @Test
    fun `onDestroy - releaseAd called`() {
        activityOwner.moveTo(Lifecycle.State.STARTED)
        binder.attach(activityOwner, "p", view, shouldShow = true)
        activityOwner.moveTo(Lifecycle.State.DESTROYED)
        verify(exactly = 1) { repo.releaseAd("p") }
    }

    @Test
    fun `onDestroy - each placement released`() {
        val view2 = mockk<NativeAdViewAd>(relaxed = true)
        every { repo.getAdStateFlow("p2") } returns MutableStateFlow(NativeAdLoadState.Idle)

        activityOwner.moveTo(Lifecycle.State.STARTED)
        binder.attach(activityOwner, "p1", view,  shouldShow = true)
        binder.attach(activityOwner, "p2", view2, shouldShow = true)
        activityOwner.moveTo(Lifecycle.State.DESTROYED)

        verify(exactly = 1) { repo.releaseAd("p1") }
        verify(exactly = 1) { repo.releaseAd("p2") }
    }

    // ─── Back-navigation detection ────────────────────────────────────────────

    @Test
    fun `back navigation - process stays foreground - forceReload triggered`() {
        activityOwner.moveTo(Lifecycle.State.STARTED)
        binder.attach(activityOwner, "p", view, shouldShow = true, autoReloadOnStart = true)

        // Simulate: user went to another Activity (activity stops, process stays foreground)
        activityOwner.moveTo(Lifecycle.State.CREATED)   // onStop() → STOPPED_FOREGROUND
        // processOwner.onStop() is NOT called → process stayed in foreground

        activityOwner.moveTo(Lifecycle.State.STARTED)   // onStart() → STOPPED_FOREGROUND → reload

        verify(exactly = 1) { repo.loadAd("p", forceReload = true) }
    }

    @Test
    fun `home button - process goes background - forceReload NOT triggered`() {
        activityOwner.moveTo(Lifecycle.State.STARTED)
        binder.attach(activityOwner, "p", view, shouldShow = true, autoReloadOnStart = true)

        activityOwner.moveTo(Lifecycle.State.CREATED)   // onStop() → STOPPED_FOREGROUND
        processOwner.moveTo(Lifecycle.State.CREATED)    // Process.onStop() → STOPPED_BACKGROUND

        activityOwner.moveTo(Lifecycle.State.STARTED)   // onStart() → STOPPED_BACKGROUND → skip

        verify(exactly = 0) { repo.loadAd("p", forceReload = true) }
    }

    @Test
    fun `fresh create - state is INIT - forceReload NOT triggered`() {
        // Activity starts for the first time — never went through onStop
        binder.attach(activityOwner, "p", view, shouldShow = true, autoReloadOnStart = true)
        activityOwner.moveTo(Lifecycle.State.STARTED)   // INIT → no reload

        verify(exactly = 0) { repo.loadAd("p", forceReload = true) }
    }

    @Test
    fun `autoReloadOnStart=false - back navigation - forceReload NOT triggered`() {
        activityOwner.moveTo(Lifecycle.State.STARTED)
        binder.attach(activityOwner, "p", view, shouldShow = true, autoReloadOnStart = false)

        activityOwner.moveTo(Lifecycle.State.CREATED)
        activityOwner.moveTo(Lifecycle.State.STARTED)

        verify(exactly = 0) { repo.loadAd("p", forceReload = true) }
    }

    @Test
    fun `back navigation - multiple placements - all reloaded`() {
        val view2 = mockk<NativeAdViewAd>(relaxed = true)
        every { repo.getAdStateFlow("p2") } returns MutableStateFlow(NativeAdLoadState.Idle)

        activityOwner.moveTo(Lifecycle.State.STARTED)
        binder.attach(activityOwner, "p1", view,  shouldShow = true, autoReloadOnStart = true)
        binder.attach(activityOwner, "p2", view2, shouldShow = true, autoReloadOnStart = true)

        activityOwner.moveTo(Lifecycle.State.CREATED)
        activityOwner.moveTo(Lifecycle.State.STARTED)

        verify(exactly = 1) { repo.loadAd("p1", forceReload = true) }
        verify(exactly = 1) { repo.loadAd("p2", forceReload = true) }
    }

    @Test
    fun `back twice - each nav triggers exactly one reload`() {
        activityOwner.moveTo(Lifecycle.State.STARTED)
        binder.attach(activityOwner, "p", view, shouldShow = true, autoReloadOnStart = true)

        // First back nav
        activityOwner.moveTo(Lifecycle.State.CREATED)
        activityOwner.moveTo(Lifecycle.State.STARTED)

        // Second back nav
        activityOwner.moveTo(Lifecycle.State.CREATED)
        activityOwner.moveTo(Lifecycle.State.STARTED)

        verify(exactly = 2) { repo.loadAd("p", forceReload = true) }
    }

    // ─── Fake lifecycle ───────────────────────────────────────────────────────

    private class FakeLifecycleOwner : LifecycleOwner {
        private val registry = LifecycleRegistry.createUnsafe(this)
        override val lifecycle: Lifecycle get() = registry
        fun moveTo(state: Lifecycle.State) { registry.currentState = state }
    }
}
