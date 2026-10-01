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

package thinklife.prism.wallpaper.widget.theme.ads

import android.content.Context
import android.os.Bundle
import com.adshub.monetization.engine.tracking.AnalyticsProvider
import com.google.firebase.analytics.FirebaseAnalytics

/** Cầu nối tracking của AdsHub SDK → Firebase Analytics (GA4). */
class AdsAnalyticsProvider(context: Context) : AnalyticsProvider {

    private val firebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    override fun logEvent(eventName: String, params: Bundle) {
        // Firebase chỉ nhận [a-zA-Z0-9_], tối đa 40 ký tự và không được bắt đầu bằng số.
        val safeName = eventName
            .replace(Regex("[^a-zA-Z0-9_]"), "_")
            .take(40)
            .ifEmpty { return }
        firebaseAnalytics.logEvent(safeName, params)
    }
}
