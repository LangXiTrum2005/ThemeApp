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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ontabb.rate.databinding.BottomSheetRateAppBinding

/**
 * Kiểu hiển thị 2: bottom sheet trượt từ đáy ([RateStyle.BOTTOM_SHEET]).
 *
 * Cùng nội dung và cùng luật với [RateAppDialog] — khác mỗi khung chứa, nên phần vẽ sao dùng
 * chung [RatePanel] và kết quả bắn qua đúng [RateResult.REQUEST_KEY].
 */
class RateAppBottomSheet : BottomSheetDialogFragment() {

    private var nullableBinding: BottomSheetRateAppBinding? = null
    private val binding get() = nullableBinding!!

    private var panel: RatePanel? = null

    /** Nền trong suốt: thẻ tự bo góc trên, và Lottie tràn ra ngoài mép thẻ không bị cắt. */
    override fun getTheme(): Int = R.style.Ontabb_Rate_BottomSheetDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
        // Mở thẳng ở trạng thái mở hết: thẻ thấp, để "half expanded" thì nội dung bị cắt ngang.
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            WindowInsetsControllerCompat(window, window.decorView).hide(WindowInsetsCompat.Type.statusBars())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        nullableBinding = BottomSheetRateAppBinding.inflate(inflater, container, false)
        return binding.root
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

        fun newInstance() = RateAppBottomSheet()
    }
}
