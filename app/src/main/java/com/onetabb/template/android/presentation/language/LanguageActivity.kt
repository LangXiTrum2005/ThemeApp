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
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.onetabb.template.android.ads.AdsBootstrap
import com.onetabb.template.android.ads.AdsScreen
import com.onetabb.template.android.ads.AdsSlot
import com.onetabb.template.android.ads.AdsTrigger
import com.onetabb.template.android.core.base.BaseActivity
import com.onetabb.template.android.core.extensions.finishWithoutAnimation
import com.onetabb.template.android.core.extensions.setAppLocale
import com.onetabb.template.android.databinding.ActivityLanguageBinding
import com.onetabb.template.android.presentation.language.adapter.LanguageAdapter
import com.onetabb.template.android.presentation.onboarding.OnboardingActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Màn Language của luồng first open (LFO) — có ads.
 *
 * TODO(project): đổi ngôn ngữ từ Settings phải đi màn RIÊNG (screenId `setting_lan_view`), không
 * dùng lại màn này, để funnel LFO và capping ads không bị lẫn.
 */
@AndroidEntryPoint
class LanguageActivity : BaseActivity<ActivityLanguageBinding>() {

    override fun inflateBinding() = ActivityLanguageBinding.inflate(layoutInflater)

    companion object {
        fun newIntent(context: Context) = Intent(context, LanguageActivity::class.java)
    }

    private val viewModel: LanguageViewModel by viewModels()

    private var isFistSelectedLanguage = false

    private val adapter by lazy { LanguageAdapter() }

    /** Trạng thái 1 của màn Language (chưa chọn). Trạng thái 2 = lan2_view, render tay. */
    override val adsScreenId = AdsScreen.LAN_VIEW

    override fun initConfig(savedInstanceState: Bundle?) {
        binding.rvLanguages.layoutManager = LinearLayoutManager(this)
        binding.rvLanguages.adapter = adapter
        adapter.submitList(viewModel.languages)
    }

    override fun initListener() {
        binding.btnNext.setOnClickListener {
            triggerAds(AdsTrigger.LAN_CLICK_DONE) { runWhenResumed { saveLanguage() } }
        }
        adapter.onItemClick = { language, _ ->
            triggerAds(AdsTrigger.LAN_CLICK_ITEM) {
                viewModel.selectLanguage(language)
                adapter.setSelectedLanguage(language.code)
                if (!isFistSelectedLanguage) {
                    isFistSelectedLanguage = true
                    binding.btnNext.visibility = View.VISIBLE
                    showLanguageSecondStateAd()
                }
            }
        }
    }

    /**
     * Trạng thái 2 của màn Language (đã chọn ngôn ngữ): đổi native LFO1 -> LFO2. Container thứ 2
     * cố ý KHÔNG gắn tag trong XML nên phải notifyScreenEnter trước rồi render tay.
     */
    private fun showLanguageSecondStateAd() {
        AdsBootstrap.releaseSlot(AdsSlot.LAN_SLOT_END)
        binding.adsSlotLanEnd.removeAllViews()
        // Gỡ HẲN khỏi cây view, không chỉ gone(): AdsSlotScanner chỉ loại slot khỏi danh sách quét
        // của màn khi isAttachedToWindow == false, mà view GONE thì vẫn attached. Nếu lan_slot_end
        // được khai refresh_policy (assets hoặc Remote Config), mỗi lần resume app SDK sẽ nạp native
        // mới vào khung đã ẩn — đốt fill, tụt show rate, user không nhìn thấy gì.
        (binding.adsSlotLanEnd.parent as? ViewGroup)?.removeView(binding.adsSlotLanEnd)
        AdsBootstrap.notifyScreenEnter(AdsScreen.LAN2_VIEW)
        AdsBootstrap.renderSlotNow(
            screenId = AdsScreen.LAN2_VIEW,
            slotId = AdsSlot.LAN2_SLOT_END,
            container = binding.adsSlotLan2End,
        )
    }

    override fun onDestroy() {
        AdsBootstrap.releaseSlot(AdsSlot.LAN2_SLOT_END)
        super.onDestroy()
    }

    /** LFO: chặn back, user phải chọn ngôn ngữ mới đi tiếp. */
    override fun onHandleBackPressed() = Unit

    private fun saveLanguage() {
        viewModel.saveLanguage()
        val localeChanged = setAppLocale(viewModel.pendingLanguage.code)
        if (viewModel.isNeedGotoWalkthrough) {
            startActivity(
                OnboardingActivity.newIntent(this).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION),
            )
        }
        if (localeChanged) finishWithoutAnimation() else finish()
    }
}
