package com.wavez.controllerwidget.core.icons

import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.bumptech.glide.Glide
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IconShortcutInstaller @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    suspend fun installShortcut(
        targetPackageName: String,
        customIconUrl: String,
        label: String,
    ): Result<Unit> = runCatching {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(targetPackageName)
            ?: error("No launcher activity for $targetPackageName")

        val bitmap = loadIconBitmap(customIconUrl)
        val shortcut = ShortcutInfoCompat.Builder(context, shortcutId(targetPackageName, customIconUrl))
            .setShortLabel(label)
            .setLongLabel(label)
            .setIcon(IconCompat.createWithBitmap(bitmap))
            .setIntent(launchIntent)
            .build()

        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            error("Pinned shortcuts are not supported on this launcher")
        }
        ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
    }

    private suspend fun loadIconBitmap(url: String): Bitmap = withContext(Dispatchers.IO) {
        Glide.with(context)
            .asBitmap()
            .load(url)
            .submit()
            .get()
    }

    private fun shortcutId(targetPackageName: String, customIconUrl: String): String {
        return "icon_${targetPackageName}_${customIconUrl.hashCode()}"
    }
}
