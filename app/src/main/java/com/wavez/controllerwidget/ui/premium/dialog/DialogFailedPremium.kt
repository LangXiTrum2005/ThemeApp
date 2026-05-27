package com.wavez.controllerwidget.ui.premium.dialog

import com.wavez.controllerwidget.core.base.BaseDialogFragment
import com.wavez.controllerwidget.databinding.DialogFailedPremiumBinding

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