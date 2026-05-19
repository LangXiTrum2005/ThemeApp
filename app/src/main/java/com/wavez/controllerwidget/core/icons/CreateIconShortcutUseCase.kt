package com.wavez.controllerwidget.core.icons

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.wavez.controllerwidget.data.model.IconMappingItem
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateIconShortcutUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /**
     * Requests a pinned shortcut for the given [item].
     * Returns true if the pin request was sent, false if unsupported.
     * Throws [IllegalArgumentException] if [item] has no mapped package.
     */
    operator fun invoke(item: IconMappingItem): Boolean {
        val packageName = requireNotNull(item.mappedPackageName) {
            "Cannot create shortcut: no app mapped for icon '${item.iconId}'"
        }

        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) return false

        val bitmap = BitmapFactory.decodeFile(item.iconPath)
        requireNotNull(bitmap) { "Cannot decode icon bitmap: ${item.iconPath}" }

        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            ?: Intent(Intent.ACTION_MAIN).apply {
                setPackage(packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

        val shortcut = ShortcutInfoCompat.Builder(context, "icon_${item.iconId}_$packageName")
            .setShortLabel(item.mappedAppName ?: item.displayName)
            .setIcon(IconCompat.createWithBitmap(bitmap))
            .setIntent(launchIntent)
            .build()

        ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
        return true
    }
}
