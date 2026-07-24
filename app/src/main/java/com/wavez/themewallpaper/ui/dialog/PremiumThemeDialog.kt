package com.wavez.themewallpaper.ui.dialog

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.ads.admob.reward.RewardAdShowState
import com.wavez.themewallpaper.ads.reward.RewardAdViewModel
import com.wavez.themewallpaper.core.base.BaseDialogFragment
import com.wavez.themewallpaper.core.extensions.gone
import com.wavez.themewallpaper.core.extensions.invisible
import com.wavez.themewallpaper.core.extensions.visible
import com.wavez.themewallpaper.data.repository.SharedPref
import com.wavez.themewallpaper.databinding.DialogPremiumThemeBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PremiumThemeDialog : BaseDialogFragment<DialogPremiumThemeBinding>() {
    override fun bindingView(): DialogPremiumThemeBinding {
        return DialogPremiumThemeBinding.inflate(layoutInflater)
    }

    override val hideNavigationBar: Boolean
        get() = true

    companion object {
        const val TAG = "PremiumThemeDialog"

        fun newInstance(): PremiumThemeDialog {
            return PremiumThemeDialog()
        }
    }

    private var listener: PremiumThemeListener? = null

    private val rewardAdViewModel by viewModels<RewardAdViewModel>()

    @Inject
    lateinit var sharedPref: SharedPref

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Prefer the hosting fragment (e.g. AddWidgetsBottomSheetDialog) so it wins over an
        // activity that also implements the listener; falls back to the activity when shown directly.
        listener = parentFragment as? PremiumThemeListener ?: context as? PremiumThemeListener
    }

    override fun initConfig(view: View, savedInstanceState: Bundle?) {
        super.initConfig(view, savedInstanceState)
        binding.btnWathAd.isVisible = sharedPref.shouldShowReward
    }

    override fun initListener() {
        super.initListener()
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnWathAd.setOnClickListener {
            rewardAdViewModel.loadAdAndShow(activity = requireActivity())
        }

        binding.btnTryAgain.setOnClickListener {
            rewardAdViewModel.loadAdAndShow(activity = requireActivity())
        }

        binding.btnGoPremium.setOnClickListener {
            listener?.onGoPremiumClick()
            dismiss()
        }
    }

    override fun initObserver() {
        super.initObserver()

        rewardAdViewModel.rewardAdState.observe(this) {
            when (it) {
                RewardAdShowState.Clicked -> {

                }

                is RewardAdShowState.Completed -> {
                    isCancelable = true
                    if (it.isRewarded) {
                        listener?.onWatchAdsComplete()
                    }
                    dismiss()
                }

                is RewardAdShowState.Failed -> {
                    isCancelable = true
                    Log.d("log_error", "initObservers: ${it.error}")
                    // Ad không hiển thị được thì vẫn cho dùng chức năng luôn.
                    listener?.onWatchAdsComplete()
                    dismiss()
                }

                RewardAdShowState.Impression -> {}
                RewardAdShowState.Initial -> {}
                RewardAdShowState.Loading -> {
                    isCancelable = false
                    binding.viewLoading.visible()
                    binding.viewFailed.gone()
                    binding.viewReward.invisible()
                }

                RewardAdShowState.RewardEarned -> {}
                RewardAdShowState.Showing -> {
                    isCancelable = true
                    binding.viewLoading.gone()
                    binding.viewFailed.gone()
                    binding.viewReward.visible()
                }
            }
        }

    }


    interface PremiumThemeListener {
        fun onWatchAdsComplete()
        fun onGoPremiumClick()
    }
}
