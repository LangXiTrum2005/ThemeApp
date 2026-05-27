package com.wavez.controllerwidget.ui.premium.model

import com.wavez.controllerwidget.ui.premium.BillingConstant
import com.wavez.controllerwidget.R

enum class PremiumPlan(
    val productId: String,
    val buttonTextRes: Int
) {
    WEEKLY(
        BillingConstant.WEEKLY_DRAW,
        R.string.s_continue
    ),
    MONTHLY(
        BillingConstant.MONTHLY_DRAW,
        R.string.s_continue
    ),
    YEARLY(
        BillingConstant.YEAR_DRAW,
        R.string.start_free_trial
    )
}