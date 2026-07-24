package com.wavez.themewallpaper.core.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.AppWidgetTarget
import com.wavez.themewallpaper.R

/**
 * Shared behaviour for the fixed-size image widgets. Each subclass is a distinct provider so the
 * launcher can pin a specific size; the image to render is chosen in the app before pinning and
 * handed over through [WidgetStorage]'s pending value (keyed by [sizeKey]), then persisted per
 * appWidgetId so it survives updates and reboots.
 */
abstract class BaseImageWidgetProvider : AppWidgetProvider() {

    /** Identifies which pending image belongs to this provider's size. */
    protected abstract val sizeKey: String

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        val storage = WidgetStorage(context)
        appWidgetIds.forEach { appWidgetId ->
            val url = storage.getImageUrl(appWidgetId)
                ?: storage.consumePendingUrl(sizeKey)?.also { storage.setImageUrl(appWidgetId, it) }
            if (url != null) renderImage(context, appWidgetManager, appWidgetId, url)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val storage = WidgetStorage(context)
        appWidgetIds.forEach { storage.removeImageUrl(it) }
    }

    private fun renderImage(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        url: String,
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_image)
        appWidgetManager.updateAppWidget(appWidgetId, views)

        Glide.with(context.applicationContext)
            .asBitmap()
            .load(url)
            .into(AppWidgetTarget(context.applicationContext, R.id.ivWidget, views, appWidgetId))
    }
}
