package com.wavez.controllerwidget.core.icons

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.Icon
import android.os.SystemClock
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.wavez.controllerwidget.ui.iconpack.activities.ShortcutProxyActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class IconShortcutInstaller @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    private companion object {
        const val SHORTCUT_URI_SCHEME = "controllerwidget"
        const val SHORTCUT_URI_HOST = "shortcut"
        const val ACTION_SHORTCUT_PINNED = "com.wavez.controllerwidget.action.SHORTCUT_PINNED"

        private const val ICON_SIZE = 256
    }

    suspend fun installShortcut(
        targetPackageName: String,
        customIconUrl: String,
        label: String,
    ): Result<Unit> = runCatching {

        val shortcutManager = context.getSystemService(ShortcutManager::class.java)
        if (!shortcutManager.isRequestPinShortcutSupported) {
            error("Pinned shortcuts are not supported on this launcher")
        }

        val launchIntent = context.packageManager.getLaunchIntentForPackage(targetPackageName)
            ?: error("No launcher activity for $targetPackageName")
        val mainActivity = launchIntent.component?.className
            ?: error("Cannot find launcher activity for $targetPackageName")

        val src = loadIconBitmap(customIconUrl)

        val bitmap = try {
            createShortcutIconBitmap(src)
        } finally {
            if (!src.isRecycled) src.recycle()
        }


        val proxyComponent = ComponentName(context, ShortcutProxyActivity::class.java)
        val targetIntent = Intent(
            Intent.ACTION_VIEW,
            "$SHORTCUT_URI_SCHEME://$SHORTCUT_URI_HOST/$targetPackageName".toUri(),
        ).apply {
            component = proxyComponent
            putExtra(ShortcutProxyActivity.EXTRA_PACKAGE_NAME, targetPackageName)
            putExtra(ShortcutProxyActivity.EXTRA_MAIN_ACTIVITY, mainActivity)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val shortcutId = shortcutIdForPackage(targetPackageName)
        val shortcut = ShortcutInfo.Builder(context, shortcutId)
            .setShortLabel(label)
            .setLongLabel(label)
            .setActivity(proxyComponent)
            .setIcon(Icon.createWithBitmap(bitmap))
            .setIntent(targetIntent)
            .build()

        if (shortcutManager.pinnedShortcuts.any { it.id == shortcutId }) {
            shortcutManager.updateShortcuts(listOf(shortcut))
        } else {
            requestPinShortcut(shortcutManager, shortcut)
        }
    }

    fun isShortcutPinned(targetPackageName: String): Boolean {
        val shortcutManager = context.getSystemService(ShortcutManager::class.java)
        val shortcutId = shortcutIdForPackage(targetPackageName)
        return shortcutManager.pinnedShortcuts.any { it.id == shortcutId }
    }

    private suspend fun requestPinShortcut(
        shortcutManager: ShortcutManager,
        shortcut: ShortcutInfo,
    ) = suspendCancellableCoroutine { continuation ->
        val callbackAction =
            "$ACTION_SHORTCUT_PINNED.${shortcut.id}.${SystemClock.elapsedRealtimeNanos()}"
        val callbackIntent = Intent(callbackAction).setPackage(context.packageName)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action != callbackAction) return
                context.unregisterReceiver(this)
                if (continuation.isActive) {
                    continuation.resume(Unit)
                }
            }
        }

        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(callbackAction),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )

        continuation.invokeOnCancellation {
            runCatching { context.unregisterReceiver(receiver) }
        }

        val callback = PendingIntent.getBroadcast(
            context,
            callbackAction.hashCode(),
            callbackIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        ).intentSender

        if (!shortcutManager.requestPinShortcut(shortcut, callback)) {
            runCatching { context.unregisterReceiver(receiver) }
            if (continuation.isActive) {
                continuation.resumeWith(Result.failure(IllegalStateException("Shortcut pin request was rejected")))
            }
        }
    }

    private suspend fun loadIconBitmap(url: String): Bitmap =
        withContext(Dispatchers.IO) {
            Glide.with(context)
                .asBitmap()
                .load(url)
                .override(ICON_SIZE, ICON_SIZE)
                .centerInside()
                .format(DecodeFormat.PREFER_ARGB_8888)
                .disallowHardwareConfig()
                .submit(ICON_SIZE, ICON_SIZE)
                .get()
        }

    private fun createShortcutIconBitmap(src: Bitmap): Bitmap {
        val size = ICON_SIZE

        val output = createBitmap(size, size)
        val canvas = Canvas(output)

        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
            isDither = true
        }

        val rect = Rect(0, 0, size, size)

        canvas.drawBitmap(src, null, rect, paint)

        return output
    }

    private fun shortcutIdForPackage(packageName: String): String {
        return "shortcut_${packageName.replace('.', '_')}"
    }
}
