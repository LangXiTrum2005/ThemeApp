package com.wavez.themewallpaper.ui.iconpack.dialog

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.wavez.themewallpaper.core.base.BaseBottomSheetDialog
import com.wavez.themewallpaper.core.base.UiState
import com.wavez.themewallpaper.core.extensions.collectWithLifecycle
import com.wavez.themewallpaper.data.model.AppInfo
import com.wavez.themewallpaper.databinding.BottomSheetSelectAppBinding
import com.wavez.themewallpaper.ui.iconpack.adapter.SelectAppAdapter
import com.wavez.themewallpaper.ui.iconpack.viewmodel.SelectAppViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectAppBottomSheetDialog : BaseBottomSheetDialog<BottomSheetSelectAppBinding>() {

    override fun bindingView() = BottomSheetSelectAppBinding.inflate(layoutInflater)

    companion object {
        private const val TAG = "SelectAppBottomSheetDialog"

        fun newInstance() = SelectAppBottomSheetDialog()
    }

    override val hideNavigationBar: Boolean
        get() = true

    private val viewModel by viewModels<SelectAppViewModel>()
    private val adapter = SelectAppAdapter()

    private var listener: OnAppSelectedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? OnAppSelectedListener ?: parentFragment as? OnAppSelectedListener
    }

    override fun initConfig(view: View, savedInstanceState: Bundle?) {
        binding.rcvApps.adapter = adapter
    }

    override fun initListener() {
        binding.btnClose.setOnClickListener { dismiss() }

        adapter.onClickItem = { app ->
            listener?.onAppSelected(app)
            dismiss()
        }
    }

    override fun initObserver() {
        viewModel.apps.collectWithLifecycle(this) { state ->
            when (state) {
                is UiState.Loading -> binding.rcvApps.alpha = 0.4f
                is UiState.Data -> {
                    binding.rcvApps.alpha = 1f
                    adapter.set(state.value)
                }

                else -> binding.rcvApps.alpha = 1f
            }
        }
    }

    interface OnAppSelectedListener {
        fun onAppSelected(appInfo: AppInfo)
    }

}
