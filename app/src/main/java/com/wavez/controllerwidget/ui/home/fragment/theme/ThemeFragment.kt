package com.wavez.controllerwidget.ui.home.fragment.theme

import android.os.Bundle
import android.view.View
import com.example.m65_gamemodefps.core.base.BaseFragment
import com.wavez.controllerwidget.R
import com.wavez.controllerwidget.databinding.FragmentThemeBinding
import com.wavez.controllerwidget.ui.setting.SettingActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ThemeFragment : BaseFragment<FragmentThemeBinding>() {
    override fun bindingView(): FragmentThemeBinding {
        return FragmentThemeBinding.inflate(layoutInflater)
    }

    companion object {
        fun newInstance(): ThemeFragment {
            return ThemeFragment()
        }
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)
        binding.toolbar.tvTitle.setText(R.string.themes)
    }

    override fun initListener() {
        super.initListener()
        binding.toolbar.btnSetting.setOnClickListener {
            startActivity(SettingActivity.Companion.newIntent(requireContext()))
        }
    }
}