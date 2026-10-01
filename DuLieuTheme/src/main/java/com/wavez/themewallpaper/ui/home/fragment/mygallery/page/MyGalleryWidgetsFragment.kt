package com.wavez.themewallpaper.ui.home.fragment.mygallery.page

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.wavez.themewallpaper.core.base.BaseFragment
import com.wavez.themewallpaper.core.base.UiState
import com.wavez.themewallpaper.core.extensions.collectWithLifecycle
import com.wavez.themewallpaper.data.model.widget.WidgetsModel
import com.wavez.themewallpaper.databinding.FragmentMyGalleryPageBinding
import com.wavez.themewallpaper.ui.home.fragment.mygallery.adapter.MyGalleryWidgetAdapter
import com.wavez.themewallpaper.ui.home.fragment.mygallery.viewmodel.MyGalleryViewModel
import com.wavez.themewallpaper.ui.home.fragment.widget.dialog.AddWidgetsBottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyGalleryWidgetsFragment : BaseFragment<FragmentMyGalleryPageBinding>() {

    override fun bindingView(): FragmentMyGalleryPageBinding {
        return FragmentMyGalleryPageBinding.inflate(layoutInflater)
    }

    companion object {
        fun newInstance(): MyGalleryWidgetsFragment = MyGalleryWidgetsFragment()
    }

    private val viewModel by activityViewModels<MyGalleryViewModel>()
    private val adapter = MyGalleryWidgetAdapter()

    override fun initView(view: View, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)
        binding.rcvGallery.adapter = adapter
    }

    override fun initListener() {
        super.initListener()
        adapter.onClickItem = { widget ->
            AddWidgetsBottomSheetDialog.newInstance(widget)
                .show(childFragmentManager, AddWidgetsBottomSheetDialog.TAG)
        }
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.widgetsState.collectWithLifecycle(this) { state ->
            renderState(state)
        }
    }

    private fun renderState(state: UiState<List<WidgetsModel>>) {
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
