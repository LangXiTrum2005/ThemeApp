package com.wavez.controllerwidget.ui.dialog

import android.os.Bundle
import android.view.View
import com.wavez.controllerwidget.core.base.BaseDialogFragment
import com.wavez.controllerwidget.databinding.DialogPremiumThemeBinding

class PremiumThemeDialog : BaseDialogFragment<DialogPremiumThemeBinding>() {
    override fun bindingView(): DialogPremiumThemeBinding {
        return DialogPremiumThemeBinding.inflate(layoutInflater)
    }

    companion object {
        fun newInstance(): PremiumThemeDialog {
            return PremiumThemeDialog()
        }
    }

    override fun initConfig(view: View, savedInstanceState: Bundle?) {
        super.initConfig(view, savedInstanceState)
    }



}