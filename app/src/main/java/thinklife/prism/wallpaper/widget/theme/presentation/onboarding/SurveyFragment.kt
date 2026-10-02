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

package thinklife.prism.wallpaper.widget.theme.presentation.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import thinklife.prism.wallpaper.widget.theme.R
import thinklife.prism.wallpaper.widget.theme.ads.AdsBootstrap
import thinklife.prism.wallpaper.widget.theme.ads.AdsScreen
import thinklife.prism.wallpaper.widget.theme.ads.AdsSlot
import thinklife.prism.wallpaper.widget.theme.ads.AdsTrigger
import thinklife.prism.wallpaper.widget.theme.core.base.BaseFragment
import thinklife.prism.wallpaper.widget.theme.databinding.FragmentSurveyBinding
import thinklife.prism.wallpaper.widget.theme.presentation.onboarding.adapter.SurveyOptionAdapter
import thinklife.prism.wallpaper.widget.theme.presentation.onboarding.model.SurveyOption

/**
 * Trang đầu onboarding: khảo sát nhu cầu.
 *
 * TODO(project): thay danh sách option cho đúng app; muốn gửi lựa chọn về server thì thêm
 * ViewModel + repository, đừng gọi thẳng trong Fragment.
 */
class SurveyFragment : BaseFragment<FragmentSurveyBinding>() {

    companion object {
        fun newInstance() = SurveyFragment()
    }

    private val adapter = SurveyOptionAdapter()

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ) = FragmentSurveyBinding.inflate(inflater, container, false)

    private var isSelectedOption = false

    /** Trạng thái 1 của onb1. Trạng thái 2 (đã chọn option) = onb1_click_view, render tay. */
    override val adsScreenId
        get() = if (isSelectedOption) AdsScreen.ONB1_CLICK_VIEW else AdsScreen.ONB1_VIEW

    override fun initConfig(savedInstanceState: Bundle?) {
        val options = listOf(
            SurveyOption(1, getString(R.string.anime), R.drawable.obd_img_anime, R.drawable.ic_anime),
            SurveyOption(2, getString(R.string.aesthetics), R.drawable.obd_img_aesthetics, R.drawable.ic_aesthetics),
            SurveyOption(3, getString(R.string.cute), R.drawable.obd_img_cute, R.drawable.ic_cute),
            SurveyOption(4, getString(R.string.kpop), R.drawable.obd_img_kpop, R.drawable.kpop),
        )
        binding.rvOptions.adapter = adapter
        adapter.submitList(options)
        updateNextButtonState()
    }

    override fun initListener() {
        binding.btnNext.setOnClickListener {
            if (adapter.getSelectedIds().isEmpty()) {
                android.widget.Toast.makeText(requireContext(), R.string.select_option, android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            triggerAds(AdsTrigger.ONB1_CLICK_NEXT) {
                runWhenResumed { (activity as? OnboardingActivity)?.nextScreen() }
            }
        }
        adapter.onItemClick = { option, _ ->
            triggerAds(AdsTrigger.ONB1_CLICK_OPTION) {
                if (!isSelectedOption) {
                    isSelectedOption = true
                    showSelectedStateAd()
                }
                adapter.toggleSelection(option.id)
                updateNextButtonState()
            }
        }
    }

    /**
     * Trạng thái 2 của onb1 (user đã chọn option đầu tiên): đổi native onb1 -> onb1_click.
     * Container thứ 2 cố ý KHÔNG gắn tag trong XML nên phải notifyScreenEnter rồi render tay.
     */
    private fun showSelectedStateAd() {
        AdsBootstrap.releaseSlot(AdsSlot.ONB1_SLOT_END)
        binding.adsSlotOnb1End.removeAllViews()
        // Gỡ HẲN khỏi cây view, không chỉ gone(): AdsSlotScanner chỉ loại slot khỏi danh sách quét
        // của màn khi isAttachedToWindow == false, mà view GONE thì vẫn attached. Nếu onb1_slot_end
        // được khai refresh_policy (assets hoặc Remote Config), mỗi lần resume app SDK sẽ nạp native
        // mới vào khung đã ẩn — đốt fill, tụt show rate, user không nhìn thấy gì.
        (binding.adsSlotOnb1End.parent as? ViewGroup)?.removeView(binding.adsSlotOnb1End)
        AdsBootstrap.notifyScreenEnter(AdsScreen.ONB1_CLICK_VIEW)
        AdsBootstrap.renderSlotNow(
            screenId = AdsScreen.ONB1_CLICK_VIEW,
            slotId = AdsSlot.ONB1_CLICK_SLOT_END,
            container = binding.adsSlotOnb1ClickEnd,
        )
    }

    override fun onDestroyView() {
        AdsBootstrap.releaseSlot(AdsSlot.ONB1_CLICK_SLOT_END)
        super.onDestroyView()
    }

    private fun updateNextButtonState() {
        val hasSelection = adapter.getSelectedIds().isNotEmpty()
        binding.btnNext.isVisible = hasSelection
    }
}
