package com.wavez.controllerwidget.ui.dialog

import android.content.Context
import com.wavez.controllerwidget.core.base.BaseDialogFragment
import com.wavez.controllerwidget.databinding.DialogDiscardFeedbackBinding

class DiscardFeedbackDialog : BaseDialogFragment<DialogDiscardFeedbackBinding>() {
    override fun bindingView(): DialogDiscardFeedbackBinding {
        return DialogDiscardFeedbackBinding.inflate(layoutInflater)
    }

    companion object {
        const val TAG = "NetworkDialog_TAG"

        fun newInstance(): DiscardFeedbackDialog {
            return DiscardFeedbackDialog()
        }
    }

    private var listener: DiscardFeedbackListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? DiscardFeedbackListener ?: parentFragment as? DiscardFeedbackListener
    }

    override fun onStart() {
        super.onStart()
        isCancelable = false
    }

    override fun initListener() {
        super.initListener()

        binding.btnDiscard.setOnClickListener {
            listener?.onDiscardConfirmed()
            dismiss()
        }

        binding.btnKeepEditing.setOnClickListener {
            dismiss()
        }
    }

    interface DiscardFeedbackListener {
        fun onDiscardConfirmed()
    }
}
