package com.src.billing_library

import android.annotation.SuppressLint
import com.src.billing_library.model.SaleProductDetail

@SuppressLint("DefaultLocale")
fun SaleProductDetail.getDailyPriceOnly(
    dividerFormat: String = "%.2f"
): String {

    val subDetails = subscriptionOfferDetails
    val oneTimeDetails = oneTimePurchaseOfferDetails

    val formattedPrice = when {
        !subDetails.isNullOrEmpty() -> {
            val offer = subDetails.last()
            val phase = offer.pricingPhases.lastOrNull()
            phase?.formattedPrice ?: return "unknown"
        }

        oneTimeDetails != null -> {
            oneTimeDetails.getReadAblePrice()
        }

        else -> return "unknown"
    }

    val regex = """(\D*)\s*([\d.,]+)\s*(\D*)""".toRegex()
    val matchResult = regex.matchEntire(formattedPrice) ?: return formattedPrice
    val (prefix, numeric, suffix) = matchResult.destructured

    val number =
        numeric.replace(".", "").replace(",", ".").toDoubleOrNull()
            ?: return formattedPrice

    val divider = when {
        productId.contains("year", true) -> 365.0
        productId.contains("month", true) -> 30.0
        productId.contains("week", true) -> 7.0
        else -> 1.0
    }

    val dailyPrice = number / divider
    return "${prefix.trim()}${String.format(dividerFormat, dailyPrice)}${suffix.trim()}"
}