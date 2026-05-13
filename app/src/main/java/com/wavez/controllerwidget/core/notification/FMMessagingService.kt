package com.wavez.controllerwidget.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.wavez.controllerwidget.R
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

@AndroidEntryPoint
class FMMessagingService : FirebaseMessagingService() {

    companion object {
        private const val CHANEL_NAME = "M52 Gps Camera"
        private const val CHANGE_ID = "default_channel_m52"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // token
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        remoteMessage.notification?.let {
            val title = it.title ?: getString(R.string.app_name)
            val body = it.body ?: getString(R.string.app_name)
            val imageUrl = it.imageUrl?.toString()
            showNotification(title, body, imageUrl)
        }
    }

    private fun showNotification(title: String, body: String, imageUrl: String?) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANGE_ID,
            CHANEL_NAME,
            NotificationManager.IMPORTANCE_HIGH,
        )
        manager.createNotificationChannel(channel)

        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val builder =
            NotificationCompat.Builder(this, CHANGE_ID).setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title).setContentText(body).setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (!imageUrl.isNullOrEmpty()) {
            try {
                val bitmap = fetchBitmap(imageUrl)

                bitmap?.let {
                    builder.setStyle(
                        NotificationCompat.BigPictureStyle().bigPicture(it),
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun fetchBitmap(src: String?): Bitmap? {
        try {
            if (src != null) {
                val url = URL(src)
                val connection = url.openConnection() as HttpURLConnection
                connection.setDoInput(true)
                connection.connectTimeout = 50_000
                connection.readTimeout = 50_000
                connection.connect()
                val input = connection.getInputStream()
                return BitmapFactory.decodeStream(input)
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        return null
    }
}
