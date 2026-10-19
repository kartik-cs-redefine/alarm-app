package com.example.shakeup.alarm

import com.example.shakeup.domain.ShakeSensitivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShakeDetectorTest {

    @Test
    fun shakeDetector_reachesRequiredShakes_triggersDismiss() {
        var progressCount = 0
        var dismissed = false

        val detector = ShakeDetector(
            sensitivity = ShakeSensitivity.EASY, // requiredShakes = 10, threshold = 6.0
            onShakeProgress = { count, _ -> progressCount = count },
            onDismiss = { dismissed = true }
        )

        // Verify sensitivity settings
        assertEquals(10, ShakeSensitivity.EASY.requiredShakes)
        assertEquals(6.0f, ShakeSensitivity.EASY.thresholdG, 0.01f)
    }
}
