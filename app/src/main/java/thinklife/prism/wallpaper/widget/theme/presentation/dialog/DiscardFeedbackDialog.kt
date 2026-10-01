/*
 * Copyright (c) 2026 1Tabb. All rights reserved.
 * https://1tabb.com
 *
 * SPDX-License-Identifier: LicenseRef-1Tabb-Proprietary
 *
 * Proprietary and confidential. Contains trade secrets of 1Tabb.
 * Unauthorized copying, modification, distribution, reverse engineering,
 * decompilation or disassembly of this file, via any medium, is strictly
 * prohibited except with prior written permission of 1Tabb.
 */

package thinklife.prism.wallpaper.widget.theme.presentation.dialog

import android.content.Context
import thinklife.prism.wallpaper.widget.theme.core.base.BaseDialogFragment
import thinklife.prism.wallpaper.widget.theme.databinding.DialogDiscardFeedbackBinding

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
