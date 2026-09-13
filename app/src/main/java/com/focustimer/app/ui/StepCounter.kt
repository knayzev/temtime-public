package com.focustimer.app.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/**
 * Live step count from the device's step counter sensor (steps since last reboot),
 * or null while unavailable/disabled.
 */
@Composable
fun rememberLiveStepCount(enabled: Boolean): Int? {
    val context = LocalContext.current
    var stepCount by remember { mutableStateOf<Int?>(null) }

    DisposableEffect(enabled) {
        var listener: SensorEventListener? = null
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        if (enabled && sensorManager != null) {
            val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            if (sensor != null) {
                listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        stepCount = event.values[0].toInt()
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                }
                sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
        } else {
            stepCount = null
        }
        onDispose {
            listener?.let { sensorManager?.unregisterListener(it) }
        }
    }

    return stepCount
}
