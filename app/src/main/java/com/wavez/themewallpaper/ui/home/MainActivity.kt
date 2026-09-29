package com.wavez.themewallpaper.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.wavez.themewallpaper.core.base.BaseActivity
import com.wavez.themewallpaper.core.base.UiState
import com.wavez.themewallpaper.core.extensions.collectWithLifecycle
import com.wavez.themewallpaper.databinding.ActivityMainBinding
import com.wavez.themewallpaper.ui.home.adapter.MainPagerAdapter
import com.wavez.themewallpaper.ui.home.adapter.MainTab
import com.wavez.themewallpaper.ui.home.viewmodel.MainViewModel
import com.wavez.themewallpaper.ui.setting.dialog.RatePromptManager
import com.wavez.themewallpaper.ui.utils.AppEvent
import com.wavez.themewallpaper.ui.utils.FirebaseControlLogEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun bindingView(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    companion object {
        fun newIntent(activity: Activity): Intent {
            return Intent(activity, MainActivity::class.java)
        }
    }

    private val pagerAdapter by lazy {
        MainPagerAdapter(supportFragmentManager, lifecycle)
    }

    private var isSyncingPage = false

    private val viewModel by viewModels<MainViewModel>()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.bottomNav.itemActiveIndicatorColor = null
        binding.bottomNav.itemIconTintList = null
        binding.bottomNav.setOnApplyWindowInsetsListener(null)
        binding.bottomNav.setPadding(0, 0, 0, 0)
    }

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
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

                    if (binding.bottomNav.selectedItemId != tab.menuId) {
                        isSyncingPage = true
                        binding.bottomNav.selectedItemId = tab.menuId
                        isSyncingPage = false
                    }
                }
            }
        )
    }

    private fun setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            if (isSyncingPage) return@setOnItemSelectedListener true

            val tab = MainTab.fromMenuId(item.itemId)

            if (binding.vpMain.currentItem != tab.position) {
                isSyncingPage = true
                binding.vpMain.setCurrentItem(tab.position, false)
                isSyncingPage = false
            }

            true
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
                UiState.Loading -> Toast.makeText(this, "Đang xử lý wallpaper…", Toast.LENGTH_SHORT)
                    .show()

                is UiState.Data -> Toast.makeText(
                    this,
                    "Wallpaper đã được apply!",
                    Toast.LENGTH_SHORT
                ).show()

                is UiState.Error -> {
                    Log.d("log_loiii", "observeViewModel: ${state.message}")
                    Toast.makeText(this, "Lỗi: ${state.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onHandleBackPressed() {
        finish()
    }
}
