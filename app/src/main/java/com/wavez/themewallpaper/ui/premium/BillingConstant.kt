package com.wavez.themewallpaper.ui.premium

import android.util.Log

class BillingConstant {
    companion object {
        fun isSub(productId: String): Boolean {
            return productId == WEEKLY_DRAW || productId == MONTHLY_DRAW || productId == YEAR_DRAW
        }

        fun isConsumeAble(productId: String): Boolean {
            return when {
                isSub(productId) -> {
                    false
                }
                inAppConsumes.contains(productId) -> {
                    true
                }
                inAppNonConsumes.contains(productId) -> {
                    false
                }
                else -> {
                    Log.w("Log_purchase", "⚠️ Unknown product id: $productId — skipping consume check")
                    false
                }
            }
        }

        const val WEEKLY_DRAW = "weekly_pro"

        const val MONTHLY_DRAW = "monthly_pro"

        const val YEAR_DRAW = "yearly_trial"

        const val DISCOUNT_50 = 50

        val subs = listOf(WEEKLY_DRAW, MONTHLY_DRAW, YEAR_DRAW)

        val inAppNonConsumes = emptyList<String>()

        val inAppConsumes = emptyList<String>()

    }
}