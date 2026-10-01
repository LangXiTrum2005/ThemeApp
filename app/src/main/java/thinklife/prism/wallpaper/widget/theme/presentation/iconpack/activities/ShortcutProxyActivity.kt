/*
 * Copyright (c) 2026 1Tabb. All rights reserved.
 * https://1tabb.com
 *
 * SPDX-License-Identifier: LicenseRef-1Tabb-Proprietary
 *
 * Proprietary and confidential. Contains trade secrets of 1Tabb.
 * Unauthorized copying, modification, distribution, reverse engineering,
 * decompilation or disassembly of this file, via any medium, is strictly
 * prohibited except with prior written permission of 1Tabb.
 */

package thinklife.prism.wallpaper.widget.theme.presentation.iconpack.activities

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import thinklife.prism.wallpaper.widget.theme.R

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
