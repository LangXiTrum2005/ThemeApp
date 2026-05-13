package com.wavez.themewallpaper.ui.launch

import android.app.Activity
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.wavez.themewallpaper.R
import com.wavez.themewallpaper.core.base.BaseActivity
import com.wavez.themewallpaper.databinding.ActivityLaunchBinding
import com.wavez.themewallpaper.ui.home.MainActivity
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