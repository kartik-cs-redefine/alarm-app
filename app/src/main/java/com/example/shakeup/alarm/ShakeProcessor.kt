package com.example.shakeup.alarm

import com.example.shakeup.domain.ShakeSensitivity
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Core shake calculation processor that isolates user movement from gravity (~9.81 m/s²)
 * and handles debouncing and shake counting.
 */
class ShakeProcessor(
    private val sensitivity: ShakeSensitivity,
    private val onShakeProgress: (currentShakes: Int, requiredShakes: Int) -> Unit,
    private val onDismiss: () -> Unit
) {
    private var currentShakeCount = 0
    private var lastShakeTime: Long = 0

    fun processAcceleration(x: Float, y: Float, z: Float, timestampMillis: Long = System.currentTimeMillis()) {
        // Accelerometer includes Earth's gravity (~9.81 m/s²).
        // Subtracting gravity magnitude isolates active user shake motion.
        val totalAcc = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val netAcc = abs(totalAcc - 9.81f)

        if (netAcc >= sensitivity.thresholdG) {
            if (timestampMillis - lastShakeTime >= 220) {
                lastShakeTime = timestampMillis
                currentShakeCount++
                onShakeProgress(currentShakeCount, sensitivity.requiredShakes)

                if (currentShakeCount >= sensitivity.requiredShakes) {
                    onDismiss()
                }
            }
        }
    }

    fun reset() {
        currentShakeCount = 0
        lastShakeTime = 0
    }
}
