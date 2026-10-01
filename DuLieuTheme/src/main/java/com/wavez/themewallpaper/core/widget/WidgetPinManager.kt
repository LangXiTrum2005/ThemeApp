package com.wavez.themewallpaper.core.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import com.wavez.themewallpaper.ui.home.fragment.widget.adapter.WidgetSize

/**
 * Entry point for creating a home-screen widget: stores the chosen image as the pending value for
 * the size, then asks the launcher to pin the size-specific provider. The provider consumes the
 * pending value in onUpdate to render the image. [successCallback] is sent by the launcher once the
 * widget is actually pinned. Returns false when the launcher does not support pinning (older /
 * custom launchers), so the caller can inform the user.
 */
object WidgetPinManager {

    fun requestPin(
        context: Context,
        size: WidgetSize,
        imageUrl: String,
        successCallback: PendingIntent? = null,
    ): Boolean {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        if (!appWidgetManager.isRequestPinAppWidgetSupported) return false

        WidgetStorage(context).setPendingUrl(size.name, imageUrl)
        val provider = ComponentName(context, providerClassFor(size))
        return appWidgetManager.requestPinAppWidget(provider, null, successCallback)
    }

    private fun providerClassFor(size: WidgetSize): Class<out BaseImageWidgetProvider> =
        when (size) {
            WidgetSize.SMALL -> WidgetProviderSmall::class.java
            WidgetSize.MEDIUM -> WidgetProviderMedium::class.java
            WidgetSize.LARGE -> WidgetProviderLarge::class.java
        }
}
