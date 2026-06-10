package com.wavez.themewallpaper.ui.onboarding.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BoardingData(
    val index: Int,
    val imageID: Int,
    val labelID: Int,
    val desID: Int,
) : Parcelable