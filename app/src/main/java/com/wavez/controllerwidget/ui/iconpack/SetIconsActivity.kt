package com.wavez.controllerwidget.ui.iconpack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.wavez.controllerwidget.R
import com.wavez.controllerwidget.ads.AdsId
import com.wavez.controllerwidget.core.base.BaseActivity
import com.wavez.controllerwidget.core.base.UiState
import com.wavez.controllerwidget.core.extensions.collectWithLifecycle
import com.wavez.controllerwidget.core.icons.IconShortcutInstaller
import com.wavez.controllerwidget.data.model.AppInfo
import com.wavez.controllerwidget.databinding.ActivitySetIconsBinding
import com.wavez.controllerwidget.ui.dialog.PremiumThemeDialog
import com.wavez.controllerwidget.ui.iconpack.adapter.SetIconAdapter
import com.wavez.controllerwidget.ui.iconpack.dialog.SelectAppBottomSheetDialog
import com.wavez.controllerwidget.ui.iconpack.model.IconPackItemUi
import com.wavez.controllerwidget.ui.iconpack.model.SetIconState
import com.wavez.controllerwidget.ui.iconpack.viewmodel.IconPackDetailViewModel
import com.wavez.controllerwidget.ui.premium.PremiumActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SetIconsActivity : BaseActivity<ActivitySetIconsBinding>(),
    SelectAppBottomSheetDialog.OnAppSelectedListener, PremiumThemeDialog.PremiumThemeListener {

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
        loadBaner()
    }

    private fun loadBaner() {
        binding.bannerAds.registerBanner(
            this, adId = AdsId.getBannerInAppAds(),
            shouldLoad = sharedPref.shouldShowBannerInApp,
        )
    }

    override fun onVipChangedState(isPremium: Boolean) {
        super.onVipChangedState(isPremium)
        if (isPremium) {
            viewModel.refreshUnlockState()
        }
    }

    override fun initListener() {
        super.initListener()
        binding.btnBack.setOnClickListener { onHandleBackPressed() }
        binding.btnInstallAll.setOnClickListener {
            val mappedItems = viewModel.mappedIconsPendingInstall()
            if (mappedItems.isEmpty()) {
                Toast.makeText(this, R.string.no_icons_to_install, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (mappedItems.any { !it.isUnlocked }) {
                viewModel.setInstallingAllAfterUnlock(true)
                showPremiumDialog()
            } else {
                installAllShortcuts(mappedItems)
            }
        }

        adapter.onActionClick = { item, state ->
            viewModel.itemIconPackItemUi = item
            when (state) {
                SetIconState.APPLIED -> installShortcut(item)
                SetIconState.UNLOCK -> unlockIcon()
                SetIconState.INSTALL -> installShortcut(item)
                SetIconState.CHOOSE -> showSelectApp(item)
            }
        }

        adapter.onEditClick = { item ->
            showSelectApp(item)
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
                    updateIconsAction(detail.items)
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

    private fun updateIconsAction(items: List<IconPackItemUi>) {
        val pendingItems = items.filter { it.isInstallable && !it.isApplied }
        val hasLockedIcons = pendingItems.any { !it.isUnlocked }
        binding.btnInstallAll.setText(
            getString(
                if (hasLockedIcons) {
                    R.string.unlock_all_icons
                } else {
                    R.string.install_all_icons
                }
            )
        )
        binding.btnInstallAll.setIcon(
            if (hasLockedIcons) {
                R.drawable.ic_lock_1
            } else {
                0
            }
        )
    }

    private fun unlockIcon() {
        viewModel.setInstallingAllAfterUnlock(false)
        showPremiumDialog()
    }

    private fun showPremiumDialog() {
        if (supportFragmentManager.findFragmentByTag(PremiumThemeDialog.TAG) != null) return
        PremiumThemeDialog.newInstance().show(supportFragmentManager, PremiumThemeDialog.TAG)
    }

    private fun installShortcut(item: IconPackItemUi) {
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
        com.wavez.controllerwidget.data.model.icon.IconItemModel(
            packageName = targetPackageName.orEmpty(),
            appName = targetLabel,
            iconUrl = iconUrl,
        )

    override fun onAppSelected(appInfo: AppInfo) {
        viewModel.applySelectedApp(appInfo)
    }

    override fun onWatchAdsComplete() {
        if (viewModel.consumeInstallingAllAfterUnlock()) {
            val mappedItems = viewModel.mappedIconsPendingInstall()
            if (mappedItems.isNotEmpty()) {
                viewModel.unlockIcons(mappedItems.map { it.id })
            }
        } else {
            viewModel.unlockIcon()
        }
    }

    override fun onGoPremiumClick() {
        startActivity(PremiumActivity.newIntent(this))
    }

    override fun onHandleBackPressed() {
        executeWithAds {
            super.onHandleBackPressed()
        }
    }
}
