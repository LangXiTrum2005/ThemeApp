package com.wavez.controllerwidget.ui.iconpack.model

import com.wavez.controllerwidget.data.model.icon.IconPackModel

enum class SetIconState { APPLIED, UNLOCK, INSTALL, CHOOSE }

data class IconPackDetailState(
    val pack: IconPackModel,
    val items: List<IconPackItemUi>,
)
