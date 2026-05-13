package com.wavez.controllerwidget.ui.launch

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.wavez.controllerwidget.core.base.BaseActivity
import com.wavez.controllerwidget.databinding.ActivityLaunchBinding
import com.wavez.controllerwidget.ui.home.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds
@AndroidEntryPoint
class LaunchActivity : BaseActivity<ActivityLaunchBinding>() {
    override fun bindingView(): ActivityLaunchBinding {
        return ActivityLaunchBinding.inflate(layoutInflater)
    }

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
        lifecycleScope.launch {
            delay(2.seconds)
            startActivity(MainActivity.newIntent(this@LaunchActivity))
        }
    }
}