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

package com.onetabb.template.android.presentation.language

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.onetabb.template.android.ads.AdsScreen
import com.onetabb.template.android.ads.AdsTrigger
import com.onetabb.template.android.core.base.BaseActivity
import com.onetabb.template.android.core.extensions.finishWithoutAnimation
import com.onetabb.template.android.core.extensions.setAppLocale
import com.onetabb.template.android.databinding.ActivitySettingLanguageBinding
import com.onetabb.template.android.presentation.language.adapter.LanguageAdapter
import dagger.hilt.android.AndroidEntryPoint

/**
 * Đổi ngôn ngữ đi từ Settings. Tách khỏi [LanguageActivity] (màn first-open) vì khác cả hành vi
 * lẫn tiền: màn này cho back, có nút Save, và KHÔNG chạy ads — slot vẫn khai theo giải phẫu
 * chuẩn nhưng để `enabled:false` trong `assets/ads_config.json`.
 *
 * Dùng chung [LanguageViewModel] và [LanguageAdapter] với màn first-open.
 */
@AndroidEntryPoint
class SettingLanguageActivity : BaseActivity<ActivitySettingLanguageBinding>() {

    override fun inflateBinding() = ActivitySettingLanguageBinding.inflate(layoutInflater)

    companion object {
        fun newIntent(context: Context) = Intent(context, SettingLanguageActivity::class.java)
    }

    private val viewModel: LanguageViewModel by viewModels()

    private val adapter by lazy { LanguageAdapter() }

    /** Không có ads nhưng VẪN bind để log đủ full màn + giữ currentScreenId đúng. */
    override val adsScreenId = AdsScreen.SETTING_LAN_VIEW

    override fun initConfig(savedInstanceState: Bundle?) {
        adapter.setSelectedLanguage(viewModel.pendingLanguage.code)
        binding.rvLanguages.layoutManager = LinearLayoutManager(this)
        binding.rvLanguages.adapter = adapter
        adapter.submitList(viewModel.languages)
    }

    override fun initListener() {
        binding.btnBack.setOnClickListener {
            triggerAds(AdsTrigger.SETTING_LAN_CLICK_BACK) { runWhenResumed { finish() } }
        }
        binding.btnSave.setOnClickListener {
            triggerAds(AdsTrigger.SETTING_LAN_CLICK_SAVE) { runWhenResumed { saveLanguage() } }
        }
        adapter.onItemClick = { language, _ ->
            triggerAds(AdsTrigger.SETTING_LAN_CLICK_ITEM) {
                viewModel.selectLanguage(language)
                adapter.setSelectedLanguage(language.code)
            }
        }
    }

    /**
     * Đổi locale làm Activity dựng lại — `finishWithoutAnimation` để user không thấy màn này
     * nháy lên một nhịp bằng ngôn ngữ mới rồi mới đóng.
     */
    private fun saveLanguage() {
        viewModel.saveLanguage()
        if (setAppLocale(viewModel.pendingLanguage.code)) finishWithoutAnimation() else finish()
    }
}
