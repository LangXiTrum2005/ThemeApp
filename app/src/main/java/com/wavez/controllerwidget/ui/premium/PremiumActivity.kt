package com.wavez.controllerwidget.ui.premium

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.wavez.controllerwidget.R
import com.wavez.controllerwidget.core.base.BaseActivity
import com.wavez.controllerwidget.core.extensions.gone
import com.wavez.controllerwidget.core.extensions.makeLinkOpenUrl
import com.wavez.controllerwidget.core.extensions.visible
import com.wavez.controllerwidget.databinding.ActivityPremiumBinding
import com.wavez.controllerwidget.ui.home.MainActivity
import com.wavez.controllerwidget.ui.premium.dialog.DialogFailedPremium
import com.wavez.controllerwidget.ui.premium.dialog.DialogSuccessPremium
import com.wavez.controllerwidget.ui.premium.model.PremiumPlan
import com.wavez.controllerwidget.ui.premium.model.PurchaseState
import com.wavez.controllerwidget.ui.premium.model.ResponseState
import com.wavez.controllerwidget.ui.utils.PRIVACY_POLICY_URL
import com.wavez.controllerwidget.ui.utils.TERMS_OF_SERVICE
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PremiumActivity : BaseActivity<ActivityPremiumBinding>(),
    DialogSuccessPremium.OnSuccessListener {

    override fun bindingView(): ActivityPremiumBinding {
        return ActivityPremiumBinding.inflate(layoutInflater)
    }


    companion object {
        const val ARG_IS_MAIN = "arg_is_main"

        fun newIntent(context: Context, isMain: Boolean = true): Intent {
            return Intent(context, PremiumActivity::class.java).apply {
                putExtra(ARG_IS_MAIN, isMain)
            }
        }
    }

    private val viewModel by viewModels<PremiumViewModel>()

    private var isMain: Boolean = false

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
        hideBottomNavigation()
        syncUiSelect(PremiumPlan.YEARLY)
        initView()
        isMain = intent?.getBooleanExtra(ARG_IS_MAIN, false) ?: false
    }


    override fun initListener() {
        super.initListener()
        binding.btnClose.setOnClickListener {
            onHandleBackPressed()
        }

        binding.btnBuyWeek.setOnClickListener {
            syncUiSelect(PremiumPlan.WEEKLY)
        }

        binding.btnBuyMonth.setOnClickListener {
            syncUiSelect(PremiumPlan.MONTHLY)
        }

        binding.btnBuyYear.setOnClickListener {
            syncUiSelect(PremiumPlan.YEARLY)
        }

        binding.btnGetPremium.setOnClickListener {
            viewModel.buyPremium(this)
        }

        binding.btnTryAgain.setOnClickListener {
            viewModel.tryAgain()
        }
    }

    private fun syncUiSelect(plan: PremiumPlan) {
        viewModel.currentProductSelected = plan.productId
        binding.btnGetPremium.text = getString(plan.buttonTextRes)

        binding.btnBuyWeek.isSelected = plan == PremiumPlan.WEEKLY
        binding.btnBuyMonth.isSelected = plan == PremiumPlan.MONTHLY
        binding.btnBuyYear.isSelected = plan == PremiumPlan.YEARLY
    }

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saleProductDetails.collect { state ->
                    when (state) {
                        is ResponseState.Success -> {
                            Log.d("log_premium", "initObserver: ${state.data}")
                            if (state.data.isEmpty()) {
                                binding.viewBuy.gone()
                                binding.viewError.visible()
                                binding.btnTryAgain.visible()
                                binding.btnGetPremium.gone()
                            } else {
                                binding.viewBuy.visible()
                                binding.btnGetPremium.visible()
                                binding.viewError.gone()
                                binding.btnTryAgain.gone()

                                state.data.find { it.productId == BillingConstant.MONTHLY_DRAW }
                                    ?.let {
                                        binding.tvPriceMonth.text = viewModel.getPrices(it)
                                    }
                                state.data.find { it.productId == BillingConstant.WEEKLY_DRAW }
                                    ?.let {
                                        binding.tvPriceWeek.text = viewModel.getPrices(it)
                                    }

                                state.data.find { it.productId == BillingConstant.YEAR_DRAW }?.let {
                                    binding.tvPriceYear.text = viewModel.getPrices(it)
                                }

                            }
                        }

                        is ResponseState.Error -> {
                            binding.viewBuy.gone()
                            binding.viewError.visible()
                        }

                        ResponseState.Loading -> {
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.purchaseState.collect { state ->
                    when (state) {
                        is PurchaseState.Error -> {
                            DialogFailedPremium.newInstance().show(
                                supportFragmentManager, DialogFailedPremium::class.java.simpleName
                            )

                        }

                        PurchaseState.Initial -> {

                        }

                        is PurchaseState.Success -> {
                            DialogSuccessPremium.newInstance().show(
                                supportFragmentManager, DialogSuccessPremium::class.java.simpleName
                            )
                        }
                    }
                }
            }
        }

    }

    private fun initView() {
        binding.tvTermsOfUse.makeLinkOpenUrl(
            getString(R.string.terms_of_use),
            TERMS_OF_SERVICE,
            R.color.text_low_em,
        )

        binding.tvPrivacy.makeLinkOpenUrl(
            getString(R.string.privacy_policy),
            PRIVACY_POLICY_URL,
            R.color.text_low_em,
        )
    }

    override fun onHandleBackPressed() {
        executeWithAds {
            if (!isMain) {
                startActivity(MainActivity.newIntent(this))
                finish()
            } else {
                super.onHandleBackPressed()
            }
        }
    }

    override fun onGotIt() {
        onHandleBackPressed()
    }

}