package com.wavez.controllerwidget.ui.home.fragment

import com.example.m65_gamemodefps.core.base.BaseFragment
import com.wavez.controllerwidget.databinding.FragmentThemeBinding
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

}