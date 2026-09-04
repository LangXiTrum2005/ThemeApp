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

package com.ontabb.rate

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.ontabb.rate.databinding.DialogRateAppBinding

/**
 * Kiểu hiển thị 1: hộp thoại giữa màn hình ([RateStyle.DIALOG]).
 *
 * Fragment KHÔNG tự quyết định làm gì với số sao: nó chỉ bắn kết quả qua [RateResult.REQUEST_KEY]
 * rồi tự đóng. Toàn bộ luật (1-3 sao đi đâu, 4-5 sao đi đâu) nằm ở [OneTabbRate] — đặt ở đây thì
 * mỗi nơi gọi lại phải nhớ nối lại một lần. Phần vẽ sao dùng chung ở [RatePanel].
 */
class RateAppDialog : DialogFragment() {

    private var nullableBinding: DialogRateAppBinding? = null
    private val binding get() = nullableBinding!!

    private var panel: RatePanel? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = super.onCreateDialog(savedInstanceState).apply {
        // Nền trong suốt để bo góc của bg_rate_dialog lộ ra, và để Lottie đè lên mép trên
        // của thẻ không bị cắt bởi nền cửa sổ mặc định.
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        nullableBinding = DialogRateAppBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        panel = RatePanel(
            views = RatePanel.Views(
                animation = binding.rateAnimation,
                title = binding.tvRateTitle,
                description = binding.tvRateDescription,
                hint = binding.rateHint,
                hintText = binding.tvRateHint,
                rateButton = binding.btnRate,
                closeButton = binding.btnClose,
                stars = listOf(
                    binding.star1,
                    binding.star2,
                    binding.star3,
                    binding.star4,
                    binding.star5,
                ),
            ),
            onRateConfirmed = { rating ->
                parentFragmentManager.setFragmentResult(
                    RateResult.REQUEST_KEY,
                    bundleOf(RateResult.KEY_RATING to rating),
                )
                dismissAllowingStateLoss()
            },
            onClose = { dismissAllowingStateLoss() },
        ).also { it.bind(savedInstanceState?.getInt(STATE_RATING, 0) ?: 0) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_RATING, panel?.selectedRating ?: 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        panel = null
        nullableBinding = null
    }

    companion object {
        private const val STATE_RATING = "state_selected_rating"

        fun newInstance() = RateAppDialog()
    }
}
