package com.wavez.controllerwidget.ui.iconpack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.wavez.controllerwidget.R
import com.wavez.controllerwidget.core.base.BaseActivity
import com.wavez.controllerwidget.core.base.UiState
import com.wavez.controllerwidget.core.extensions.collectWithLifecycle
import com.wavez.controllerwidget.core.icons.IconShortcutInstaller
import com.wavez.controllerwidget.data.model.AppInfo
import com.wavez.controllerwidget.databinding.ActivitySetIconsBinding
import com.wavez.controllerwidget.ui.iconpack.adapter.SetIconAdapter
import com.wavez.controllerwidget.ui.iconpack.dialog.SelectAppBottomSheetDialog
import com.wavez.controllerwidget.ui.iconpack.model.IconPackItemUi
import com.wavez.controllerwidget.ui.iconpack.model.SetIconState
import com.wavez.controllerwidget.ui.iconpack.viewmodel.IconPackDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SetIconsActivity : BaseActivity<ActivitySetIconsBinding>(),
    SelectAppBottomSheetDialog.OnAppSelectedListener {

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
    private var latestItems: List<IconPackItemUi> = emptyList()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.tvTitle.text = intent.getStringExtra(EXTRA_PACK_NAME).orEmpty()
        binding.rcvIcons.adapter = adapter
    }

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
        val packId = intent.getStringExtra(EXTRA_PACK_ID) ?: return finish()
        viewModel.load(packId)
    }

    override fun initListener() {
        super.initListener()
        binding.btnBack.setOnClickListener { onHandleBackPressed() }
        binding.btnInstallAll.setOnClickListener {
            val installableItems = latestItems.filter { it.canInstall }
            if (installableItems.isEmpty()) {
                Toast.makeText(this, R.string.no_icons_to_install, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            installableItems.forEach(::installShortcut)
        }

        adapter.onActionClick = { item, state ->
            when (state) {
                SetIconState.UNLOCK -> unlockIcon(item)
                SetIconState.INSTALL -> installShortcut(item)
                SetIconState.CHOOSE -> showSelectApp(item)
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
                    val pack = detail.pack
                    binding.tvTitle.text = pack.name
                    latestItems = detail.items
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

    private fun unlockIcon(item: IconPackItemUi) {
        viewModel.unlockIcon(item)
    }

    private fun installShortcut(item: IconPackItemUi) {
        if (!item.canInstall) return
        val packageName = item.targetPackageName ?: return
        lifecycleScope.launch {
            iconShortcutInstaller.installShortcut(
                targetPackageName = packageName,
                customIconUrl = item.iconUrl,
                label = item.targetLabel,
            ).onSuccess {
                Toast.makeText(
                    this@SetIconsActivity, R.string.shortcut_install_requested, Toast.LENGTH_SHORT
                ).show()
            }.onFailure {
                Toast.makeText(
                    this@SetIconsActivity, R.string.shortcut_install_failed, Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onAppSelected(appInfo: AppInfo) {
        viewModel.applySelectedApp(appInfo)
    }
}
