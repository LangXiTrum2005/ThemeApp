package com.wavez.controllerwidget.ui.premium.dialog

import android.content.Context
import android.content.DialogInterface
import com.wavez.controllerwidget.core.base.BaseDialogFragment
import com.wavez.controllerwidget.databinding.DialogSuccessPremiumBinding

class DialogSuccessPremium : BaseDialogFragment<DialogSuccessPremiumBinding>() {
    override fun bindingView(): DialogSuccessPremiumBinding {
        return DialogSuccessPremiumBinding.inflate(layoutInflater)
    }

    companion object {
        fun newInstance() = DialogSuccessPremium()
    }

    private var listener: OnSuccessListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? OnSuccessListener ?: parentFragment as? OnSuccessListener
    }

    override fun initListener() {
        super.initListener()
        binding.btnGotIt.setOnClickListener {
            dismiss()
        }

    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener?.onGotIt()
    }

    interface OnSuccessListener {
        fun onGotIt()
    }
}