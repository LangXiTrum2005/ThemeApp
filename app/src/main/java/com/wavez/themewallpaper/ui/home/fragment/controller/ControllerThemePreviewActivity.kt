package com.wavez.themewallpaper.ui.home.fragment.controller

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.wavez.themewallpaper.R
import com.wavez.themewallpaper.core.base.BaseActivity
import com.wavez.themewallpaper.core.controllercenter.ControllerThemeAssets
import com.wavez.themewallpaper.core.controllercenter.ControllerThemeCatalog
import com.wavez.themewallpaper.core.controllercenter.service.ControlCenterService
import com.wavez.themewallpaper.databinding.ActivityControllerThemePreviewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ControllerThemePreviewActivity : BaseActivity<ActivityControllerThemePreviewBinding>() {
    override fun bindingView() = ActivityControllerThemePreviewBinding.inflate(layoutInflater)

    companion object {
        private const val EXTRA_THEME_ID = "controller_theme_id"

        fun newIntent(context: Context, themeId: String) =
            Intent(context, ControllerThemePreviewActivity::class.java).putExtra(EXTRA_THEME_ID, themeId)
    }

    private val themeId by lazy { intent.getStringExtra(EXTRA_THEME_ID).orEmpty() }
    private var downloading = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        val theme = ControllerThemeCatalog.theme(this, themeId) ?: run {
            finish()
            return
        }
        Glide.with(this).load(theme.thumbnail).centerCrop().into(binding.ivPreview)
        updateAction()
    }

    override fun initListener() {
        super.initListener()
        binding.btnBack.setOnClickListener { finish() }
        binding.btnAction.setOnClickListener {
            if (downloading) return@setOnClickListener
            val urls = ControllerThemeCatalog.assetUrls(this, themeId)
            val requiredUrl = urls.firstOrNull() ?: return@setOnClickListener
            if (ControllerThemeAssets.isDownloaded(this, themeId, urls)) {
                sharedPref.activeThemeId = themeId
                sendBroadcast(Intent(ControlCenterService.ACTION_RELOAD_THEME).setPackage(packageName))
                ControllerThemeCatalog.theme(this, themeId)?.let { theme ->
                    Toast.makeText(this, getString(R.string.controller_theme_applied, theme.name), Toast.LENGTH_SHORT).show()
                }
                finish()
            } else {
                download(requiredUrl)
            }
        }
    }

    private fun updateAction() {
        val urls = ControllerThemeCatalog.assetUrls(this, themeId)
        val downloaded = ControllerThemeAssets.isDownloaded(this, themeId, urls)
        binding.btnAction.text = when {
            downloaded && sharedPref.activeThemeId == themeId -> getString(R.string.controller_theme_active)
            downloaded -> getString(R.string.unlock)
            else -> getString(R.string.download)
        }
        binding.btnAction.isEnabled = !downloaded || sharedPref.activeThemeId != themeId
    }

    private fun download(requiredUrl: String) {
        downloading = true
        binding.btnAction.isEnabled = false
        binding.btnAction.text = getString(R.string.downloading_please_wait_a_moment)
        binding.progressDownload.progress = 0
        binding.progressDownload.visibility = View.VISIBLE
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val urls = ControllerThemeCatalog.assetUrls(applicationContext, themeId)
                    ControllerThemeAssets.download(applicationContext, themeId, urls, requiredUrl) { progress ->
                        runOnUiThread { binding.progressDownload.progress = progress }
                    }
                }
            }
            downloading = false
            binding.btnAction.isEnabled = true
            binding.progressDownload.visibility = View.GONE
            result.onFailure { error ->
                Log.w("ControllerThemePreview", "Theme download failed: $themeId", error)
                Toast.makeText(this@ControllerThemePreviewActivity, R.string.download_failed, Toast.LENGTH_SHORT).show()
            }
            updateAction()
        }
    }
}
