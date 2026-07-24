package com.wavez.themewallpaper.core.widget

import android.content.Context
import androidx.core.content.edit

/**
 * Persists which image URL each pinned app widget should render, keyed by its appWidgetId.
 * App widgets outlive the app process (survive reboot), so the mapping cannot live in memory.
 *
 * A pending URL (keyed by size) is stored right before asking the launcher to pin a widget: the
 * launcher does not reliably forward the request extras into the widget options, so [onUpdate][
 * BaseImageWidgetProvider.onUpdate] consumes this pending value to associate the freshly-pinned
 * widget with the image the user selected.
 */
class WidgetStorage(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE)

    fun getImageUrl(appWidgetId: Int): String? =
        prefs.getString(keyFor(appWidgetId), null)

    fun setImageUrl(appWidgetId: Int, url: String) =
        prefs.edit { putString(keyFor(appWidgetId), url) }

    fun removeImageUrl(appWidgetId: Int) =
        prefs.edit { remove(keyFor(appWidgetId)) }

    fun setPendingUrl(sizeKey: String, url: String) =
        prefs.edit { putString(pendingKeyFor(sizeKey), url) }

    /** Returns and clears the pending URL for the given size, if any. */
    fun consumePendingUrl(sizeKey: String): String? {
        val url = prefs.getString(pendingKeyFor(sizeKey), null) ?: return null
        prefs.edit { remove(pendingKeyFor(sizeKey)) }
        return url
    }

    private fun keyFor(appWidgetId: Int) = "$KEY_PREFIX$appWidgetId"

    private fun pendingKeyFor(sizeKey: String) = "$PENDING_PREFIX$sizeKey"

    companion object {
        private const val PREFERENCE_FILE = "widget_preferences"
        private const val KEY_PREFIX = "widget_image_url_"
        private const val PENDING_PREFIX = "widget_pending_url_"
    }
}
