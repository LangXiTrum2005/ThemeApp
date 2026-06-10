package com.wavez.themewallpaper.ui.premium

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.src.billing_library.callback.OnPurchasesUpdatedListener
import com.src.billing_library.model.PurchaseResponseCode
import com.src.billing_library.model.SaleProductDetail
import com.src.billing_library.model.SalePurchase
import com.wavez.themewallpaper.data.repository.PremiumRepository
import com.wavez.themewallpaper.ui.premium.model.PurchaseState
import com.wavez.themewallpaper.ui.premium.model.ResponseState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val repository: PremiumRepository,
) : ViewModel(), OnPurchasesUpdatedListener {

    val isPremium: StateFlow<Boolean> get() = repository.isPremium

    val saleProductDetails = repository.saleProductDetails

    var currentProductSelected = BillingConstant.MONTHLY_DRAW

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Initial)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState

    private val _week: MutableStateFlow<ResponseState<SaleProductDetail?>> =
        MutableStateFlow(ResponseState.Loading)

    private val _month: MutableStateFlow<ResponseState<SaleProductDetail?>> =
        MutableStateFlow(ResponseState.Loading)

    private val _year: MutableStateFlow<ResponseState<SaleProductDetail?>> =
        MutableStateFlow(ResponseState.Loading)

    init {
        repository.addListener(this)
        collectSaleProductDetails()
    }

    fun unlockPremium(pre: Boolean) {
        repository.unlockPremium(pre)
    }

    private fun collectSaleProductDetails() {
        viewModelScope.launch {
            repository.saleProductDetails.collect { state ->
                when (state) {
                    is ResponseState.Success -> {
                        val saleProductDetails: List<SaleProductDetail> = state.data
                        Log.d("sdfdsfds", "collectSaleProductDetails: ${state.data}")
                        _week.value =
                            ResponseState.Success(saleProductDetails.find { it.productId == BillingConstant.WEEKLY_DRAW })
                        _month.value =
                            ResponseState.Success(saleProductDetails.find { it.productId == BillingConstant.MONTHLY_DRAW })
                        _year.value =
                            ResponseState.Success(saleProductDetails.find { it.productId == BillingConstant.YEAR_DRAW })
                    }

                    is ResponseState.Error -> {
                        _week.value = ResponseState.Error(state.message)
                        _month.value = ResponseState.Error(state.message)
                        _year.value = ResponseState.Error(state.message)
                    }

                    is ResponseState.Loading -> {
                        _week.value = ResponseState.Loading
                        _month.value = ResponseState.Loading
                        _year.value = ResponseState.Loading
                    }
                }
            }
        }
    }

    fun buyPremium(activity: Activity) {
        val productDetail = when (currentProductSelected) {
            BillingConstant.WEEKLY_DRAW -> _week.value
            BillingConstant.MONTHLY_DRAW -> _month.value
            BillingConstant.YEAR_DRAW -> _year.value
            else -> _week.value
        }
        if (productDetail is ResponseState.Success) {
            productDetail.data?.let {
                repository.buy(activity, it)
            }
        }

    }

    fun tryAgain() {
        repository.startConnection()
    }

    fun getPrices(saleProductDetail: SaleProductDetail): String {
        return if (BillingConstant.isSub(saleProductDetail.productId)) {
            val subscriptionOfferDetails = saleProductDetail.subscriptionOfferDetails
            subscriptionOfferDetails?.last()?.pricingPhases?.last()?.getReadAblePrice() ?: "unknown"
        } else {
            val oneTimePurchaseOfferDetails = saleProductDetail.oneTimePurchaseOfferDetails
            oneTimePurchaseOfferDetails?.getReadAblePrice() ?: "unknown"
        }
    }


    fun calculateYearSavePercent(): Int {
        val year = (_year.value as? ResponseState.Success)?.data
        val week = (_week.value as? ResponseState.Success)?.data

        val yearPrice = year?.subscriptionOfferDetails?.first()?.let {
            it.getPriceAmountMicros() / 1_000_000.0
        }
        val weekPrice = week?.subscriptionOfferDetails?.first()?.let {
            it.getPriceAmountMicros() / 1_000_000.0
        }
        if (yearPrice == 0.0 || weekPrice == 0.0 || yearPrice == null || weekPrice == null) return 0

        val yearCostFromWeek = weekPrice * 52
        val savePercent = ((yearCostFromWeek - yearPrice) / yearCostFromWeek) * 100

        return savePercent.toInt()
    }

    override fun onSucceeded(purchase: SalePurchase) {
        _purchaseState.value = PurchaseState.Success(purchase)
    }

    override fun onFailed(responseCode: Int) {
        if (responseCode == PurchaseResponseCode.USER_CANCELED) return
        _purchaseState.value = PurchaseState.Error(responseCode)
    }

    override fun onCleared() {
        repository.removeListener(this)
        super.onCleared()
    }

}