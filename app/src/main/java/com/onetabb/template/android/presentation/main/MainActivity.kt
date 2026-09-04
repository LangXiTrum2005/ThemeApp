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

package com.onetabb.template.android.presentation.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withResumed
import androidx.viewpager2.widget.ViewPager2
import com.onetabb.template.android.R
import com.onetabb.template.android.ads.AdsBootstrap
import com.onetabb.template.android.ads.AdsScreen
import com.onetabb.template.android.ads.AdsTrigger
import com.onetabb.template.android.core.base.BaseActivity
import com.onetabb.template.android.databinding.ActivityMainBinding
import com.ontabb.rate.OneTabbRate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Màn chính: ViewPager2 4 tab + bottom navigation. Banner đáy nằm ở layout này (`main_slot_bottom`),
 * mỗi tab khai screenId riêng.
 */
@AndroidEntryPoint
class MainActivity :
    BaseActivity<ActivityMainBinding>(),
    MainPageHost {

    companion object {
        private const val EXTRA_PAGE = "extra_page"

        fun newIntent(context: Context, page: Int = MainPage.HOME): Intent = Intent(context, MainActivity::class.java)
            .putExtra(EXTRA_PAGE, page)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }

    override fun inflateBinding() = ActivityMainBinding.inflate(layoutInflater)

    /** Container của các tab (banner đáy nav bar nằm ở layout này); mỗi tab khai screenId riêng. */
    override val adsScreenId = AdsScreen.MAIN_VIEW

    /** Đang đồng bộ trạng thái nav bar theo pager — không phải user bấm, không kéo cò ads. */
    private var isSyncingNavSelection = false

    private var isFirstResume = true

    /** Màn Home đã bị che hoàn toàn (user thật sự rời đi), không tính lần bị ads che. */
    private var wasHidden = false

    /** Cả app đã xuống background — lần resume kế tiếp là "mở lại app", không phải vào lại Home. */
    private var wasInBackground = false

    /**
     * ProcessLifecycleOwner chỉ bắn ON_STOP khi Activity CUỐI CÙNG của app dừng (có debounce
     * ~700ms nên đi sang màn con trong app không kích) — đây là cách phân biệt "user thoát app
     * rồi mở lại" với "user từ màn con quay về Home".
     */
    private val appBackgroundObserver = object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
            wasInBackground = true
        }
    }

    override fun initConfig(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            // Process bị OS kill rồi mở lại — không phải cold start thật.
            AdsBootstrap.notifyAppRestoredFromBackground()
            // Activity dựng lại (OS kill, đổi ngôn ngữ) cũng không phải một lần user vào Home.
            isFirstResume = false
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(appBackgroundObserver)
        binding.viewPager.adapter = MainPagerAdapter(this)
        binding.viewPager.offscreenPageLimit = 2
        // Chỉ đổi tab bằng nav bar — vuốt tay sẽ nhảy qua trigger ads của tab.
        binding.viewPager.isUserInputEnabled = false
        selectPage(intent.getIntExtra(EXTRA_PAGE, MainPage.HOME))
    }

    /**
     * Điểm bung hộp thoại đánh giá: mỗi lần user vào hoặc quay lại tab Home TỪ TRONG APP
     * (back từ màn con), KHÔNG tính lần mở lại app từ background.
     *
     * Cổng bằng [wasHidden] chứ không đếm thẳng mọi onResume: overlay tạm thời (hộp thoại xin
     * quyền, lớp phủ của SDK quảng cáo) cũng làm Activity resume lại mà user không hề rời Home —
     * đếm ở đó thì riêng lần mở app đầu tiên đã ngốn 2 lượt.
     *
     * Cổng thêm bằng [wasInBackground] vì onStop không phân biệt "đi sang màn con" với "thoát
     * app": tính cả lần mở lại app thì mọi lượt được phép bung đều bị resume đốt sạch — user cứ
     * mở lại app là gặp hộp thoại, lại đúng lúc resume ad đang bung.
     *
     * Chỉ đếm ở Activity, không đếm trong Fragment con: đổi tab trong ViewPager không đụng vòng
     * đời Activity nên một lần vào Home vẫn tính đúng một lần.
     */
    override fun onResume() {
        super.onResume()
        val isBackFromBackground = wasInBackground
        val isHomeVisit = isFirstResume || wasHidden
        wasInBackground = false
        isFirstResume = false
        wasHidden = false
        if (!isHomeVisit || isBackFromBackground) return
        OneTabbRate.showOnHome(this)
    }

    override fun onStop() {
        super.onStop()
        // Quảng cáo toàn màn hình cũng là một Activity: nó che Home rồi trả lại y nguyên. Đó
        // không phải user điều hướng đi đâu cả, nên không tính là một lần "vào màn Home".
        wasHidden = !AdsBootstrap.isFullScreenAdActive()
    }

    override fun onDestroy() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(appBackgroundObserver)
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        selectPage(intent.getIntExtra(EXTRA_PAGE, MainPage.HOME))
    }

    override fun initListener() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            if (isSyncingNavSelection) return@setOnItemSelectedListener true
            val page = when (item.itemId) {
                R.id.navTab2 -> MainPage.TAB2
                R.id.navTab3 -> MainPage.TAB3
                R.id.navSettings -> MainPage.SETTINGS
                else -> MainPage.HOME
            }
            triggerAds(navTriggerOf(page)) {
                runWhenResumed { binding.viewPager.setCurrentItem(page, false) }
            }
            // Trả false: trạng thái chọn của nav bar do onPageSelected đồng bộ lại sau khi ads đóng.
            false
        }

        binding.viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    isSyncingNavSelection = true
                    binding.bottomNavigation.selectedItemId = when (position) {
                        MainPage.TAB2 -> R.id.navTab2
                        MainPage.TAB3 -> R.id.navTab3
                        MainPage.SETTINGS -> R.id.navSettings
                        else -> R.id.navHome
                    }
                    isSyncingNavSelection = false
                }
            },
        )
    }

    private fun navTriggerOf(page: Int) = when (page) {
        MainPage.TAB2 -> AdsTrigger.MAIN_CLICK_NAV_TAB2
        MainPage.TAB3 -> AdsTrigger.MAIN_CLICK_NAV_TAB3
        MainPage.SETTINGS -> AdsTrigger.MAIN_CLICK_NAV_SETTINGS
        else -> AdsTrigger.MAIN_CLICK_NAV_HOME
    }

    override fun selectPage(page: Int) {
        if (binding.viewPager.currentItem == page) return
        lifecycleScope.launch {
            lifecycle.withResumed {
                binding.viewPager.setCurrentItem(page, false)
            }
        }
    }

    /**
     * Back của máy: từ tab khác thì về Home, ở Home thì đây là ý định THOÁT APP — chặng cuối
     * này là chỗ bung hộp thoại đánh giá.
     *
     * `showOnBackPressed` trả true nghĩa là hộp thoại đã bung, phải hoãn `finish()` lại, nếu
     * không user thấy hộp thoại loé lên rồi app đóng luôn. Bấm back lần nữa sau khi đóng hộp
     * thoại thì lượt đó không còn nằm trong danh sách nên app thoát bình thường.
     */
    override fun onHandleBackPressed() {
        if (binding.viewPager.currentItem != MainPage.HOME) {
            selectPage(MainPage.HOME)
            return
        }
        if (OneTabbRate.showOnBackPressed(this)) return
        finish()
    }
}
