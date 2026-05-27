package com.wavez.controllerwidget.ui.dialog

import com.wavez.controllerwidget.core.base.BaseDialogFragment
import com.wavez.controllerwidget.core.extensions.openInternetSettings
import com.wavez.controllerwidget.databinding.DialogNetworkBinding

class NetworkDialog : BaseDialogFragment<DialogNetworkBinding>() {
    override fun bindingView(): DialogNetworkBinding {
        return DialogNetworkBinding.inflate(layoutInflater)
    }

    companion object {
        const val TAG = "NetworkDialog_TAG"

        fun newInstance(): NetworkDialog {
            return NetworkDialog()
        }
    }

    override fun onStart() {
        super.onStart()
        isCancelable = false
    }

    override fun initListener() {
        super.initListener()

        binding.btnGoSetting.setOnClickListener {
            requireContext().openInternetSettings()
        }
    }
}
