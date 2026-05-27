package com.wavez.controllerwidget.ui.language.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.wavez.controllerwidget.core.extensions.gone
import com.wavez.controllerwidget.core.extensions.visible
import com.wavez.controllerwidget.data.model.LanguageModel
import com.wavez.controllerwidget.ui.home.MainActivity
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
        startActivity(
            MainActivity.newIntent(this).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            },
        )
    }
}
