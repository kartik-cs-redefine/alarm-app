package com.example.shakeup.alarm

import com.example.shakeup.domain.ShakeSensitivity
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Core shake calculation processor that isolates user movement from gravity (~9.81 m/s²)
 * and handles debouncing and shake counting.
 *
 * Threshold Justifications (values in ShakeSensitivity):
 * - EASY (6.0 m/s²): Detects light hand shakes beyond standard gravity. Requires 10 shakes.
 * - MEDIUM (10.0 m/s²): Requires intentional moderate shaking (approx. 1g of user acceleration above gravity). Requires 20 shakes.
 * - HARD (15.0 m/s²): Requires vigorous physical movement (approx. 1.5g net acceleration). Requires 30 shakes.
 *
 * Gravity Subtraction Justification:
 * Accelerometer readings measure total specific force including Earth's gravity (~9.81 m/s²).
 * When at rest, |(0, 0, 9.81)| = 9.81 m/s². Subtracting 9.81 m/s² calculates net user-applied acceleration
 * independent of device orientation (portrait, landscape, or flat on table).
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
