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

package thinklife.prism.wallpaper.widget.theme.presentation.setting.feedback

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import thinklife.prism.wallpaper.widget.theme.R
import thinklife.prism.wallpaper.widget.theme.core.base.BaseActivity
import thinklife.prism.wallpaper.widget.theme.core.extensions.gone
import thinklife.prism.wallpaper.widget.theme.core.extensions.showKeyboard
import thinklife.prism.wallpaper.widget.theme.core.extensions.visible
import thinklife.prism.wallpaper.widget.theme.databinding.ActivityFeedbackBinding
import thinklife.prism.wallpaper.widget.theme.presentation.dialog.DiscardFeedbackDialog
import thinklife.prism.wallpaper.widget.theme.presentation.setting.feedback.FeedbackViewModel.Companion.MAX_IMAGE
import thinklife.prism.wallpaper.widget.theme.presentation.setting.feedback.adapter.FeedbackImageAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FeedbackActivity : BaseActivity<ActivityFeedbackBinding>(),
    DiscardFeedbackDialog.DiscardFeedbackListener {

    override fun bindingView(): ActivityFeedbackBinding {
        return ActivityFeedbackBinding.inflate(LayoutInflater.from(this))
    }


    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, FeedbackActivity::class.java)
        }
    }

    private var imagePickerLauncher: ActivityResultLauncher<String>? = null

    private lateinit var adapter: FeedbackImageAdapter

    private val feedbackViewModel: FeedbackViewModel by viewModels()

    private var hasSentFeedback: Boolean = false

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
        registerPickImageLauncher()
        initFeedbackImageAdapter()
        binding.edFeedback.showKeyboard()
    }

    private fun registerPickImageLauncher() {
        imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    feedbackViewModel.updateListImage(it)
                }
            }
    }

    private fun initFeedbackImageAdapter() {
        adapter = FeedbackImageAdapter()

        adapter.onRemoveItem = {
            feedbackViewModel.updateListImage(it, true)
        }
        binding.rcvImage.adapter = adapter
    }

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                feedbackViewModel.uris.collect {
                    adapter.set(it)
                    updateStateButton()
                    updateStateAddImage(it)
                }
            }
        }
    }


    override fun initListener() {
        super.initListener()

        binding.edFeedback.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                updateStateButton()
            }

        })


        binding.btnBack.setOnClickListener { onHandleBackPressed() }

        binding.btnSendFeedback.setOnClickListener {
            if (validateFeedbackForm()) {
                hasSentFeedback = true
                val strDes = binding.edFeedback.text.toString()
                feedbackViewModel.addDescription(strDes)
                feedbackViewModel.submitFeedback(this)
            } else {
                Toast.makeText(this, getString(R.string.feed_back_empty), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnAddImage.setOnClickListener {
            imagePickerLauncher?.launch("image/*")
        }

    }

    private fun updateStateAddImage(uri: List<Uri>) {
        if (uri.size > MAX_IMAGE) {
            binding.btnAddImage.gone()
        } else {
            binding.btnAddImage.visible()
        }
    }

    private fun validateFeedbackForm(): Boolean {
        return feedbackViewModel.hasImages() || binding.edFeedback.text.toString().isNotEmpty()
    }

    fun updateStateButton() {
        binding.btnSendFeedback.isSelected = validateFeedbackForm()
        binding.btnSendFeedback.isEnabled = validateFeedbackForm()
    }

    override fun onHandleBackPressed() {
        if (hasSentFeedback || (!validateFeedbackForm())) {
            super.onHandleBackPressed()
        } else {
            DiscardFeedbackDialog().show(
                supportFragmentManager,
                DiscardFeedbackDialog::class.java.simpleName
            )
        }
    }

    override fun onDiscardConfirmed() {
        super.onHandleBackPressed()
    }
}