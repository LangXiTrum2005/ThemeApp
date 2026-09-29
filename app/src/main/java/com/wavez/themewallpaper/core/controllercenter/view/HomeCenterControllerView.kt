package com.wavez.themewallpaper.core.controllercenter.view

import android.accessibilityservice.AccessibilityService
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.toColorInt
import com.wavez.themewallpaper.core.controllercenter.PermissionHelper
import com.wavez.themewallpaper.core.controllercenter.controllers.BluetoothController
import com.wavez.themewallpaper.core.controllercenter.controllers.BrightnessController
import com.wavez.themewallpaper.core.controllercenter.controllers.FlashlightController
import com.wavez.themewallpaper.core.controllercenter.controllers.MediaController
import com.wavez.themewallpaper.core.controllercenter.controllers.NetworkController
import com.wavez.themewallpaper.core.controllercenter.controllers.RingerMode
import com.wavez.themewallpaper.core.controllercenter.controllers.VolumeController
import com.wavez.themewallpaper.core.controllercenter.model.ControllerConfigModel
import com.wavez.themewallpaper.core.controllercenter.service.record.ScreenRecordPermissionActivity
import com.wavez.themewallpaper.core.controllercenter.service.record.ScreenRecordService
import com.wavez.themewallpaper.core.controllercenter.utils.UtilsKt
import com.wavez.themewallpaper.core.controllercenter.view.SeekBarControlView.OnProgressChangeListener
import com.wavez.themewallpaper.core.extensions.onAvoidDoubleClick
import com.wavez.themewallpaper.core.extensions.openCalculatorApp
import com.wavez.themewallpaper.core.extensions.openStopwatchApp
import com.wavez.themewallpaper.databinding.LayoutControllerCenterBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

class HomeCenterControllerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val START_RECORD_DELAY_MS = 3_000L
        const val TAG = "HomeCenterControllerView"
    }

    private val binding =
        LayoutControllerCenterBinding.inflate(LayoutInflater.from(context), this, true)

    private var config = ControllerConfigModel()
    private var onDismiss: () -> Unit = {}
    private var boundMedia: MediaController? = null
    private var boundBrightness: BrightnessController? = null
    private var boundVolume: VolumeController? = null
    private var suppressNextAutoRotateObserver = false
    private var writeSettingsRequestOpening = false

    private val screenRecordStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != ScreenRecordService.ACTION_RECORDING_STATE_CHANGED) return
            val isRecording = intent.getBooleanExtra(ScreenRecordService.EXTRA_IS_RECORDING, false)
            post { syncRecordVideoState(isRecording, playAnim = true) }
        }
    }

    private val autoRotateObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            if (suppressNextAutoRotateObserver) {
                suppressNextAutoRotateObserver = false
                return
            }
            binding.autoRotate.setState(
                if (isAutoRotateOn()) AnimationImageView.State.Open else AnimationImageView.State.Close,
                playAnim = true
            )
        }
    }

    private val brightnessObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            val pct = boundBrightness?.getBrightness() ?: return
            post { binding.brightness.syncProgress(pct) }
        }
    }

    fun initData(model: ControllerConfigModel) {
        config = model
        applyTheme()
        boundBrightness?.let(::bindBrightness)
        boundVolume?.let(::bindVolume)
    }

    private fun applyTheme() {
        val m = config
        // --- Card backgrounds ---
        binding.networkLayout.loadBackgroundFromPath(m.homeControlBackground)
        binding.musicLayout.loadBackgroundFromPath(m.homeMusicBackground)
        UtilsKt.loadImageOptimizeRam(binding.bottomDiy, m.bottomDiyBackground)

        // --- Network icons (off state by default) ---
        UtilsKt.loadImageOptimizeRam(binding.wifi, m.wifiIcon)
        UtilsKt.loadImageOptimizeRam(binding.airplane, m.airplaneIcon)
        UtilsKt.loadImageOptimizeRam(binding.mobileData, m.mobileDataIcon)
        UtilsKt.loadImageOptimizeRam(binding.blueTooth, m.bluetoothIcon)

        // --- Music text colors ---
        val cfg = m.controllerJsonConfig
        binding.musicInfo.song.apply {
            setTextColor(parseColor(cfg.musicKnownTitleColor, Color.WHITE))
            alpha = cfg.musicKnownTitleAlpha
        }
        binding.musicInfo.singer.apply {
            setTextColor(parseColor(cfg.musicKnownSingerColor, Color.WHITE))
            alpha = cfg.musicKnownSingerAlpha
        }

        // --- Music controller icons ---
        UtilsKt.loadImageOptimizeRam(binding.musicController.next, m.musicNextIcon)
        UtilsKt.loadImageOptimizeRam(binding.musicController.playPause, m.musicPlayIcon)
        UtilsKt.loadImageOptimizeRam(binding.musicController.previous, m.musicPreviousIcon)


        // --- AnimationImageView: second row toggles ---
        // lockingIcon = small rotation-lock circle → fits the 65×65 auto_rotate slot
        binding.autoRotate.initData(
            openPath = m.lockingSelectedIcon,
            closePath = m.lockingIcon,
            openAnim = "",
            closeAnim = "",
            initState = AnimationImageView.State.Close,
            playAnim = false
        )
        binding.mute.initData(
            openPath = m.ringSelectedIcon,
            closePath = m.ringIcon,
            openAnim = "",
            closeAnim = "",
            initState = AnimationImageView.State.Close,
            playAnim = false
        )
        // focusIcon = wide Focus-mode pill → fits the full-width doNotDisturb slot
        binding.doNotDisturb.initData(
            openPath = m.focusIcon,
            closePath = m.focusSelectedIcon,
            openAnim = "",
            closeAnim = "",
            initState = AnimationImageView.State.Close,
            playAnim = false
        )

        // --- Tool icons: row 1 ---
        binding.flash.initData(
            openPath = m.flashlightSelectedIcon,
            closePath = m.flashlightIcon,
            openAnim = "",
            closeAnim = "",
            initState = AnimationImageView.State.Close,
            playAnim = false
        )
        UtilsKt.loadImageOptimizeRam(binding.timing, m.timingIcon)
        UtilsKt.loadImageOptimizeRam(binding.calculator, m.calculatorIcon)
        UtilsKt.loadImageOptimizeRam(binding.camera, m.cameraIcon)

        // --- Tool icons: row 2 ---
        UtilsKt.loadImageOptimizeRam(binding.recordAudio, m.recordingIcon)

        binding.recordVideo.initData(
            openPath = m.recordSelectedIcon,
            closePath = m.recordIcon,
            openAnim = m.recordOpenAnimation,
            closeAnim = m.recordCloseAnimation,
            initState = AnimationImageView.State.Close,
            playAnim = false
        )
        UtilsKt.loadImageOptimizeRam(binding.battery, m.batteryIcon)
        UtilsKt.loadImageOptimizeRam(binding.screenShot, m.screenshotIcon)

        // --- Tool icons: row 3 ---
        UtilsKt.loadImageOptimizeRam(binding.setup, m.setupIcon)

    }

    // ================= BIND CONTROLLERS =================

    fun bindControllers(
        network: NetworkController,
        bluetooth: BluetoothController,
        flashlight: FlashlightController,
        media: MediaController,
        volume: VolumeController,
        brightness: BrightnessController
    ) {
        bindNetwork(network, bluetooth)
        bindFlashlight(flashlight)
        bindMedia(media)
        bindVolume(volume)
        bindBrightness(brightness)
        bindStaticActions(volume)
    }

    private fun bindNetwork(network: NetworkController, bluetooth: BluetoothController) {
        // Sync initial states
        updateNetworkIcon(
            binding.wifi,
            network.isWifiEnabled(),
            config.wifiIcon,
            config.wifiSelectedIcon
        )
        updateNetworkIcon(
            binding.airplane,
            network.isAirplaneModeOn(),
            config.airplaneIcon,
            config.airplaneSelectedIcon
        )
        updateNetworkIcon(
            binding.mobileData,
            network.isMobileDataEnabled(),
            config.mobileDataIcon,
            config.mobileDataSelectedIcon
        )
        updateNetworkIcon(
            binding.blueTooth,
            bluetooth.isEnabled(),
            config.bluetoothIcon,
            config.bluetoothSelectedIcon
        )

        // Click listeners
        binding.wifi.onAvoidDoubleClick {
            network.toggleWifi()
            dismiss()
        }
        binding.airplane.onAvoidDoubleClick {
            network.toggleAirplaneMode(context)
            dismiss()
        }
        binding.mobileData.onAvoidDoubleClick {
            network.openMobileDataSettings()
            dismiss()
        }
        binding.blueTooth.onAvoidDoubleClick {
            bluetooth.toggleBluetooth()
            dismiss()
        }

        // State callbacks
        network.onWifiStateChanged = { on ->
            post { updateNetworkIcon(binding.wifi, on, config.wifiIcon, config.wifiSelectedIcon) }
        }
        network.onAirplaneModeChanged = { on ->
            post {
                updateNetworkIcon(
                    binding.airplane,
                    on,
                    config.airplaneIcon,
                    config.airplaneSelectedIcon
                )
            }
        }
        network.onMobileDataStateChanged = { on ->
            post {
                updateNetworkIcon(
                    binding.mobileData,
                    on,
                    config.mobileDataIcon,
                    config.mobileDataSelectedIcon
                )
            }
        }
        bluetooth.onStateChanged = { on ->
            post {
                updateNetworkIcon(
                    binding.blueTooth,
                    on,
                    config.bluetoothIcon,
                    config.bluetoothSelectedIcon
                )
            }
        }
    }

    private fun bindFlashlight(flashlight: FlashlightController) {
        if (flashlight.isOn()) {
            binding.flash.setState(AnimationImageView.State.Open, playAnim = false)
        }

        binding.flash.onAvoidDoubleClick { flashlight.toggle() }

        flashlight.onTorchChanged = { on ->
            post {
                binding.flash.setState(
                    if (on) AnimationImageView.State.Open else AnimationImageView.State.Close,
                    playAnim = true
                )
            }
        }
    }

    private fun bindMedia(media: MediaController) {
        boundMedia = media

        binding.musicController.previous.onAvoidDoubleClick {
            runMediaAction(media) { previous() }
        }
        binding.musicController.next.onAvoidDoubleClick {
            runMediaAction(media) { next() }
        }
        binding.musicController.playPause.onAvoidDoubleClick {
            runMediaAction(media, syncAfterAction = false) {
                val shouldPlay = !isPlaying()
                if (shouldPlay) play() else pause()
                updateMediaPlaybackIcon(shouldPlay)
            }
        }

        syncMediaState(media, refreshSessions = true)

        media.onTrackChanged = { track ->
            post {
                binding.musicInfo.song.text = track?.title ?: ""
                binding.musicInfo.singer.text = track?.artist ?: ""
            }
        }
        media.onPlaybackStateChanged = { isPlaying ->
            post {
                updateMediaPlaybackIcon(isPlaying)
            }
        }
    }

    private fun runMediaAction(
        media: MediaController,
        syncAfterAction: Boolean = true,
        action: MediaController.() -> Unit
    ) {
        if (!PermissionHelper.hasNotificationAccess(context)) {
            PermissionHelper.openNotificationAccessSettings(context)
            dismiss()
            return
        }

        media.refreshSessions()
        media.action()
        if (syncAfterAction) {
            syncMediaState(media, refreshSessions = false)
        }
    }

    private fun syncMediaState(media: MediaController, refreshSessions: Boolean) {
        if (refreshSessions) media.refreshSessions()

        val track = media.getCurrentTrack()
        binding.musicInfo.song.text = track?.title ?: ""
        binding.musicInfo.singer.text = track?.artist ?: ""
        updateMediaPlaybackIcon(media.isPlaying())
    }

    private fun updateMediaPlaybackIcon(isPlaying: Boolean) {
        UtilsKt.loadImageOptimizeRam(
            binding.musicController.playPause,
            if (isPlaying) config.musicPlayIcon else config.musicPauseIcon
        )
    }

    private fun bindVolume(volume: VolumeController) {
        boundVolume = volume
        binding.volume.initData(
            iconActive = config.soundBigIcon,
            iconNormal = config.soundMuteIcon,
            progressForeground = config.soundSlider,
            background = config.soundBackground,
            progress = volume.getCurrentVolume(),
            maxProgress = 100
        )

        binding.volume.progressListener = object : OnProgressChangeListener {
            override fun onProgressChanged(progress: Int) {
                volume.setMediaVolume(progress)
            }

        }

        volume.onVolumeChanged = { v ->
            post { binding.volume.syncProgress(v) }
        }
    }

    private fun bindBrightness(brightness: BrightnessController) {
        boundBrightness = brightness
        binding.brightness.initData(
            iconActive = config.lightIcon,
            iconNormal = config.lightIcon,
            progressForeground = config.lightSlider,
            background = config.lightBackground,
            progress = brightness.getBrightness(),
            maxProgress = 100
        )
        binding.brightness.progressListener = object : OnProgressChangeListener {
            override fun onProgressChanged(progress: Int) {
                if (!brightness.canWriteSettings()) {
                    openWriteSettingsOnce()
                    return
                }
                brightness.setBrightness(progress)
            }
        }
    }

    private fun bindStaticActions(volume: VolumeController) {
        syncStaticStates(volume)

        binding.autoRotate.onAvoidDoubleClick { toggleAutoRotate() }
        binding.mute.onAvoidDoubleClick { toggleMute(volume) }
        binding.doNotDisturb.onAvoidDoubleClick { toggleDoNotDisturb() }

        binding.screenShot.onAvoidDoubleClick {
            (context as? AccessibilityService)
                ?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)
            dismiss()
        }
        binding.setup.onAvoidDoubleClick {
            openActivity(Intent(Settings.ACTION_SETTINGS))
            dismiss()
        }
        binding.camera.onAvoidDoubleClick {
            openActivity(Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA))
            dismiss()
        }
        binding.timing.onAvoidDoubleClick {
            if (context.openStopwatchApp()) dismiss()
        }
        binding.calculator.onAvoidDoubleClick {
            if (context.openCalculatorApp()) dismiss()
        }
        binding.recordAudio.onAvoidDoubleClick {
            openActivity(Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION))
            dismiss()
        }

        binding.recordVideo.onAvoidDoubleClick {
            if (ScreenRecordService.isRecording()) {
                binding.recordVideo.setState(AnimationImageView.State.Close, playAnim = true)
                context.startService(ScreenRecordService.stopIntent(context))
                dismiss()
            } else {
                binding.recordVideo.setState(AnimationImageView.State.Open, playAnim = true)
                ScreenRecordPermissionActivity.start(context, START_RECORD_DELAY_MS)
            }
        }

        binding.battery.onAvoidDoubleClick {
            openActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS))
            dismiss()
        }
    }

    // ================= HELPERS =================

    private fun updateNetworkIcon(view: ImageView, isOn: Boolean, offPath: String, onPath: String) {
        UtilsKt.loadImageOptimizeRam(view, if (isOn) onPath else offPath)
    }

    private fun View.loadBackgroundFromPath(path: String) {
        if (path.isEmpty()) return
        Glide.with(this)
            .load(path)
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?,
                ) {
                    background = resource
                }

                override fun onLoadCleared(placeholder: Drawable?) = Unit
            })
    }

    private fun parseColor(hex: String, fallback: Int): Int = try {
        hex.toColorInt()
    } catch (_: Exception) {
        fallback
    }

    private fun syncStaticStates(volume: VolumeController) {
        syncRecordVideoState(ScreenRecordService.isRecording(), playAnim = false)
        binding.autoRotate.setState(
            if (isAutoRotateOn()) AnimationImageView.State.Open else AnimationImageView.State.Close,
            playAnim = false
        )
        binding.mute.setState(
            if (volume.getRingerMode() == RingerMode.SILENT) {
                AnimationImageView.State.Open
            } else {
                AnimationImageView.State.Close
            },
            playAnim = false
        )
        binding.doNotDisturb.setState(
            if (isDoNotDisturbOn()) AnimationImageView.State.Close else AnimationImageView.State.Open,
            playAnim = false
        )
    }

    private fun toggleAutoRotate() {
        if (!Settings.System.canWrite(context)) {
            PermissionHelper.openWriteSettingsPage(context)
            dismiss()
            return
        }

        val enabled = !isAutoRotateOn()
        try {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.ACCELEROMETER_ROTATION,
                if (enabled) 1 else 0
            )
            binding.autoRotate.setState(
                if (enabled) AnimationImageView.State.Open else AnimationImageView.State.Close,
                playAnim = true
            )
        } catch (_: Exception) {
            openActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS))
        }
    }

    private fun isAutoRotateOn(): Boolean = try {
        Settings.System.getInt(
            context.contentResolver,
            Settings.System.ACCELEROMETER_ROTATION,
            0
        ) == 1
    } catch (_: Exception) {
        false
    }

    private fun toggleMute(volume: VolumeController) {
        val mute = volume.getRingerMode() != RingerMode.SILENT
        volume.setRingerMode(if (mute) RingerMode.SILENT else RingerMode.NORMAL)
        binding.mute.setState(
            if (mute) AnimationImageView.State.Open else AnimationImageView.State.Close,
            playAnim = true
        )
    }

    private fun toggleDoNotDisturb() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!manager.isNotificationPolicyAccessGranted) {
            openActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
            return
        }

        val enable = !isDoNotDisturbOn()
        try {
            manager.setInterruptionFilter(
                if (enable) {
                    NotificationManager.INTERRUPTION_FILTER_NONE
                } else {
                    NotificationManager.INTERRUPTION_FILTER_ALL
                }
            )
            binding.doNotDisturb.setState(
                if (enable) AnimationImageView.State.Close else AnimationImageView.State.Open,
                playAnim = true
            )
        } catch (_: Exception) {
            openActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
        }
    }

    private fun isDoNotDisturbOn(): Boolean = try {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
    } catch (_: Exception) {
        false
    }

    private fun openActivity(intent: Intent, swallowError: Boolean = false): Boolean {
        return try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            true
        } catch (e: Exception) {
            Log.d(TAG, "openActivity: Failed to open activity with intent: $intent", e)
            if (!swallowError) dismiss()
            false
        }
    }

    // ================= SHOW / HIDE / DISMISS =================

    fun show() {
        visibility = VISIBLE
        boundMedia?.let { syncMediaState(it, refreshSessions = true) }
    }

    fun setDismissListener(listener: () -> Unit) {
        onDismiss = listener
    }

    private fun dismiss() {
        onDismiss.invoke()
    }

    private fun openWriteSettingsOnce() {
        if (writeSettingsRequestOpening) return
        writeSettingsRequestOpening = true
        runCatching {
            PermissionHelper.openWriteSettingsPage(context)
        }.onFailure {
            Log.e(TAG, "openWriteSettingsOnce: failed", it)
        }
        dismiss()
    }

    private fun syncRecordVideoState(isRecording: Boolean, playAnim: Boolean) {
        binding.recordVideo.setState(
            if (isRecording) {
                AnimationImageView.State.Open
            } else {
                AnimationImageView.State.Close
            },
            playAnim = playAnim
        )
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        boundMedia?.let { syncMediaState(it, refreshSessions = true) }
        syncRecordVideoState(ScreenRecordService.isRecording(), playAnim = false)
        context.registerReceiver(
            screenRecordStateReceiver,
            IntentFilter(ScreenRecordService.ACTION_RECORDING_STATE_CHANGED),
            Context.RECEIVER_NOT_EXPORTED
        )
        context.contentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION),
            false,
            autoRotateObserver
        )
        context.contentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS),
            false,
            brightnessObserver
        )
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        runCatching { context.unregisterReceiver(screenRecordStateReceiver) }
        context.contentResolver.unregisterContentObserver(autoRotateObserver)
        context.contentResolver.unregisterContentObserver(brightnessObserver)
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (changedView == this && visibility == VISIBLE) {
            writeSettingsRequestOpening = false
            boundMedia?.let { syncMediaState(it, refreshSessions = true) }
            syncRecordVideoState(ScreenRecordService.isRecording(), playAnim = false)
            boundBrightness?.let { binding.brightness.syncProgress(it.getBrightness()) }
        }
    }
}
