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

package com.onetabb.template.android.presentation.dialog

import com.onetabb.template.android.ads.AdsScreen
import com.onetabb.template.android.ads.AdsTrigger
import com.onetabb.template.android.core.base.BaseDialogFragment
import com.onetabb.template.android.core.extensions.openInternetSettings
import com.onetabb.template.android.databinding.DialogNoInternetBinding

class NetworkDialog : BaseDialogFragment<DialogNoInternetBinding>() {
    override fun bindingView(): DialogNoInternetBinding = DialogNoInternetBinding.inflate(layoutInflater)

    override fun isCancelable(): Boolean = true

    companion object {
        const val TAG = "NetworkDialog_TAG"

        fun newInstance(): NetworkDialog = NetworkDialog()
    }

    override fun onStart() {
        super.onStart()
        isCancelable = false
    }

    override val adsScreenId = AdsScreen.NO_INTERNET_DIALOG_VIEW

    override fun initListener() {
        super.initListener()

        binding.btnTryAgain.setOnClickListener {
            triggerAds(AdsTrigger.NO_INTERNET_CLICK_TRY_AGAIN) {
                runWhenResumed { requireContext().openInternetSettings() }
            }
        }

        binding.btnClose.setOnClickListener {
            triggerAds(AdsTrigger.NO_INTERNET_CLICK_CLOSE) { runWhenResumed { dismiss() } }
        }
    }
}
