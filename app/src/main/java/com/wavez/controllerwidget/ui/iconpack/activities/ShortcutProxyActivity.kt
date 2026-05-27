package com.wavez.controllerwidget.ui.iconpack.activities


import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.wavez.controllerwidget.R

class ShortcutProxyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val targetPackage = intent.getStringExtra(EXTRA_PACKAGE_NAME)
            ?: intent.data?.lastPathSegment
        val mainActivity = intent.getStringExtra(EXTRA_MAIN_ACTIVITY)

        if (!targetPackage.isNullOrBlank() && !mainActivity.isNullOrBlank()) {
            try {
                val launchIntent = Intent().apply {
                    component = ComponentName(targetPackage, mainActivity)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (packageManager.resolveActivity(launchIntent, 0) != null) {
                    startActivity(launchIntent)
                } else {
                    Toast.makeText(this,
                        getString(R.string.cannot_open_target_app), Toast.LENGTH_SHORT).show()
                }
            } catch (_: Exception) {
                Toast.makeText(this, R.string.cannot_open_target_app, Toast.LENGTH_SHORT).show()
            }
        }

        finish()
    }

    companion object {
        const val EXTRA_PACKAGE_NAME = "packageName"
        const val EXTRA_MAIN_ACTIVITY = "mainActivity"
    }
}
