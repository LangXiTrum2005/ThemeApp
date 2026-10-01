package com.wavez.themewallpaper.ui.setting.feedback

import android.net.Uri

data class Feedback(
    val description: String,
    val images: List<Uri>
)