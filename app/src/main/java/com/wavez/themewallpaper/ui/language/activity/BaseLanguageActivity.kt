package com.wavez.themewallpaper.ui.language.activity

import android.os.Bundle
import androidx.activity.viewModels
import com.wavez.themewallpaper.core.base.BaseActivity
import com.wavez.themewallpaper.core.base.UiState
import com.wavez.themewallpaper.data.model.LanguageModel
import com.wavez.themewallpaper.databinding.ActivityLanguageBinding
import com.wavez.themewallpaper.ui.language.LanguageAdapter
import com.wavez.themewallpaper.ui.language.LanguageViewModel

abstract class BaseLanguageActivity : BaseActivity<ActivityLanguageBinding>() {

    override fun bindingView(): ActivityLanguageBinding = ActivityLanguageBinding.inflate(
        layoutInflater
    )

    val viewModel by viewModels<LanguageViewModel>()

    protected lateinit var adapter: LanguageAdapter

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
        initAdapter()
        viewModel.getUiLanguages()
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.selectedUiLanguage.observe(this) {
            adapter.onLanguageSelected(it.code)
        }

        viewModel.uiLanguageState.observe(this) {
            when (it) {
                is UiState.Data -> {
                    onLanguagesChanged(it.value)
                }

                is UiState.Error -> {}
                UiState.Loading -> {}
                else -> {}
            }
        }
    }

    override fun initListener() {
        super.initListener()

        binding.btnSave.setOnClickListener {
            setLanguageSuccess()
        }

        binding.btnNextLanguage.setOnClickListener {
            setLanguageSuccess()
        }
    }

    private fun setLanguageSuccess() {
        viewModel.saveConfig()
        onSaveLanguage()
    }

    private fun initAdapter() {
        adapter = LanguageAdapter().apply {
            onSelected = {
                viewModel.onLanguageChanged(it)
                updateStateLanguageSelected(true)
            }
        }
        binding.languageRV.adapter = adapter
    }

    open fun updateStateLanguageSelected(isEnabled: Boolean) {}

    open fun onSaveLanguage() {}

    open fun onLanguagesChanged(languages: List<LanguageModel>) {
        adapter.set(languages)
    }
}
