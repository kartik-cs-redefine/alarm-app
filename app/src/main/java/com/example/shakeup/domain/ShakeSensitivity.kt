package com.example.shakeup.domain

enum class ShakeSensitivity(
    val thresholdG: Float, // Net acceleration threshold in m/s^2 (excluding gravity)
    val requiredShakes: Int // Number of shakes needed to dismiss
) {
    EASY(thresholdG = 6.0f, requiredShakes = 10),
    MEDIUM(thresholdG = 10.0f, requiredShakes = 20),
    HARD(thresholdG = 15.0f, requiredShakes = 30)
}
