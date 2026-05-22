package com.wavez.controllerwidget.ui.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.wavez.controllerwidget.core.base.BaseActivity
import com.wavez.controllerwidget.core.base.UiState
import com.wavez.controllerwidget.core.controllercenter.PermissionHelper
import com.wavez.controllerwidget.core.controllercenter.RequiredPermission
import com.wavez.controllerwidget.core.controllercenter.ThemeLoader
import com.wavez.controllerwidget.core.controllercenter.service.ControlCenterService
import com.wavez.controllerwidget.core.extensions.collectWithLifecycle
import com.wavez.controllerwidget.data.model.wallpaper.WallpaperModel
import com.wavez.controllerwidget.databinding.ActivityMainBinding
import com.wavez.controllerwidget.ui.home.adapter.MainPagerAdapter
import com.wavez.controllerwidget.ui.home.adapter.MainTab
import com.wavez.controllerwidget.ui.home.viewmodel.MainViewModel
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

    private var panelActive = false

    private val runtimePermissions = buildList {
        add(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { /* no-op */ }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.bottomNav.itemActiveIndicatorColor = null

        binding.bottomNav.setOnApplyWindowInsetsListener(null)
        binding.bottomNav.setPadding(0, 0, 0, 0)

    }

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
        setupViewPager()
        setupBottomNavigation()
        viewModel.getWallpapers()
        binding.togglePanelButton.setOnClickListener { togglePanel() }
        requestPermissions.launch(runtimePermissions.toTypedArray())
        binding.testGifWallpaperButton.setOnClickListener {
            viewModel.applyWallpaper(
                WallpaperModel(
                    id = "test_gif_001",
                    name = "Test GIF",
                    thumb = "",
                    premium = 0,
                    url = "https://cdn.leansoft-ai.com/ls35-emoji-keyboard/thumbnails/00001_cbn_Dont_Touch_NInja_Gif/previews/type_4/theme_preview.gif",
                    categoryId = "test",
                )
            )
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


    override fun onResume() {
        super.onResume()
        renderHomeState()
    }

    private fun renderHomeState() {
        val missingSpecial = PermissionHelper.getMissingPermissions(this)
        val accessibilityReady = missingSpecial.none { it == RequiredPermission.ACCESSIBILITY }
        val themeReady = ThemeLoader.isReady(this)
        val missingCount = ThemeLoader.missingAssets(this).size

        binding.accessibilityStatusText.apply {
            text = if (accessibilityReady) {
                setTextColor(Color.rgb(56, 142, 60))
                "Accessibility: ENABLED"
            } else {
                setTextColor(Color.rgb(176, 0, 32))
                "Accessibility: NOT ENABLED"
            }
        }

        binding.themeStatusText.apply {
            text = if (themeReady) {
                setTextColor(Color.rgb(56, 142, 60))
                "Theme: READY"
            } else {
                setTextColor(Color.rgb(230, 81, 0))
                "Theme: $missingCount/${ThemeLoader.REQUIRED_ASSETS.size} files missing - copy assets to filesDir/theme/theme_1/"
            }
        }

        binding.togglePanelButton.isEnabled = accessibilityReady
        binding.permissionButtonsContainer.removeAllViews()
        missingSpecial.forEach { permission ->
            binding.permissionButtonsContainer.addView(createPermissionButton(permission))
        }
    }

    private fun togglePanel() {
        panelActive = !panelActive
        sendBroadcast(
            Intent(ControlCenterService.Companion.ACTION_ACTIVE_CONTROL_PANEL).putExtra(
                ControlCenterService.Companion.EXTRA_SHOW, panelActive
            )
        )
        binding.togglePanelButton.text = if (panelActive) "Hide Panel" else "Show Panel"
    }

    private fun createPermissionButton(permission: RequiredPermission): Button {
        return Button(this).apply {
            text = when (permission) {
                RequiredPermission.ACCESSIBILITY -> "Enable Accessibility Service"
                RequiredPermission.WRITE_SETTINGS -> "Grant Write Settings"
                RequiredPermission.NOTIFICATION_ACCESS -> "Grant Notification Access"
            }
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(230, 81, 0))
            setOnClickListener {
                when (permission) {
                    RequiredPermission.ACCESSIBILITY -> PermissionHelper.openAccessibilitySettings(
                        this@MainActivity
                    )

                    RequiredPermission.WRITE_SETTINGS -> PermissionHelper.openWriteSettingsPage(this@MainActivity)
                    RequiredPermission.NOTIFICATION_ACCESS -> PermissionHelper.openNotificationAccessSettings(
                        this@MainActivity
                    )
                }
            }
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 4.dp
                bottomMargin = 4.dp
            }
        }
    }

    override fun onHandleBackPressed() {
        finish()
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}
