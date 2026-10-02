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

package thinklife.prism.wallpaper.widget.theme.presentation.main

import thinklife.prism.wallpaper.widget.theme.ads.AdsTrigger
import thinklife.prism.wallpaper.widget.theme.ads.AdsScreen
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import thinklife.prism.wallpaper.widget.theme.R
import thinklife.prism.wallpaper.widget.theme.core.base.BaseActivity
import thinklife.prism.wallpaper.widget.theme.core.base.UiState
import thinklife.prism.wallpaper.widget.theme.core.extensions.collectWithLifecycle
import thinklife.prism.wallpaper.widget.theme.databinding.ActivityMainBinding
import thinklife.prism.wallpaper.widget.theme.presentation.main.adapter.MainPagerAdapter
import thinklife.prism.wallpaper.widget.theme.presentation.main.adapter.MainTab
import thinklife.prism.wallpaper.widget.theme.presentation.main.viewmodel.MainViewModel
import thinklife.prism.wallpaper.widget.theme.presentation.setting.dialog.RatePromptManager
import thinklife.prism.wallpaper.widget.theme.util.AppEvent
import thinklife.prism.wallpaper.widget.theme.util.FirebaseControlLogEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    override val adsScreenId = AdsScreen.MAIN_VIEW


    override fun bindingView(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    companion object {
        fun newIntent(context: android.content.Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }

    private val pagerAdapter by lazy {
        MainPagerAdapter(supportFragmentManager, lifecycle)
    }

    private var isSyncingPage = false

    private val viewModel by viewModels<MainViewModel>()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.bottomNav.itemIconTintList = null
        binding.bottomNav.setOnApplyWindowInsetsListener(null)
        binding.bottomNav.setPadding(0, 0, 0, 0)
    }

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
        if (savedInstanceState != null) com.adshub.monetization.AdsController.notifyAppRestoredFromBackground()
        logHomeViewEvent()
        setupViewPager()
        setupBottomNavigation()
        viewModel.getWallpapers()
        if (savedInstanceState == null) {
            RatePromptManager.showOnMainFromSplash(supportFragmentManager, sharedPref)
        }
    }

    private fun logHomeViewEvent() {
        val isReopen = sharedPref.hasLoggedHomeView
        val event = if (isReopen) {
            AppEvent.HOME_REOPEN_VIEW
        } else {
            AppEvent.HOME_1ST_VIEW
        }
        FirebaseControlLogEvent.logEvent(event)
        if (!isReopen) {
            sharedPref.hasLoggedHomeView = true
        }
    }

    private fun setupViewPager() {
        binding.vpMain.apply {
            adapter = pagerAdapter
            offscreenPageLimit = MainTab.entries.size
            isUserInputEnabled = false
        }

        binding.vpMain.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    if (isSyncingPage) return

                    val tab = MainTab.fromPosition(position)
                    if (tab == MainTab.MY_GALLERY) return

                    if (binding.bottomNav.selectedItemId != tab.menuId) {
                        isSyncingPage = true
                        binding.bottomNav.selectedItemId = tab.menuId
                        isSyncingPage = false
                    }
                }
            }
        )
    }

    override fun onResume() {
        super.onResume()
        updateBottomNavTitles()
    }

    private fun setupBottomNavigation() {
        updateBottomNavTitles()
        binding.bottomNav.setOnItemSelectedListener { item ->
            if (isSyncingPage) return@setOnItemSelectedListener true

            val tab = MainTab.fromMenuId(item.itemId)

            if (binding.vpMain.currentItem != tab.position) {
                triggerAds(tab.adsTrigger) {
                    runWhenResumed {
                        isSyncingPage = true
                        binding.vpMain.setCurrentItem(tab.position, false)
                        isSyncingPage = false
                    }
                }
            }

            true
        }
    }

    private fun updateBottomNavTitles() {
        binding.bottomNav.menu.findItem(R.id.nav_theme)?.setTitle(R.string.themes)
        binding.bottomNav.menu.findItem(R.id.nav_wallpaper)?.setTitle(R.string.wallpapers)
        binding.bottomNav.menu.findItem(R.id.nav_widgets)?.setTitle(R.string.widgets)
        binding.bottomNav.menu.findItem(R.id.nav_controller)?.setTitle(R.string.controller)
        binding.bottomNav.menu.findItem(R.id.nav_icons)?.setTitle(R.string.icons)
    }

    fun showMyGallery() {
        if (binding.vpMain.currentItem != MainTab.MY_GALLERY.position) {
            isSyncingPage = true
            binding.vpMain.setCurrentItem(MainTab.MY_GALLERY.position, false)
            isSyncingPage = false
        }
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.wallpapersState.collectWithLifecycle(this) { state ->
            when (state) {
                UiState.Idle -> Unit
                UiState.Loading -> { /* Show loading */
                }

                is UiState.Data -> { /* Bind to RecyclerView */
                }

                is UiState.Error -> { /* Show error */
                }
            }
        }
        viewModel.applyState.collectWithLifecycle(this) { state ->
            when (state) {
                UiState.Idle -> Unit
                UiState.Loading -> Toast.makeText(this, R.string.toast_processing_wallpaper, Toast.LENGTH_SHORT)
                    .show()

                is UiState.Data -> Toast.makeText(
                    this,
                    R.string.toast_wallpaper_applied,
                    Toast.LENGTH_SHORT
                ).show()

                is UiState.Error -> {
                    Log.d("log_loiii", "observeViewModel: ${state.message}")
                    Toast.makeText(this, getString(R.string.toast_error_prefix, state.message), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onHandleBackPressed() {
        triggerAds(AdsTrigger.MAIN_CLICK_BACK) {
            runWhenResumed {
                finish()
            }
        }
    }
}
