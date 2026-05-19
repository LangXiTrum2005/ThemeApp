package com.wavez.controllerwidget.ui.home.fragment

import com.example.m65_gamemodefps.core.base.BaseFragment
import com.wavez.controllerwidget.databinding.FragmentIconBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IconFragment : BaseFragment<FragmentIconBinding>() {
    override fun bindingView(): FragmentIconBinding {
        return FragmentIconBinding.inflate(layoutInflater)
    }

    companion object {
        fun newInstance(): IconFragment {
            return IconFragment()
        }
    }

}