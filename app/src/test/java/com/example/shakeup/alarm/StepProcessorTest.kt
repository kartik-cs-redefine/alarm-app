package com.example.shakeup.alarm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StepProcessorTest {

    @Test
    fun stepAccumulation_triggersDismissWhenReached() {
        var progressCount = 0
        var dismissed = false

        val processor = StepProcessor(
            requiredSteps = 5,
            onStepProgress = { count, _ -> progressCount = count },
            onDismiss = { dismissed = true }
        )

        for (i in 1..4) {
            processor.onStepDetected()
            assertEquals(i, progressCount)
            org.junit.Assert.assertFalse(dismissed)
        }

        processor.onStepDetected() // 5th step
        assertEquals(5, progressCount)
        assertTrue(dismissed)
    }
}
