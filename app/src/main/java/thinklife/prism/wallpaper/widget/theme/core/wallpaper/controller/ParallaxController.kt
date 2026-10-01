/*
 * Copyright (c) 2026 1Tabb. All rights reserved.
 * https://1tabb.com
 *
 * SPDX-License-Identifier: LicenseRef-1Tabb-Proprietary
 *
 * Proprietary and confidential. Contains trade secrets of 1Tabb.
 * Unauthorized copying, modification, distribution, reverse engineering,
 * decompilation or disassembly of this file, via any medium, is strictly
 * prohibited except with prior written permission of 1Tabb.
 */

package thinklife.prism.wallpaper.widget.theme.core.wallpaper.controller

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.math.PI

/**
 * Sensor-driven parallax controller.
 *
 * Emits [Offset] values derived from the device's rotation or gravity sensor.
 * Collect from this flow in a scope tied to your screen's lifecycle; the sensor
 * listener is automatically unregistered when the collector cancels.
 *
 * Usage (in ViewModel or Composable with rememberCoroutineScope):
 * ```
 * parallaxController.offsets(intensity = options.parallaxIntensity)
 *     .onEach { (x, y) -> updatePreviewOffset(x, y) }
 *     .launchIn(viewModelScope)
 * ```
 */
class ParallaxController(context: Context) {

    data class Offset(val x: Float, val y: Float)

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    val isSupported: Boolean
        get() = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null ||
                sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null

    /**
     * Returns a cold Flow of [Offset] values in the range [-intensity, +intensity].
     * Only emits when the value actually changes ([distinctUntilChanged]).
     *
     * @param intensity Maximum shift magnitude (0..1).
     */
    fun offsets(intensity: Float = 0.3f): Flow<Offset> =
        rawTiltFlow()
            .map { (pitch, roll) ->
                Offset(
                    x = (roll  * intensity).coerceIn(-intensity, intensity),
                    y = (pitch * intensity).coerceIn(-intensity, intensity),
                )
            }
            .distinctUntilChanged()

    // ── Raw sensor flow ───────────────────────────────────────────────────────

    /**
     * Emits (pitch, roll) normalized to [-1, 1].
     * Prefers TYPE_ROTATION_VECTOR; falls back to TYPE_GRAVITY on devices that lack it.
     */
    private fun rawTiltFlow(): Flow<Pair<Float, Float>> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)

        if (sensor == null) {
            close(UnsupportedOperationException("No suitable motion sensor found."))
            return@callbackFlow
        }

        val rotMatrix  = FloatArray(9)
        val orientation = FloatArray(3)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR -> {
                        SensorManager.getRotationMatrixFromVector(rotMatrix, event.values)
                        SensorManager.getOrientation(rotMatrix, orientation)
                        // orientation[1] = pitch, orientation[2] = roll, both in radians ≈ ±π/2
                        val halfPi = (PI / 2).toFloat()
                        val pitch = (orientation[1] / halfPi).coerceIn(-1f, 1f)
                        val roll  = (orientation[2] / halfPi).coerceIn(-1f, 1f)
                        trySend(pitch to roll)
                    }
                    Sensor.TYPE_GRAVITY -> {
                        val g     = SensorManager.GRAVITY_EARTH
                        val pitch = (-event.values[1] / g).coerceIn(-1f, 1f)
                        val roll  = ( event.values[0] / g).coerceIn(-1f, 1f)
                        trySend(pitch to roll)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
        }

        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        awaitClose { sensorManager.unregisterListener(listener) }
    }
}
