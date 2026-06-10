package com.wavez.themewallpaper.ui.premium.model

import com.src.billing_library.model.SalePurchase

sealed class ResponseState<out T> {
    data class Success<T>(val data: T) : ResponseState<T>()
    data class Error(val message: String? = null) : ResponseState<Nothing>()
    data object Loading : ResponseState<Nothing>()
}

sealed class PurchaseState {
    object Initial : PurchaseState()
    data class Success(val purchase: SalePurchase) : PurchaseState()
    data class Error(val code: Int) : PurchaseState()
}