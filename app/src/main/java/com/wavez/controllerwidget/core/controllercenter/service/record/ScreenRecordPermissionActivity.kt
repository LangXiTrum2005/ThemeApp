package com.wavez.controllerwidget.core.controllercenter.service.record

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.wavez.controllerwidget.R
import com.wavez.controllerwidget.core.controllercenter.service.ControlCenterService

class ScreenRecordPermissionActivity : Activity() {

    private val handler = Handler(Looper.getMainLooper())
    private var waitingNotificationSettings = false
    private var flowStarted = false

    private val mediaProjectionManager by lazy {
        getSystemService(MediaProjectionManager::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ScreenRecordService.isRecording()) {
            startService(ScreenRecordService.stopIntent(this))
            finish()
            return
        }

        val startDelayMs = intent.getLongExtra(EXTRA_START_DELAY_MS, 0L)
        handler.postDelayed({
            hideControlPanel()
            startPermissionFlow()
        }, startDelayMs)
    }

    override fun onResume() {
        super.onResume()
        if (!waitingNotificationSettings) return

        if (shouldRequestNotificationPermission()) {
            return
        }

        waitingNotificationSettings = false
        requestScreenCapturePermission()
    }

    private fun startPermissionFlow() {
        if (flowStarted) return
        flowStarted = true

        if (shouldRequestNotificationPermission()) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_POST_NOTIFICATIONS
            )
            return
        }

        requestScreenCapturePermission()
    }

    private fun requestScreenCapturePermission() {
        runCatching {
            startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(),
                REQUEST_SCREEN_CAPTURE
            )
        }.onFailure {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SCREEN_CAPTURE) {
            if (resultCode == RESULT_OK && data != null) {
                ContextCompat.startForegroundService(
                    this,
                    ScreenRecordService.startIntent(this, resultCode, data)
                )
            } else {
                sendRecordingStateChanged(false)
            }
        }
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != REQUEST_POST_NOTIFICATIONS) return

        if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            requestScreenCapturePermission()
        } else {
            Toast.makeText(
                this,
                R.string.screen_recording_notification_required,
                Toast.LENGTH_SHORT
            ).show()
            waitingNotificationSettings = true
            openNotificationSettings()
        }
    }

    private fun shouldRequestNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
    }

    private fun hideControlPanel() {
        sendBroadcast(
            Intent(ControlCenterService.ACTION_ACTIVE_CONTROL_PANEL).apply {
                putExtra(ControlCenterService.EXTRA_SHOW, false)
            }
        )
    }

    private fun openNotificationSettings() {
        startActivity(
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        )
    }

    private fun sendRecordingStateChanged(isRecording: Boolean) {
        sendBroadcast(
            Intent(ScreenRecordService.ACTION_RECORDING_STATE_CHANGED).apply {
                putExtra(ScreenRecordService.EXTRA_IS_RECORDING, isRecording)
            }
        )
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    companion object {
        private const val REQUEST_SCREEN_CAPTURE = 2105
        private const val REQUEST_POST_NOTIFICATIONS = 2106
        private const val EXTRA_START_DELAY_MS = "extra_start_delay_ms"

        fun start(context: Context, startDelayMs: Long = 0L) {
            context.startActivity(
                Intent(context, ScreenRecordPermissionActivity::class.java)
                    .putExtra(EXTRA_START_DELAY_MS, startDelayMs)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
