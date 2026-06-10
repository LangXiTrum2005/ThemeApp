package com.wavez.themewallpaper.data.repository

import android.app.Activity
import com.src.billing_library.BillingLibrary
import com.src.billing_library.callback.ConnectionListener
import com.src.billing_library.callback.OnPurchasesUpdatedListener
import com.src.billing_library.model.SaleProductDetail
import com.src.billing_library.model.SalePurchase
import com.wavez.themewallpaper.ui.premium.BillingConstant
import com.wavez.themewallpaper.ui.premium.model.ResponseState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PremiumRepository(
    private val billingManager: BillingLibrary,
    private val sharedPref: SharedPref
) : OnPurchasesUpdatedListener {

    init {
        addListener(this)
    }

    fun startConnection() {
        billingManager.startConnection(object : ConnectionListener {
            override fun onConnectionSuccess() {
                queryPurchases()
                querySaleProductDetails()
            }

            override fun onDisconnected() {
                _saleProductDetails.value = ResponseState.Error("Disconnected")
            }

            override fun onConnectionFailure() {
                _saleProductDetails.value = ResponseState.Error("Connection Failure")
            }
        })
    }


    private val _isPremium: MutableStateFlow<Boolean> = MutableStateFlow(sharedPref.isVip)
    val isPremium: StateFlow<Boolean> = _isPremium

    private val _saleProductDetails =
        MutableStateFlow<ResponseState<List<SaleProductDetail>>>(ResponseState.Loading)
    val saleProductDetails: StateFlow<ResponseState<List<SaleProductDetail>>> = _saleProductDetails

    private val _purchases =
        MutableStateFlow<ResponseState<List<SalePurchase>>>(ResponseState.Loading)

    fun addListener(ls: OnPurchasesUpdatedListener) {
        billingManager.addListener(ls)
    }

    fun removeListener(ls: OnPurchasesUpdatedListener) {
        billingManager.removeListener(ls)
    }

    private fun querySaleProductDetails() {
        _saleProductDetails.value = ResponseState.Loading
        billingManager.queryProductDetails(
            subs = BillingConstant.subs,
            inApp = listOf(),
            onCompleted = {
                _saleProductDetails.value = ResponseState.Success(it)
            }
        )
    }

    private fun queryPurchases() {
        _purchases.value = ResponseState.Loading
        billingManager.queryPurchases(onCompleted = {
            unlockPremium(it.isNotEmpty())
            _purchases.value = ResponseState.Success(it)
        })
    }

    fun buy(activity: Activity, product: SaleProductDetail) {
        billingManager.purchase(activity, product)
    }

    override fun onSucceeded(purchase: SalePurchase) {
        unlockPremium(true)
        billingManager.onTransactionCompleted(
            purchase,
            BillingConstant.isConsumeAble(purchase.productId)
        )
    }

    override fun onFailed(responseCode: Int) {
    }

    fun unlockPremium(pre: Boolean) {
        sharedPref.isVip = pre
        _isPremium.value = pre
    }


}