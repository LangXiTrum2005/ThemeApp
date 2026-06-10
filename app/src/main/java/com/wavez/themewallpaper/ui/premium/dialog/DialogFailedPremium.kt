package com.wavez.themewallpaper.ui.premium.dialog

import com.wavez.themewallpaper.core.base.BaseDialogFragment
import com.wavez.themewallpaper.databinding.DialogFailedPremiumBinding

class DialogFailedPremium : BaseDialogFragment<DialogFailedPremiumBinding>() {
    override fun bindingView(): DialogFailedPremiumBinding {
        return DialogFailedPremiumBinding.inflate(layoutInflater)

    }

    companion object {
        fun newInstance() = DialogFailedPremium()
    }

    override fun initListener() {
        super.initListener()
        binding.btnTryAgain.setOnClickListener {
            dismiss()
        }
    }


}