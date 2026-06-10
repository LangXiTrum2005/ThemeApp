package com.wavez.themewallpaper.ui.language.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.wavez.themewallpaper.core.extensions.gone
import com.wavez.themewallpaper.core.extensions.visible
import com.wavez.themewallpaper.data.model.LanguageModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingLanguageActivity : BaseLanguageActivity() {

    companion object {
        fun newIntent(context: Context): Intent =
            Intent(context, SettingLanguageActivity::class.java)
    }

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
        binding.nativeLanguage1.gone()
        binding.nativeLanguage2.gone()
        binding.btnSave.visible()
        binding.btnSave.isSelected = true
        binding.btnNextLanguage.gone()
        adapter.onLanguageSelected(viewModel.getConfig())
    }

    override fun onLanguagesChanged(languages: List<LanguageModel>) {
        super.onLanguagesChanged(languages)
        viewModel.setLanguageDefault(languages)
    }

    override fun onSaveLanguage() {
        setResult(RESULT_OK)
        finish()
    }
}
