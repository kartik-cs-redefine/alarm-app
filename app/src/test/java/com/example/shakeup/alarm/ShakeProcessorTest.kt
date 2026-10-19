package com.example.shakeup.alarm

import com.example.shakeup.domain.ShakeSensitivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShakeProcessorTest {

    @Test
    fun belowThreshold_ignored() {
        var progressCalled = false
        val processor = ShakeProcessor(
            sensitivity = ShakeSensitivity.EASY, // threshold = 6.0
            onShakeProgress = { _, _ -> progressCalled = true },
            onDismiss = {}
        )

        // Still acceleration (approx 9.81), net acceleration = 0
        processor.processAcceleration(0f, 0f, 9.81f, 1000L)
        assertFalse(progressCalled)
    }

    private fun assertFalse(condition: Boolean) {
        org.junit.Assert.assertFalse(condition)
    }

    @Test
    fun aboveThreshold_registersShake() {
        var progressCount = 0
        val processor = ShakeProcessor(
            sensitivity = ShakeSensitivity.EASY, // threshold = 6.0, requiredShakes = 10
            onShakeProgress = { count, _ -> progressCount = count },
            onDismiss = {}
        )

        // Provide high acceleration (e.g. 20 m/s^2), net acceleration ~ 10.19 > 6.0
        processor.processAcceleration(10f, 10f, 10f, 1000L)
        assertEquals(1, progressCount)
    }

    @Test
    fun debounce_ignoresRapidShakes() {
        var progressCount = 0
        val processor = ShakeProcessor(
            sensitivity = ShakeSensitivity.EASY,
            onShakeProgress = { count, _ -> progressCount = count },
            onDismiss = {}
        )

        // First shake at t = 1000
        processor.processAcceleration(15f, 15f, 15f, 1000L)
        assertEquals(1, progressCount)

        // Rapid second shake at t = 1100 (< 220ms debounce)
        processor.processAcceleration(15f, 15f, 15f, 1100L)
        assertEquals(1, progressCount) // Should be ignored by debounce

        // Third shake at t = 1300 (>= 220ms debounce)
        processor.processAcceleration(15f, 15f, 15f, 1300L)
        assertEquals(2, progressCount)
    }

    @Test
    fun exactDismissCount_triggersDismiss() {
        var dismissed = false
        val processor = ShakeProcessor(
            sensitivity = ShakeSensitivity.EASY, // requiredShakes = 10
            onShakeProgress = { _, _ -> },
            onDismiss = { dismissed = true }
        )

        var time = 1000L
        for (i in 1..10) {
            processor.processAcceleration(20f, 20f, 20f, time)
            time += 300L
        }

        assertTrue(dismissed)
    }

    private fun assertTrue(condition: Boolean) {
        org.junit.Assert.assertTrue(condition)
    }
}
