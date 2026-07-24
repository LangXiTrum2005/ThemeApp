package com.wavez.themewallpaper.ui.home.fragment.widget.page

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.wavez.themewallpaper.core.base.BaseFragment
import com.wavez.themewallpaper.core.base.UiState
import com.wavez.themewallpaper.core.extensions.collectWithLifecycle
import com.wavez.themewallpaper.databinding.FragmentWallpaperCategoryBinding
import com.wavez.themewallpaper.ui.home.fragment.widget.adapter.WidgetAdapter
import com.wavez.themewallpaper.ui.home.fragment.widget.dialog.AddWidgetsBottomSheetDialog
import com.wavez.themewallpaper.ui.home.fragment.widget.viewmodel.WidgetPageViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WidgetCategoryFragment : BaseFragment<FragmentWallpaperCategoryBinding>() {

    override fun bindingView(): FragmentWallpaperCategoryBinding =
        FragmentWallpaperCategoryBinding.inflate(layoutInflater)

    companion object {
        private const val ARG_CATEGORY_ID = "category_id"

        fun newInstance(categoryId: String) = WidgetCategoryFragment().apply {
            arguments = Bundle().apply { putString(ARG_CATEGORY_ID, categoryId) }
        }
    }

    private val categoryId by lazy { arguments?.getString(ARG_CATEGORY_ID).orEmpty() }
    private val widgetAdapter = WidgetAdapter()
    private val viewModel by viewModels<WidgetPageViewModel>()

    override fun initConfig(view: View, savedInstanceState: Bundle?) {
        super.initConfig(view, savedInstanceState)
        binding.rcvWallpaper.adapter = widgetAdapter
        viewModel.getWidgetsByCategory(categoryId)
    }

    override fun initListener() {
        super.initListener()
        widgetAdapter.onClickItem = { widget ->
            AddWidgetsBottomSheetDialog.newInstance(widget)
                .show(childFragmentManager, AddWidgetsBottomSheetDialog.TAG)
        }
        binding.viewError.setOnRetryClickListener {
            viewModel.getWidgetsByCategory(categoryId, forceRefresh = true)
        }
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.widgetsState.collectWithLifecycle(this) { state ->
            when (state) {
                is UiState.Data -> {
                    setViewState(showContent = true, showLoading = false, showError = false)
                    widgetAdapter.set(state.value)
                }

                is UiState.Error -> {
                    setViewState(showContent = false, showLoading = false, showError = true)
                }

                UiState.Loading -> {
                    setViewState(showContent = false, showLoading = true, showError = false)
                }

                UiState.Idle -> Unit
            }
        }
    }

    private fun setViewState(showContent: Boolean, showLoading: Boolean, showError: Boolean) {
        binding.rcvWallpaper.isVisible = showContent
        binding.viewLoading.isVisible = showLoading
        binding.viewError.isVisible = showError
    }
}
