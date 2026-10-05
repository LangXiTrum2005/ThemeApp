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

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.ontabb.rate.databinding.ActivityFeedbackBinding

/**
 * Màn góp ý cho user chấm 1-3 sao, mở từ [OneTabbRate].
 *
 * Kế thừa thẳng `AppCompatActivity` chứ không dùng base class của app: module phải chạy được
 * trong bất kỳ app nào nhúng nó.
 */
class FeedbackActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedbackBinding

    private val rating: Int by lazy {
        intent.getIntExtra(ARG_RATING, 1).coerceIn(1, 3)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowInsetsControllerCompat(window, window.decorView).hide(WindowInsetsCompat.Type.statusBars())
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvFeedbackHeader.applyRateFont(bold = true)
        binding.tvFeedbackTitle.applyRateFont(bold = true)
        binding.btnSendFeedback.applyRateFont(bold = true)
        binding.tvSelectedRating.applyRateFont(bold = false)
        binding.tvFeedbackDescription.applyRateFont(bold = false)
        binding.edtFeedback.applyRateFont(bold = false)

        binding.tvSelectedRating.text = resources.getQuantityString(
            R.plurals.ontabb_rate_selected_rating,
            rating,
            rating,
        )

        binding.btnBack.setOnClickListener { finish() }
        binding.btnSendFeedback.setOnClickListener { sendFeedback() }
    }

    private fun sendFeedback() {
        val feedback = binding.edtFeedback.text?.toString()?.trim().orEmpty()
        if (feedback.isBlank()) {
            Toast.makeText(this, R.string.ontabb_rate_please_enter_feedback, Toast.LENGTH_SHORT).show()
            return
        }

        val email = OneTabbRate.requireConfig().contactEmail
        // ACTION_SENDTO + scheme mailto: chỉ app email mới nhận, không lôi ra danh sách mọi app
        // biết share text.
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.ontabb_rate_email_subject))
            putExtra(
                Intent.EXTRA_TEXT,
                getString(R.string.ontabb_rate_email_body, rating, feedback),
            )
        }
        runCatching { startActivity(intent) }
            .onSuccess { finish() }
            .onFailure {
                Toast.makeText(this, R.string.ontabb_rate_no_email_app, Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        private const val ARG_RATING = "ontabb_rate_arg_rating"

        internal fun newIntent(context: Context, rating: Int): Intent = Intent(context, FeedbackActivity::class.java).putExtra(ARG_RATING, rating)
    }
}
