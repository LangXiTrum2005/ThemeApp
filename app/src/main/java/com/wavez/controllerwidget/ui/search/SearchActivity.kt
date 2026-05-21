package com.wavez.controllerwidget.ui.search

import android.content.Context
import android.content.Intent
import com.wavez.controllerwidget.core.base.BaseActivity
import com.wavez.controllerwidget.databinding.ActivitySearchBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchActivity : BaseActivity<ActivitySearchBinding>() {
    override fun bindingView(): ActivitySearchBinding {
        return ActivitySearchBinding.inflate(layoutInflater)
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, SearchActivity::class.java)
        }
    }

}