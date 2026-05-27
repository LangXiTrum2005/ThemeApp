package com.wavez.controllerwidget.ui.home.fragment.theme.page

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.wavez.controllerwidget.core.base.BaseFragment
import com.wavez.controllerwidget.core.base.UiState
import com.wavez.controllerwidget.core.extensions.collectWithLifecycle
import com.wavez.controllerwidget.databinding.FragmentThemeCategoryBinding
import com.wavez.controllerwidget.ui.home.fragment.theme.adapter.ThemeAdapter
import com.wavez.controllerwidget.ui.home.fragment.theme.viewmodel.ThemePageViewModel
import com.wavez.controllerwidget.ui.theme.activities.PreviewThemeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ThemeCategoryFragment : BaseFragment<FragmentThemeCategoryBinding>() {

    override fun bindingView(): FragmentThemeCategoryBinding {
        return FragmentThemeCategoryBinding.inflate(layoutInflater)
    }

    private val categoryId: String by lazy {
        arguments?.getString(ARG_CATEGORY_ID).orEmpty()
    }

    private val themeAdapter = ThemeAdapter()
    private val viewModel by viewModels<ThemePageViewModel>()

    override fun initConfig(view: View, savedInstanceState: Bundle?) {
        super.initConfig(view, savedInstanceState)
        binding.rcvTheme.adapter = themeAdapter
        viewModel.getThemesByCategory(categoryId)
    }

    override fun initListener() {
        super.initListener()
        themeAdapter.onClickItem = { theme ->
            startActivity(PreviewThemeActivity.newIntent(requireContext(), theme.id))
        }

        binding.viewError.setOnRetryClickListener {
            viewModel.getThemesByCategory(categoryId, forceRefresh = true)
        }
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.themesState.collectWithLifecycle(this) { state ->
            when (state) {
                is UiState.Error -> {
                    Log.d("ThemeCategory", "load failed: ${state.throwable}")
                    binding.viewError.isVisible = true
                    binding.rcvTheme.isVisible = false
                }

                UiState.Loading -> {
                    binding.viewError.isVisible = false
                }

                is UiState.Data -> {
                    binding.viewError.isVisible = false
                    binding.rcvTheme.isVisible = true
                    themeAdapter.set(state.value)
                }

                else -> Unit
            }
        }
    }

    companion object {
        private const val ARG_CATEGORY_ID = "category_id"

        fun newInstance(categoryId: String): ThemeCategoryFragment {
            return ThemeCategoryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CATEGORY_ID, categoryId)
                }
            }
        }
    }
}
