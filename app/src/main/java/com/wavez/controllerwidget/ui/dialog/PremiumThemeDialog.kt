package com.wavez.controllerwidget.ui.dialog

import android.content.Context
import android.os.Bundle
import android.view.View
import com.wavez.controllerwidget.core.base.BaseDialogFragment
import com.wavez.controllerwidget.databinding.DialogPremiumThemeBinding

class PremiumThemeDialog : BaseDialogFragment<DialogPremiumThemeBinding>() {
    override fun bindingView(): DialogPremiumThemeBinding {
        return DialogPremiumThemeBinding.inflate(layoutInflater)
    }


    companion object {
        const val TAG = "PremiumThemeDialog"

        fun newInstance(): PremiumThemeDialog {
            return PremiumThemeDialog()
        }
    }

    private var listener : PremiumThemeListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? PremiumThemeListener ?: parentFragment as? PremiumThemeListener
    }

    override fun initConfig(view: View, savedInstanceState: Bundle?) {
        super.initConfig(view, savedInstanceState)
    }

    override fun initListener() {
        super.initListener()
        binding.btnClose.setOnClickListener {
            dismiss()
        }
        binding.btnWathAd.setOnClickListener {
            listener?.onWatchAdClick()
            dismiss()
        }
        binding.btnGoPremium.setOnClickListener {
            listener?.onGoPremiumClick()
            dismiss()
        }
    }

    interface PremiumThemeListener {
        fun onWatchAdClick()
        fun onGoPremiumClick()
    }
}
