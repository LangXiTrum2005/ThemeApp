package com.wavez.controllerwidget.ui.home.fragment.mygallery.page

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.wavez.controllerwidget.core.base.BaseFragment
import com.wavez.controllerwidget.core.base.UiState
import com.wavez.controllerwidget.core.extensions.collectWithLifecycle
import com.wavez.controllerwidget.data.model.theme.ThemeModel
import com.wavez.controllerwidget.databinding.FragmentMyGalleryPageBinding
import com.wavez.controllerwidget.ui.home.fragment.mygallery.adapter.MyGalleryThemeAdapter
import com.wavez.controllerwidget.ui.home.fragment.mygallery.viewmodel.MyGalleryViewModel
import com.wavez.controllerwidget.ui.theme.activities.PreviewThemeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyGalleryThemesFragment : BaseFragment<FragmentMyGalleryPageBinding>() {

    override fun bindingView(): FragmentMyGalleryPageBinding {
        return FragmentMyGalleryPageBinding.inflate(layoutInflater)
    }

    companion object {
        fun newInstance(): MyGalleryThemesFragment = MyGalleryThemesFragment()
    }

    private val viewModel by activityViewModels<MyGalleryViewModel>()
    private val adapter = MyGalleryThemeAdapter()

    override fun initView(view: View, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)
        binding.rcvGallery.adapter = adapter
    }

    override fun initListener() {
        super.initListener()
        adapter.onClickItem = { theme ->
            startActivity(PreviewThemeActivity.newIntent(requireContext(), theme.id))
        }
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.themesState.collectWithLifecycle(this) { state ->
            renderState(state)
        }
    }

    private fun renderState(state: UiState<List<ThemeModel>>) {
        when (state) {
            is UiState.Data -> {
                adapter.set(state.value)
                binding.viewLoading.isVisible = false
                binding.rcvGallery.isVisible = state.value.isNotEmpty()
                binding.tvEmpty.isVisible = state.value.isEmpty()
            }

            is UiState.Loading -> {
                binding.viewLoading.isVisible = true
                binding.rcvGallery.isVisible = false
                binding.tvEmpty.isVisible = false
            }

            is UiState.Error -> {
                adapter.set(emptyList())
                binding.viewLoading.isVisible = false
                binding.rcvGallery.isVisible = false
                binding.tvEmpty.isVisible = true
            }

            else -> Unit
        }
    }
}
