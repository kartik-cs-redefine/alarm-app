package com.example.shakeup.alarm

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.shakeup.domain.ShakeSensitivity
import kotlin.math.sqrt

class ShakeDetector(
    private val sensitivity: ShakeSensitivity,
    private val onShakeProgress: (currentShakes: Int, requiredShakes: Int) -> Unit,
    private val onDismiss: () -> Unit
) : SensorEventListener {

    private var currentShakeCount = 0
    private var lastShakeTime: Long = 0

    fun start(sensorManager: SensorManager): Boolean {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return false
        return sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
    }

    fun stop(sensorManager: SensorManager) {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Calculate total acceleration magnitude
        val acceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

        // Check if acceleration exceeds sensitivity threshold
        if (acceleration > sensitivity.thresholdG) {
            val now = System.currentTimeMillis()
            // Debounce shakes by at least 220ms
            if (now - lastShakeTime >= 220) {
                lastShakeTime = now
                currentShakeCount++
                onShakeProgress(currentShakeCount, sensitivity.requiredShakes)

                if (currentShakeCount >= sensitivity.requiredShakes) {
                    onDismiss()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }

    fun reset() {
        currentShakeCount = 0
        lastShakeTime = 0
    }
}
