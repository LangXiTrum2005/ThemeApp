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

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.airbnb.lottie.LottieAnimationView

/**
 * Phần ruột của khối đánh giá: vẽ sao, đổi mặt cười / tiêu đề / mô tả theo số sao, bắt sự kiện.
 *
 * Tách khỏi Fragment vì có HAI kiểu hiển thị dùng chung đúng bộ logic này — hộp thoại giữa màn
 * ([RateAppDialog]) và bottom sheet ([RateAppBottomSheet]). Hai layout khác nhau nên ViewBinding
 * sinh ra hai lớp không họ hàng, do đó lớp này nhận thẳng các View qua [Views].
 */
internal class RatePanel(
    private val views: Views,
    private val onRateConfirmed: (rating: Int) -> Unit,
    private val onClose: () -> Unit,
) {

    /** Các View mà panel cần. Layout đặt id gì cũng được, miễn truyền đúng vai trò vào đây. */
    class Views(
        val animation: LottieAnimationView,
        val title: TextView,
        val description: TextView,
        val hint: View,
        val hintText: TextView,
        val rateButton: TextView,
        val closeButton: View,
        val stars: List<ImageView>,
    )

    var selectedRating = 0
        private set

    /** Gắn sự kiện và vẽ trạng thái đầu. [initialRating] > 0 khi khôi phục sau xoay màn. */
    fun bind(initialRating: Int) {
        views.title.applyRateFont(bold = true)
        views.rateButton.applyRateFont(bold = true)
        views.description.applyRateFont(bold = false)
        views.hintText.applyRateFont(bold = false)

        render(initialRating)

        views.closeButton.setOnClickListener { onClose() }

        views.stars.forEachIndexed { index, star ->
            star.setOnClickListener {
                render(index + 1)
                star.animate()
                    .scaleX(STAR_POP_SCALE)
                    .scaleY(STAR_POP_SCALE)
                    .setDuration(STAR_POP_MS)
                    .withEndAction {
                        star.animate().scaleX(1f).scaleY(1f).setDuration(STAR_POP_MS).start()
                    }
                    .start()
            }
        }

        views.rateButton.setOnClickListener {
            if (selectedRating == 0) return@setOnClickListener
            onRateConfirmed(selectedRating)
        }
    }

    private fun render(rating: Int) {
        selectedRating = rating

        views.stars.forEachIndexed { index, star ->
            star.setImageResource(
                if (index < rating) R.drawable.ic_rate_checked else R.drawable.ic_rate_unchecked,
            )
        }

        val state = when (rating) {
            5 -> RateState(
                R.raw.smiling_2,
                R.string.ontabb_rate_amazing_title,
                R.string.ontabb_rate_amazing_description,
            )

            4 -> RateState(
                R.raw.smiling_3,
                R.string.ontabb_rate_four_title,
                R.string.ontabb_rate_four_description,
            )

            in 1..3 -> RateState(
                R.raw.smiling_4,
                R.string.ontabb_rate_low_title,
                R.string.ontabb_rate_low_description,
            )

            else -> RateState(
                R.raw.smiling_1,
                R.string.ontabb_rate_initial_title,
                R.string.ontabb_rate_initial_description,
            )
        }

        views.animation.apply {
            setAnimation(state.animation)
            playAnimation()
        }
        views.title.setText(state.title)
        views.description.setText(state.description)
        // Mũi tên "chấm 5 sao đi" chỉ có nghĩa lúc user chưa chấm gì.
        views.hint.isVisible = rating == 0
        views.rateButton.alpha = if (rating == 0) DISABLED_ALPHA else 1f
    }

    private data class RateState(
        @param:RawRes val animation: Int,
        @param:StringRes val title: Int,
        @param:StringRes val description: Int,
    )

    private companion object {
        const val STAR_POP_SCALE = 1.18f
        const val STAR_POP_MS = 90L
        const val DISABLED_ALPHA = 0.65f
    }
}

/** Khoá kết quả dùng chung cho cả hai kiểu hiển thị — [OneTabbRate] lắng nghe đúng khoá này. */
internal object RateResult {
    const val REQUEST_KEY = "ontabb_rate_result"
    const val KEY_RATING = "ontabb_rate_rating"
}
