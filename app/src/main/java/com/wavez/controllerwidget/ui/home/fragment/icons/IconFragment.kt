package com.wavez.controllerwidget.ui.home.fragment.icons

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.example.m65_gamemodefps.core.base.BaseFragment
import com.wavez.controllerwidget.R
import com.wavez.controllerwidget.core.base.UiState
import com.wavez.controllerwidget.core.extensions.collectWithLifecycle
import com.wavez.controllerwidget.databinding.FragmentIconBinding
import com.wavez.controllerwidget.ui.home.fragment.icons.adapter.IconAdapter
import com.wavez.controllerwidget.ui.home.fragment.icons.viewmodel.IconsViewModel
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

    private val viewModel by viewModels<IconsViewModel>()

    private val iconAdapter = IconAdapter()

    override fun initView(view: View, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)
        binding.toolbar.tvTitle.text = getString(R.string.icons)
        binding.rcvIcons.adapter = iconAdapter
    }

    override fun initListener() {
        super.initListener()

        binding.viewError.setOnRetryClickListener {
            viewModel.getIcons()
        }

    }

    override fun initObserver() {
        super.initObserver()

        viewModel.iconResult.collectWithLifecycle(this) {
            when (it) {
                is UiState.Data -> {
                    Log.d("Log_tabbb", "Data: ${it}")
                    binding.viewError.isVisible = false
                    binding.viewLoading.isVisible = false
                    iconAdapter.set(it.value)
                }

                is UiState.Error -> {
                    Log.d("Log_tabbb", "initObserver: ${it.message}")
                    binding.viewError.isVisible = true
                    binding.viewLoading.isVisible = false
                }

                UiState.Loading -> {
                    binding.viewLoading.isVisible = true
                    binding.viewError.isVisible = false
                }

                else -> {}
            }
        }
    }


}