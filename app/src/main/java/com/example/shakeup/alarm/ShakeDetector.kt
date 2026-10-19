package com.example.shakeup.alarm

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.shakeup.domain.ShakeSensitivity

class ShakeDetector(
    sensitivity: ShakeSensitivity,
    onShakeProgress: (currentShakes: Int, requiredShakes: Int) -> Unit,
    onDismiss: () -> Unit
) : SensorEventListener {

    private val processor = ShakeProcessor(sensitivity, onShakeProgress, onDismiss)

    fun start(sensorManager: SensorManager): Boolean {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return false
        return sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
    }

    fun stop(sensorManager: SensorManager) {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return
        processor.processAcceleration(event.values[0], event.values[1], event.values[2])
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }

    fun reset() {
        processor.reset()
    }
}
