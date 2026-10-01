package com.wavez.themewallpaper.core.controllercenter.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import com.wavez.themewallpaper.core.controllercenter.controllers.BluetoothController
import com.wavez.themewallpaper.core.controllercenter.controllers.BrightnessController
import com.wavez.themewallpaper.core.controllercenter.controllers.FlashlightController
import com.wavez.themewallpaper.core.controllercenter.controllers.MediaController
import com.wavez.themewallpaper.core.controllercenter.controllers.NetworkController
import com.wavez.themewallpaper.core.controllercenter.controllers.VolumeController
import com.wavez.themewallpaper.core.controllercenter.model.ControllerConfigModel
import com.wavez.themewallpaper.core.controllercenter.utils.UtilsKt
import com.wavez.themewallpaper.core.controllercenter.view.PullDirection
import com.wavez.themewallpaper.core.controllercenter.view.PullingListener
import com.wavez.themewallpaper.databinding.LayoutPullBarBinding
import com.wavez.themewallpaper.databinding.SceneControlCenterBinding
import kotlin.getValue

class ControlCenterOverlay(private val context: Context) {

    companion object {
        private const val TAG = "ControlCenterOverlay"
        private const val PULL_THRESHOLD = 80f
    }

    private val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private lateinit var pullBarBinding: LayoutPullBarBinding
    private lateinit var sceneBinding: SceneControlCenterBinding

    private var isPullBarAttached = false
    private var isPanelAttached = false
    private var isPanelVisible = false
    private var panelParams: WindowManager.LayoutParams? = null
    private val focusHandler = Handler(Looper.getMainLooper())
    private var focusLockRunnable: Runnable? = null
    private val touchSlop by lazy {
        ViewConfiguration.get(context).scaledTouchSlop.toFloat()
    }

    // ─── Public API ───────────────────────────────────────────────

    fun init(config: ControllerConfigModel) {
        initPullBar(config)
        initScene(config)
    }

    fun bindControllers(
        network: NetworkController,
        bluetooth: BluetoothController,
        flashlight: FlashlightController,
        media: MediaController,
        volume: VolumeController,
        brightness: BrightnessController
    ) {
        sceneBinding.main.bindControllers(
            network = network,
            bluetooth = bluetooth,
            flashlight = flashlight,
            media = media,
            volume = volume,
            brightness = brightness
        )
    }

    fun show() = showPanel()

    fun hide() = hidePanel()

    // Reload the overlay views in-place with a new theme config, without detaching from WindowManager.
    fun reload(config: ControllerConfigModel) {
        pullBarBinding.pullBarLayout.alpha = config.controllerJsonConfig.pullBarAlpha.coerceIn(0.3f, 1f)
        UtilsKt.loadImageOptimizeRam(sceneBinding.background, config.homeBackground)
        sceneBinding.main.initData(config)
        Log.d(TAG, "Theme reloaded: ${config.key}")
    }

    fun destroy() {
        focusLockRunnable?.let(focusHandler::removeCallbacks)
        detachPullBar()
        detachPanel()
    }

    // ─── Pull bar ─────────────────────────────────────────────────

    private fun initPullBar(config: ControllerConfigModel) {
        pullBarBinding = LayoutPullBarBinding.inflate(LayoutInflater.from(context))

        pullBarBinding.pullBarLayout.alpha = config.controllerJsonConfig.pullBarAlpha.coerceIn(0.3f, 1f)

        pullBarBinding.pullBarLayout.setPullDirection(PullDirection.DOWN)
        pullBarBinding.pullBarLayout.setPullingListener(object : PullingListener {
            override fun onPull(distance: Float, isForward: Boolean) {
                Log.d(TAG, "onPull: distance=$distance, isForward=$isForward")
                if (!isForward) {
                    return
                }
                if (distance < touchSlop * 2f) {
                    return
                }
                if (!isPanelVisible) showPanel()
            }

            override fun onRelease(distance: Float) {}
            override fun onCancel(distance: Float) {}
        })

        attachPullBar()
    }

    private fun attachPullBar() {
        if (isPullBarAttached) return
        val density = context.resources.displayMetrics.density

        val barWidth = (getScreenWidth() * 0.3f).toInt()
            .coerceAtLeast((density * 96).toInt())
        val barHeight = (20 * density).toInt()
        val params = WindowManager.LayoutParams(
            barWidth,
            barHeight,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            overlayFlags(),
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 0
            y = 0
            layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        try {
            wm.addView(pullBarBinding.root, params)
            isPullBarAttached = true
            Log.d(TAG, "Pull bar attached OK")
        } catch (e: Exception) {
            Log.e(TAG, "Pull bar attach FAILED", e)
        }
    }

    private fun detachPullBar() {
        if (!isPullBarAttached) return
        runCatching { wm.removeView(pullBarBinding.root) }
        isPullBarAttached = false
    }

    // ─── Scene panel ──────────────────────────────────────────────

    private fun initScene(config: ControllerConfigModel) {
        sceneBinding = SceneControlCenterBinding.inflate(LayoutInflater.from(context))

        UtilsKt.loadImageOptimizeRam(sceneBinding.background, config.homeBackground)
        sceneBinding.main.initData(config)
        sceneBinding.main.setDismissListener { hidePanel() }
        sceneBinding.main.setFocusClickListener { showFocusDurations() }
        bindFocusDurations()

        sceneBinding.pullRoot.setPullDirection(PullDirection.UP)
        sceneBinding.pullRoot.setPullingListener(object : PullingListener {
            override fun onPull(distance: Float, isForward: Boolean) {}
            override fun onRelease(distance: Float) {
                if (isPanelVisible && distance > PULL_THRESHOLD) hidePanel()
            }

            override fun onCancel(distance: Float) {}
        })

        attachPanel()
        collapsePanelWindow()
    }

    private fun showPanel() {
        if (isPanelVisible) return
        isPanelVisible = true
        Log.d(TAG, "showPanel")

        attachPanel()
        expandPanelWindow()
        sceneBinding.background.visibility = View.VISIBLE
        sceneBinding.main.alpha = 0f
        sceneBinding.main.translationY = -60f
        sceneBinding.main.visibility = View.VISIBLE

        sceneBinding.main.animate().alpha(1f).translationY(0f).setDuration(280).start()
    }

    private fun hidePanel() {
        if (!isPanelVisible) return
        isPanelVisible = false
        sceneBinding.focusDurationPanel.visibility = View.GONE
        sceneBinding.main.animate()
            .alpha(0f).translationY(-60f).setDuration(220)
            .withEndAction { collapsePanelWindow() }
            .start()
    }

    private fun attachPanel() {
        if (isPanelAttached) return
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            1,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            overlayFlags(),
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 0
            y = 0
            layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        try {
            wm.addView(sceneBinding.root, params)
            panelParams = params
            isPanelAttached = true
            Log.d(TAG, "Panel attached OK")
        } catch (e: Exception) {
            Log.e(TAG, "Panel attach FAILED", e)
            isPanelVisible = false
        }
    }

    private fun expandPanelWindow() {
        val params = panelParams ?: return
        params.height = getScreenHeight()
        sceneBinding.root.requestLayout()
        runCatching { wm.updateViewLayout(sceneBinding.root, params) }
            .onFailure { Log.e(TAG, "Panel expand FAILED", it) }
    }

    private fun collapsePanelWindow() {
        sceneBinding.main.visibility = View.GONE
        sceneBinding.background.visibility = View.GONE
        sceneBinding.main.alpha = 1f
        sceneBinding.main.translationY = 0f
        val params = panelParams ?: return
        params.height = 1
        sceneBinding.root.requestLayout()
        runCatching { wm.updateViewLayout(sceneBinding.root, params) }
            .onFailure { Log.e(TAG, "Panel collapse FAILED", it) }
    }

    private fun detachPanel() {
        sceneBinding.focusDurationPanel.visibility = View.GONE
        sceneBinding.main.visibility = View.GONE
        sceneBinding.background.visibility = View.GONE
        sceneBinding.main.alpha = 1f
        sceneBinding.main.translationY = 0f
        if (isPanelAttached) {
            runCatching { wm.removeView(sceneBinding.root) }
            isPanelAttached = false
            panelParams = null
        }
    }

    // ─── Utilities ────────────────────────────────────────────────

    private fun bindFocusDurations() {
        sceneBinding.focus15s.setOnClickListener { scheduleScreenLock(15_000L) }
        sceneBinding.focus30s.setOnClickListener { scheduleScreenLock(30_000L) }
        sceneBinding.focus1m.setOnClickListener { scheduleScreenLock(60_000L) }
        sceneBinding.focus2m.setOnClickListener { scheduleScreenLock(2 * 60_000L) }
        sceneBinding.focus5m.setOnClickListener { scheduleScreenLock(5 * 60_000L) }
        sceneBinding.focus10m.setOnClickListener { scheduleScreenLock(10 * 60_000L) }
        sceneBinding.focus30m.setOnClickListener { scheduleScreenLock(30 * 60_000L) }
    }

    private fun showFocusDurations() {
        sceneBinding.focusDurationPanel.visibility = View.VISIBLE
    }

    private fun scheduleScreenLock(delayMillis: Long) {
        focusLockRunnable?.let(focusHandler::removeCallbacks)
        focusLockRunnable = Runnable {
            (context as? AccessibilityService)
                ?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
            focusLockRunnable = null
        }.also { focusHandler.postDelayed(it, delayMillis) }
        hidePanel()
    }

    private fun overlayFlags(): Int =
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_SPLIT_TOUCH or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED

    private fun getScreenWidth(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            wm.currentWindowMetrics.bounds.width()
        } else {
            context.resources.displayMetrics.widthPixels
        }

    private fun getScreenHeight(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            wm.currentWindowMetrics.bounds.height()
        } else {
            context.resources.displayMetrics.heightPixels
        }
}
