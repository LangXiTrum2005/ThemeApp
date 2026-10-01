package com.wavez.themewallpaper.ui.iconpack.model

import com.wavez.themewallpaper.data.model.icon.IconPackModel

enum class SetIconState { APPLIED, INSTALL, CHOOSE }

data class IconPackDetailState(
    val pack: IconPackModel,
    val items: List<IconPackItemUi>,
)
