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

package thinklife.prism.wallpaper.widget.theme.presentation.setting.dialog

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import thinklife.prism.wallpaper.widget.theme.R
import thinklife.prism.wallpaper.widget.theme.core.base.BaseDialogFragment
import thinklife.prism.wallpaper.widget.theme.core.extensions.rateApp
import thinklife.prism.wallpaper.widget.theme.data.local.SharedPref
import thinklife.prism.wallpaper.widget.theme.databinding.DialogRateBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RateDialogFragment : BaseDialogFragment<DialogRateBinding>() {

    override fun bindingView(): DialogRateBinding = DialogRateBinding.inflate(layoutInflater)

    override val hideNavigationBar: Boolean get() = true

    @Inject
    lateinit var sharedPref: SharedPref

    private val preSelectedStars by lazy {
        arguments?.getInt(ARG_PRE_SELECTED_STARS, 0) ?: 0
    }

    private val handler = Handler(Looper.getMainLooper())
    private var isAnimating = false

    override fun initConfig(view: View, savedInstanceState: Bundle?) {
        super.initConfig(view, savedInstanceState)
        updateContent(preSelectedStars)
        if (preSelectedStars > 0) {
            binding.ratingBar.rating = preSelectedStars.toFloat()
        } else {
            startAnimation()
        }
    }

    override fun initListener() {
        super.initListener()
        binding.ratingBar.setOnRatingChangeListener { _, rating, fromUser ->
            updateContent(rating.toInt())
            if (fromUser) {
                stopAnimation()
            }
        }
        binding.btnClose.setOnClickListener {
            stopAnimation()
            dismiss()
        }
        binding.btnRate.setOnClickListener {
            val stars = binding.ratingBar.rating.toInt()
            if (stars <= 0) {
                return@setOnClickListener
            }
            stopAnimation()
            sharedPref.ratedStars = stars
            sharedPref.isRate = true
            if (stars >= 4) requireContext().rateApp()
            dismiss()
        }
    }

    private fun updateContent(stars: Int) {
        when (stars) {
            1 -> {
                binding.ivAppIcon.setImageResource(R.drawable.ic_rate_1)
                binding.tvTitle.setText(R.string.rate_dialog_title_1)
                binding.tvMessage.setText(R.string.rate_dialog_message_1)
            }

            2 -> {
                binding.ivAppIcon.setImageResource(R.drawable.ic_rate_2)
                binding.tvTitle.setText(R.string.rate_dialog_title_1)
                binding.tvMessage.setText(R.string.rate_dialog_message_1)
            }

            3 -> {
                binding.ivAppIcon.setImageResource(R.drawable.ic_rate_3)
                binding.tvTitle.setText(R.string.rate_dialog_title_3)
                binding.tvMessage.setText(R.string.rate_dialog_message_3)
            }

            4 -> {
                binding.ivAppIcon.setImageResource(R.drawable.ic_rate_4)
                binding.tvTitle.setText(R.string.rate_dialog_title_4)
                binding.tvMessage.setText(R.string.rate_dialog_message_4)
            }

            5 -> {
                binding.ivAppIcon.setImageResource(R.drawable.ic_rate_5)
                binding.tvTitle.setText(R.string.rate_dialog_title_5)
                binding.tvMessage.setText(R.string.rate_dialog_message_5)
            }

            else -> {
                binding.ivAppIcon.setImageResource(R.drawable.ic_rate_app)
                binding.tvTitle.text =
                    getString(R.string.rate_dialog_title)
                binding.tvMessage.setText(R.string.rate_dialog_message)
            }
        }
    }

    private fun startAnimation() {
        isAnimating = true
        val stepDelay = 300L
        for (i in 1..5) {
            handler.postDelayed({
                if (isAnimating) binding.ratingBar.rating = i.toFloat()
            }, i * stepDelay)
        }
        handler.postDelayed({
            if (isAnimating) {
                binding.ratingBar.rating = 5f
                isAnimating = false
            }
        }, 5 * stepDelay + 500L)
    }

    private fun stopAnimation() {
        handler.removeCallbacksAndMessages(null)
        isAnimating = false
    }

    override fun onDestroyView() {
        stopAnimation()
        super.onDestroyView()
    }

    companion object {
        const val TAG = "RateDialogFragment"
        private const val ARG_PRE_SELECTED_STARS = "arg_pre_selected_stars"

        fun newInstance(preSelectedStars: Int = 0) = RateDialogFragment().apply {
            arguments = Bundle().apply { putInt(ARG_PRE_SELECTED_STARS, preSelectedStars) }
        }
    }
}
