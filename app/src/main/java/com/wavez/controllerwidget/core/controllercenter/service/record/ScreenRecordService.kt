package com.wavez.controllerwidget.core.controllercenter.service.record

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.wavez.controllerwidget.R
import com.wavez.controllerwidget.core.extensions.getParcel
import java.io.FileDescriptor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScreenRecordService : Service() {

    private var mediaProjection: MediaProjection? = null
    private var mediaRecorder: MediaRecorder? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var outputUri: Uri? = null
    private var outputDescriptor: ParcelFileDescriptor? = null
    private var isPrepared = false
    private var projectionCallback: MediaProjection.Callback? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopRecording(saveFile = true)
                Toast.makeText(
                    this,
                    R.string.screen_recording_saved,
                    Toast.LENGTH_SHORT
                ).show()
                stopSelf()
            }

            ACTION_START -> startRecording(intent)
        }
        return START_NOT_STICKY
    }

    private fun startRecording(intent: Intent) {
        if (recording) return

        val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
        val resultData = intent.getParcel<Intent>(EXTRA_RESULT_DATA) ?: return

        startForegroundCompat()

        runCatching {
            val projectionManager = getSystemService(MediaProjectionManager::class.java)
            val projection = projectionManager.getMediaProjection(resultCode, resultData)
                ?: error("MediaProjection is null")
            val videoSpec = getVideoSpec()
            val uri = createOutputUri()
            outputUri = uri
            val descriptor = contentResolver.openFileDescriptor(uri, "w")
                ?: error("Cannot open output file")
            outputDescriptor = descriptor
            val recorder = createRecorder(descriptor.fileDescriptor, videoSpec)
            mediaRecorder = recorder
            val callback = object : MediaProjection.Callback() {
                override fun onStop() {
                    stopRecording(saveFile = true)
                    stopSelf()
                }
            }
            projection.registerCallback(callback, Handler(Looper.getMainLooper()))

            val display = projection.createVirtualDisplay(
                "ControllerWidgetScreenRecord",
                videoSpec.width,
                videoSpec.height,
                videoSpec.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                recorder.surface,
                null,
                null
            )
            recorder.start()

            mediaProjection = projection
            virtualDisplay = display
            projectionCallback = callback
            isPrepared = true
            recording = true
            sendRecordingStateChanged(true)
        }.onFailure {
            stopRecording(saveFile = false)
            stopSelf()
        }
    }

    private fun startForegroundCompat() {
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createRecorder(fileDescriptor: FileDescriptor, spec: VideoSpec): MediaRecorder {
        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        recorder.setVideoSize(spec.width, spec.height)
        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        recorder.setVideoEncodingBitRate(spec.bitRate)
        recorder.setVideoFrameRate(FRAME_RATE)
        recorder.setOutputFile(fileDescriptor)
        recorder.prepare()
        return recorder
    }

    private fun getVideoSpec(): VideoSpec {
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        (getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay.getRealMetrics(metrics)

        val width = metrics.widthPixels.coerceAtLeast(2).toEven()
        val height = metrics.heightPixels.coerceAtLeast(2).toEven()
        val bitRate = (width * height * 6).coerceIn(4_000_000, 18_000_000)
        return VideoSpec(width, height, metrics.densityDpi, bitRate)
    }

    private fun createOutputUri(): Uri {
        val name = "Screen_Record_${
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        }.mp4"
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/ScreenRecordings")
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }
        return requireNotNull(
            contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
        )
    }

    private fun createNotification(): Notification {
        val stopIntent = PendingIntent.getService(
            this,
            0,
            stopIntent(this),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.screen_recording_title))
            .setContentText(getString(R.string.screen_recording_message))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(
                R.drawable.ic_notification,
                getString(R.string.screen_recording_stop),
                stopIntent
            )
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.screen_recording_channel),
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun stopRecording(saveFile: Boolean) {
        val uri = outputUri
        runCatching {
            if (isPrepared) {
                mediaRecorder?.stop()
            }
        }
        runCatching { mediaRecorder?.reset() }
        runCatching { mediaRecorder?.release() }
        runCatching { virtualDisplay?.release() }
        runCatching {
            projectionCallback?.let { callback ->
                mediaProjection?.unregisterCallback(callback)
            }
        }
        runCatching { mediaProjection?.stop() }
        runCatching { outputDescriptor?.close() }

        mediaRecorder = null
        virtualDisplay = null
        mediaProjection = null
        outputUri = null
        outputDescriptor = null
        projectionCallback = null
        isPrepared = false
        recording = false
        sendRecordingStateChanged(false)

        if (uri != null) {
            if (saveFile) {
                publishOutput(uri)
            } else {
                runCatching { contentResolver.delete(uri, null, null) }
            }
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun sendRecordingStateChanged(isRecording: Boolean) {
        sendBroadcast(
            Intent(ACTION_RECORDING_STATE_CHANGED).apply {
                putExtra(EXTRA_IS_RECORDING, isRecording)
            }
        )
    }

    private fun publishOutput(uri: Uri) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.IS_PENDING, 0)
        }
        contentResolver.update(uri, values, null, null)
    }

    override fun onDestroy() {
        stopRecording(saveFile = true)
        super.onDestroy()
    }

    private fun Int.toEven(): Int = if (this % 2 == 0) this else this - 1

    private data class VideoSpec(
        val width: Int,
        val height: Int,
        val densityDpi: Int,
        val bitRate: Int
    )

    companion object {
        private const val ACTION_START =
            "com.wavez.themewallpaper.screenrecord.action.START"
        private const val ACTION_STOP =
            "com.wavez.themewallpaper.screenrecord.action.STOP"
        const val ACTION_RECORDING_STATE_CHANGED =
            "com.wavez.themewallpaper.screenrecord.action.RECORDING_STATE_CHANGED"
        const val EXTRA_IS_RECORDING = "extra_is_recording"
        private const val EXTRA_RESULT_CODE = "extra_result_code"
        private const val EXTRA_RESULT_DATA = "extra_result_data"
        private const val CHANNEL_ID = "screen_recording"
        private const val NOTIFICATION_ID = 5201
        private const val FRAME_RATE = 30

        @Volatile
        private var recording = false

        fun isRecording(): Boolean = recording

        fun startIntent(context: Context, resultCode: Int, resultData: Intent): Intent {
            return Intent(context, ScreenRecordService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_RESULT_CODE, resultCode)
                putExtra(EXTRA_RESULT_DATA, resultData)
            }
        }

        fun stopIntent(context: Context): Intent {
            return Intent(context, ScreenRecordService::class.java).apply {
                action = ACTION_STOP
            }
        }
    }
}
