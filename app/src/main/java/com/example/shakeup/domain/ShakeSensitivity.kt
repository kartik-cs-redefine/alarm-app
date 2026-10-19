package com.example.shakeup.domain

enum class ShakeSensitivity(
    val thresholdG: Float, // Acceleration threshold in m/s^2 (excluding gravity) or total g-force
    val requiredShakes: Int // Number of shakes needed to dismiss
) {
    EASY(thresholdG = 12.0f, requiredShakes = 10),
    MEDIUM(thresholdG = 16.0f, requiredShakes = 20),
    HARD(thresholdG = 22.0f, requiredShakes = 35)
}
