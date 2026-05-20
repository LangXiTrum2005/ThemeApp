package com.wavez.controllerwidget.ui.iconpack.model

import com.wavez.controllerwidget.data.model.IconPackModel

enum class SetIconState { UNLOCK, INSTALL, CHOOSE }

data class IconPackDetailState(
    val pack: IconPackModel,
    val items: List<IconPackItemUi>,
)
