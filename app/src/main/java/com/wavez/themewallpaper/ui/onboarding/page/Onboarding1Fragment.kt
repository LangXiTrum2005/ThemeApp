package com.wavez.themewallpaper.ui.onboarding.page

import android.os.Bundle
import android.view.Gravity
import android.view.View
import com.wavez.themewallpaper.R
import com.wavez.themewallpaper.core.base.BaseFragment
import com.wavez.themewallpaper.core.utils.CustomToast
import com.wavez.themewallpaper.databinding.FragmentOnboarding1Binding
import com.wavez.themewallpaper.ui.onboarding.WalkthroughActivity
import com.wavez.themewallpaper.ui.onboarding.adapter.HabitsAdapter
import com.wavez.themewallpaper.ui.onboarding.provider.HabitsProvider
import com.wavez.themewallpaper.ui.utils.AppEvent
import com.wavez.themewallpaper.ui.utils.FirebaseControlLogEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Onboarding1Fragment : BaseFragment<FragmentOnboarding1Binding>() {

    override fun bindingView() = FragmentOnboarding1Binding.inflate(layoutInflater)

    companion object {
        fun newInstance() = Onboarding1Fragment()
    }

    private var isClickedFirst = false

    private val habitsAdapter by lazy { HabitsAdapter() }

    override fun initConfig(view: View, savedInstanceState: Bundle?) {
        super.initConfig(view, savedInstanceState)
        initHabits()
        FirebaseControlLogEvent.logEvent(AppEvent.ONBOARDING_1_VIEW)
    }

    override fun initListener() {
        super.initListener()
        binding.btnNext.setOnClickListener {
            if (habitsAdapter.getSelectedItems().isEmpty()) {
                showSelectToast()
            } else {
                HabitsProvider.pushEvent(habitsAdapter.getSelectedItems())
                FirebaseControlLogEvent.logEvent(AppEvent.ONBOARDING_1_CLICK_NEXT)
                (activity as WalkthroughActivity).nextScreen()
            }
        }
    }

    private fun initHabits() {
        with(binding.viewObd1) {
            rcvHabits.adapter = habitsAdapter
            habitsAdapter.set(HabitsProvider.getHabits())
            habitsAdapter.onItemSelected = {
                if (!isClickedFirst) {
                    FirebaseControlLogEvent.logEvent(AppEvent.ONBOARDING_1_CLICK)
                    isClickedFirst = true
                }
                binding.btnNext.isSelected = habitsAdapter.getSelectedItems().isNotEmpty()
            }
        }
    }

    private fun showSelectToast() {
        CustomToast.makeText(
            requireContext(),
            R.string.select_option,
            bgDrawable = R.drawable.bg_toas,
            gravity = Gravity.TOP,
            textColor = R.color.white,
        ).show()
    }
}
