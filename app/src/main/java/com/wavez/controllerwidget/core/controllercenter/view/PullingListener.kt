package com.wavez.controllerwidget.core.controllercenter.view

interface PullingListener {

    fun onPull(distance: Float, isForward: Boolean)

    fun onRelease(distance: Float)

    fun onCancel(distance: Float)
}