package com.example.shakeup.alarm

class StepProcessor(
    private val requiredSteps: Int,
    private val onStepProgress: (currentSteps: Int, requiredSteps: Int) -> Unit,
    private val onDismiss: () -> Unit
) {
    private var currentSteps = 0

    fun onStepDetected() {
        currentSteps++
        onStepProgress(currentSteps, requiredSteps)
        if (currentSteps >= requiredSteps) {
            onDismiss()
        }
    }

    fun reset() {
        currentSteps = 0
    }
}
