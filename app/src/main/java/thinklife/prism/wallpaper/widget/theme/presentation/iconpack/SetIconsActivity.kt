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

package thinklife.prism.wallpaper.widget.theme.presentation.iconpack

import thinklife.prism.wallpaper.widget.theme.ads.AdsTrigger
import thinklife.prism.wallpaper.widget.theme.ads.AdsScreen
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import thinklife.prism.wallpaper.widget.theme.R
import thinklife.prism.wallpaper.widget.theme.core.base.BaseActivity
import thinklife.prism.wallpaper.widget.theme.core.base.UiState
import thinklife.prism.wallpaper.widget.theme.core.extensions.collectWithLifecycle
import thinklife.prism.wallpaper.widget.theme.core.icons.IconShortcutInstaller
import thinklife.prism.wallpaper.widget.theme.data.model.AppInfo
import thinklife.prism.wallpaper.widget.theme.databinding.ActivitySetIconsBinding
import thinklife.prism.wallpaper.widget.theme.presentation.iconpack.adapter.SetIconAdapter
import thinklife.prism.wallpaper.widget.theme.presentation.iconpack.dialog.SelectAppBottomSheetDialog
import thinklife.prism.wallpaper.widget.theme.presentation.iconpack.model.IconPackItemUi
import thinklife.prism.wallpaper.widget.theme.presentation.iconpack.model.SetIconState
import thinklife.prism.wallpaper.widget.theme.presentation.iconpack.viewmodel.IconPackDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SetIconsActivity : BaseActivity<ActivitySetIconsBinding>(),
    SelectAppBottomSheetDialog.OnAppSelectedListener {

    override val adsScreenId = AdsScreen.SET_ICONS_VIEW


    companion object {
        private const val EXTRA_PACK_ID = "extra_pack_id"
        private const val EXTRA_PACK_NAME = "extra_pack_name"

        fun newIntent(context: Context, packId: String, packName: String): Intent =
            Intent(context, SetIconsActivity::class.java).apply {
                putExtra(EXTRA_PACK_ID, packId)
                putExtra(EXTRA_PACK_NAME, packName)
            }
    }

    override fun bindingView() = ActivitySetIconsBinding.inflate(layoutInflater)

    private val viewModel by viewModels<IconPackDetailViewModel>()

    @Inject
    lateinit var iconShortcutInstaller: IconShortcutInstaller

    private var adapter = SetIconAdapter()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.rcvIcons.adapter = adapter
    }

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
        val packId = intent.getStringExtra(EXTRA_PACK_ID) ?: return finish()
        viewModel.currentIconPackId = packId
        viewModel.load(packId)
    }

    override fun initListener() {
        super.initListener()
        binding.btnBack.setOnClickListener { onHandleBackPressed() }
        binding.btnInstallAll.setOnClickListener {
            triggerAds(AdsTrigger.SET_ICONS_CLICK_INSTALL_ALL) {
                runWhenResumed {
                    val mappedItems = viewModel.mappedIconsPendingInstall()
                    if (mappedItems.isEmpty()) {
                        Toast.makeText(this, R.string.no_icons_to_install, Toast.LENGTH_SHORT).show()
                        return@runWhenResumed
                    }
                    installAllShortcuts(mappedItems)
                }
            }
        }

        adapter.onActionClick = { item, state ->
            triggerAds(AdsTrigger.SET_ICONS_CLICK_ADAPTER_ACTION_CLICK) {
                runWhenResumed {
                    viewModel.itemIconPackItemUi = item
                    when (state) {
                        SetIconState.APPLIED -> installShortcut(item)
                        SetIconState.INSTALL -> installShortcut(item)
                        SetIconState.CHOOSE -> showSelectApp(item)
                    }
                }
            }
        }

        adapter.onEditClick = { item ->
            triggerAds(AdsTrigger.SET_ICONS_CLICK_ADAPTER_EDIT_CLICK) {
                runWhenResumed {
                    showSelectApp(item)
                }
            }
        }
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.uiState.collectWithLifecycle(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.viewLoading.isVisible = true
                }

                is UiState.Data -> {
                    binding.viewLoading.isVisible = false
                    binding.rcvIcons.isVisible = true
                    binding.btnInstallAll.isVisible = true
                    val detail = state.value
                    adapter.submitList(detail.items)
                }

                is UiState.Error -> {
                    binding.rcvIcons.isVisible = false
                    binding.btnInstallAll.isVisible = false
                    binding.viewLoading.isVisible = false
                }

                else -> {}
            }
        }
    }

    private fun showSelectApp(item: IconPackItemUi) {
        viewModel.setPendingIcon(item)
        SelectAppBottomSheetDialog.newInstance().show(
            supportFragmentManager, SelectAppBottomSheetDialog::class.java.simpleName
        )
    }

    private fun installShortcut(item: IconPackItemUi) {
        if (!item.canInstall) return
        triggerRewardAds(AdsTrigger.SET_ICONS_CLICK_INSTALL_SHORTCUT) { performInstallShortcut(item) }
    }

    private fun performInstallShortcut(item: IconPackItemUi) {
        if (!item.canInstall) return
        lifecycleScope.launch {
            val success = requestInstallShortcut(item)
            if (success) {
                viewModel.markIconApplied(item.id)
                val remainingIcons = viewModel.currentIconItems()
                    .filter { it.id != item.id }
                    .map { it.toIconItemModel() }
                startActivity(
                    ApplyIconsActivity.singleIconIntent(
                        this@SetIconsActivity,
                        remainingIcons,
                    )
                )
            } else {
                Toast.makeText(
                    this@SetIconsActivity, R.string.shortcut_install_failed, Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun installAllShortcuts(items: List<IconPackItemUi>) {
        triggerRewardAds(AdsTrigger.SET_ICONS_CLICK_INSTALL_ALL_SHORTCUTS) { performInstallAllShortcuts(items) }
    }

    private fun performInstallAllShortcuts(items: List<IconPackItemUi>) {
        lifecycleScope.launch {
            val appliedIds = items.mapNotNull { item ->
                if (requestInstallShortcut(item)) item.id else null
            }
            if (appliedIds.isNotEmpty()) {
                viewModel.markIconsApplied(appliedIds)
                startActivity(ApplyIconsActivity.allIconsIntent(this@SetIconsActivity,
                    viewModel.currentIconPackId.toString()
                ))
            } else {
                Toast.makeText(
                    this@SetIconsActivity, R.string.shortcut_install_failed, Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private suspend fun requestInstallShortcut(item: IconPackItemUi): Boolean {
        val packageName = item.targetPackageName ?: return false
        return iconShortcutInstaller.installShortcut(
            targetPackageName = packageName,
            customIconUrl = item.iconUrl,
            label = item.targetLabel,
        ).isSuccess
    }

    private fun IconPackItemUi.toIconItemModel() =
        thinklife.prism.wallpaper.widget.theme.data.model.icon.IconItemModel(
            packageName = targetPackageName.orEmpty(),
            appName = targetLabel,
            iconUrl = iconUrl,
        )

    override fun onAppSelected(appInfo: AppInfo) {
        viewModel.applySelectedApp(appInfo)
    }
    override fun onHandleBackPressed() {
        triggerBackAds(AdsTrigger.SET_ICONS_CLICK_BACK) { finish() }
    }

}
