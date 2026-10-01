package com.wavez.themewallpaper.ui.dialog

import android.content.Context
import com.wavez.themewallpaper.core.base.BaseDialogFragment
import com.wavez.themewallpaper.databinding.DialogDiscardFeedbackBinding

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
