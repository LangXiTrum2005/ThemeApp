package com.ads.admob.native_ads

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ads.admob.MMonetize
import com.ads.admob.native_ads.adapter.NativeAdViewAd
import com.ads.admob.native_ads.adapter.NativeDisplayController
import kotlinx.coroutines.launch

/**
 * One instance per LifecycleOwner (Activity or Fragment).
 *
 * Back-navigation detection is fully self-contained — callers never need to
 * call any external API. The mechanism uses [ProcessLifecycleOwner] to
 * distinguish two cases that both fire [onStop] on the owner:
 *
 *   Back navigation  → owner.onStop() … owner.onStart()
 *                       Process stays STARTED throughout → reload
 *
 *   Home button      → owner.onStop() → Process.onStop() … owner.onStart()
 *                       Process went STOPPED → no reload
 *
 *   Fresh create     → owner never went through onStop → no reload
 *   App killed+reopen→ new instance, state is INIT → no reload
 */
class NativeAdBinder(
    private val repository: NativeAdRepository = MMonetize.nativeAdRepository,
    private val processLifecycle: Lifecycle = ProcessLifecycleOwner.get().lifecycle,
) {

    private val activePlacements     = mutableSetOf<String>()
    private val autoReloadPlacements = mutableSetOf<String>()
    private var autoReloadObserverRegistered = false

    // ─── Back-nav state machine ───────────────────────────────────────────────

    private enum class BackNavState {
        /** Activity has not been stopped since creation / last onStart. */
        INIT,
        /** Activity stopped while process was still in foreground. */
        STOPPED_FOREGROUND,
        /** Activity stopped AND process went to background. */
        STOPPED_BACKGROUND,
    }

    private var backNavState = BackNavState.INIT
    private var processObserver: DefaultLifecycleObserver? = null

    // ─── Public API ───────────────────────────────────────────────────────────

    fun attach(
        lifecycleOwner: LifecycleOwner,
        placement: String,
        view: NativeAdViewAd,
        shouldShow: Boolean = true,
        autoLoad: Boolean = false,
        autoReloadOnStart: Boolean = false,
        preloadOnClick: Boolean = false,
    ) {
        activePlacements.add(placement)
        view.shouldShowAd = shouldShow
        repository.setPreloadOnClick(placement, preloadOnClick)
        if (autoLoad && shouldShow) repository.loadAd(placement)

        val controller = NativeDisplayController(view)

        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.getAdStateFlow(placement).collect { state ->
                    controller.onStateChanged(state)
                }
            }
        }

        if (autoReloadOnStart) {
            autoReloadPlacements.add(placement)
            if (!autoReloadObserverRegistered) {
                autoReloadObserverRegistered = true
                registerBackNavObserver(lifecycleOwner)
            }
        }

        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                activePlacements.remove(placement)
                autoReloadPlacements.remove(placement)
                controller.cleanup()
                repository.releaseAd(placement)
                owner.lifecycle.removeObserver(this)
            }
        })
    }

    /**
     * Releases all active placements immediately.
     * Call when VIP is purchased so native ads disappear without waiting for lifecycle events.
     * Each placement emits [NativeAdLoadState.Idle] → [NativeDisplayController] hides the view.
     */
    fun releaseAllPlacements() {
        activePlacements.toList().forEach { repository.releaseAd(it) }
    }

    // ─── Internals ────────────────────────────────────────────────────────────

    /**
     * Registers two observers:
     * 1. On [ProcessLifecycleOwner] — detects when the app goes to background.
     * 2. On the [lifecycleOwner] — tracks stop/start to decide if reload is needed.
     *
     * State transitions:
     *   onStop()          → STOPPED_FOREGROUND  (assume back nav until proven otherwise)
     *   Process.onStop()  → STOPPED_BACKGROUND  (home/recents proved it's not back nav)
     *   onStart()         → reload if STOPPED_FOREGROUND, then reset to INIT
     *   onDestroy()       → remove process observer to prevent leak
     */
    private fun registerBackNavObserver(lifecycleOwner: LifecycleOwner) {
        val procObserver = object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                if (backNavState == BackNavState.STOPPED_FOREGROUND) {
                    backNavState = BackNavState.STOPPED_BACKGROUND
                }
            }
        }
        processObserver = procObserver
        processLifecycle.addObserver(procObserver)

        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                backNavState = BackNavState.STOPPED_FOREGROUND
            }

            override fun onStart(owner: LifecycleOwner) {
                if (backNavState == BackNavState.STOPPED_FOREGROUND) {
                    autoReloadPlacements.forEach { repository.loadAd(it, forceReload = true) }
                }
                backNavState = BackNavState.INIT
            }

            override fun onDestroy(owner: LifecycleOwner) {
                processObserver?.let {
                    processLifecycle.removeObserver(it)
                }
                processObserver = null
            }
        })
    }
}
