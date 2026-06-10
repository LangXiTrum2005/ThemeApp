package com.wavez.themewallpaper.ui.home.fragment.theme.page

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.wavez.themewallpaper.core.base.BaseFragment
import com.wavez.themewallpaper.core.base.UiState
import com.wavez.themewallpaper.core.extensions.collectWithLifecycle
import com.wavez.themewallpaper.databinding.FragmentThemeCategoryBinding
import com.wavez.themewallpaper.ui.home.fragment.theme.adapter.ThemeAdapter
import com.wavez.themewallpaper.ui.home.fragment.theme.viewmodel.ThemePageViewModel
import com.wavez.themewallpaper.ui.theme.activities.PreviewThemeActivity
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
            navigateTo(PreviewThemeActivity.newIntent(requireContext(), theme.id, true))
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
                    setViewState(showContent = false, showLoading = false, showError = true)
                }

                UiState.Loading -> {
                    setViewState(showContent = false, showLoading = true, showError = false)
                }

                is UiState.Data -> {
                    setViewState(showContent = true, showLoading = false, showError = false)
                    themeAdapter.set(state.value)
                }

                else -> Unit
            }
        }
    }

    private fun setViewState(showContent: Boolean, showLoading: Boolean, showError: Boolean) {
        binding.rcvTheme.isVisible = showContent
        binding.viewLoading.isVisible = showLoading
        binding.viewError.isVisible = showError
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
